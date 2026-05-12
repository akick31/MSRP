package com.msrp.backend.services.ebay

import com.msrp.backend.model.DailyItem
import com.msrp.backend.model.dto.VerifyResponse
import com.msrp.backend.repositories.DailyItemRepository
import com.msrp.backend.util.EbayParser
import com.msrp.backend.util.ItemNotFoundException
import com.msrp.backend.util.NoItemsAvailableException
import com.msrp.backend.util.SEARCH_CATEGORIES
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.CookieManager
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.ln

@Service
class EbayService(
    private val dailyItemRepository: DailyItemRepository,
) {
    private val log = LoggerFactory.getLogger(EbayService::class.java)

    private val itemsPerDay = 5
    private val minBidCount = 5
    private val maxConsecutiveFailures = 3
    private val requestDelayMs = 1500L..3000L
    private val httpRequestTimeout: Duration = Duration.ofSeconds(20)
    private val httpConnectTimeout: Duration = Duration.ofSeconds(10)

    private val desktopUa =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36"

    private val bidCountRegex = Regex("(\\d+)\\s+bid", RegexOption.IGNORE_CASE)

    private val httpClient: HttpClient =
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(httpConnectTimeout)
            .cookieHandler(CookieManager())
            .build()

    fun getItemsForDate(date: LocalDate): List<DailyItem> {
        val items = dailyItemRepository.findByGameDate(date)
        if (items.isEmpty()) throw NoItemsAvailableException()
        return items
    }

    fun verifyGuess(
        itemId: Long,
        guess: Double,
    ): VerifyResponse {
        val item = dailyItemRepository.findById(itemId).orElseThrow { ItemNotFoundException() }
        val percentageOff = (abs(guess - item.soldPrice) / item.soldPrice) * 100.0
        val roundedPercentageOff = Math.round(percentageOff * 100.0) / 100.0
        if (abs(guess - item.soldPrice) <= 1.0) {
            return VerifyResponse(itemId = item.id, guess = guess, actualPrice = item.soldPrice, percentageOff = roundedPercentageOff, score = 100)
        }
        val ratio = if (guess > 0) maxOf(guess / item.soldPrice, item.soldPrice / guess) else Double.MAX_VALUE
        val logPenalty = 16.0 * ln(ratio)
        val scalePenalty = 13.0 * abs(guess - item.soldPrice) / (10.0 + 0.15 * item.soldPrice)
        val score = (100.0 - logPenalty - scalePenalty).toInt().coerceIn(0, 100)
        return VerifyResponse(
            itemId = item.id,
            guess = guess,
            actualPrice = item.soldPrice,
            percentageOff = roundedPercentageOff,
            score = score,
        )
    }

    fun curateDailyItems(targetDate: LocalDate = LocalDate.now().plusDays(1)) {
        val existing = dailyItemRepository.findByGameDate(targetDate)
        if (existing.size >= itemsPerDay) {
            log.info("Items for {} already curated, skipping", targetDate)
            return
        }

        val usedIds = existing.map { it.ebayItemId }.toMutableSet()
        val curatedItems = existing.toMutableList()
        val shuffledCategories = SEARCH_CATEGORIES.shuffled().toMutableList()
        var consecutiveFailures = 0

        while (curatedItems.size < itemsPerDay && shuffledCategories.isNotEmpty()) {
            if (consecutiveFailures >= maxConsecutiveFailures) {
                log.warn("Stopping curation after {} consecutive failures — eBay may be rate-limiting", consecutiveFailures)
                break
            }

            val keyword = shuffledCategories.removeFirst()
            log.info("Scraping '{}' ({}/{})", keyword, curatedItems.size + 1, itemsPerDay)

            val pageItems = scrapeCompletedAuctions(keyword)
            val usable = pageItems.filter { it.ebayItemId !in usedIds }

            if (usable.isEmpty()) {
                consecutiveFailures++
                if (pageItems.isNotEmpty()) {
                    log.warn("Keyword '{}' returned items but all already used", keyword)
                } else {
                    log.warn("No eligible items for keyword '{}'", keyword)
                }
                continue
            }

            consecutiveFailures = 0
            val pick = usable.random()
            pick.gameDate = targetDate
            curatedItems.add(pick)
            usedIds.add(pick.ebayItemId)
            log.info("Selected from '{}': {} — \${} ({} bids)", keyword, pick.title, pick.soldPrice, pick.bidCount)
        }

        val newItems = curatedItems.filter { it.id == 0L }
        if (newItems.isNotEmpty()) {
            dailyItemRepository.saveAll(newItems)
            log.info("Saved {} items for {}", newItems.size, targetDate)
        }
        if (curatedItems.size < itemsPerDay) {
            log.warn("Only curated {}/{} items for {}", curatedItems.size, itemsPerDay, targetDate)
        }
    }

    private fun fetchHtml(
        url: String,
        userAgent: String,
        referer: String?,
    ): String? {
        val builder =
            HttpRequest.newBuilder(URI.create(url))
                .timeout(httpRequestTimeout)
                .GET()
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Cache-Control", "max-age=0")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", if (referer != null) "same-origin" else "none")
                .header("Sec-Fetch-User", "?1")
                .header("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not-A.Brand\";v=\"24\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "\"macOS\"")
        if (referer != null) builder.header("Referer", referer)

        return try {
            val response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray())
            if (response.statusCode() !in 200..299) {
                log.warn("HTTP {} fetching {}", response.statusCode(), url)
                return null
            }
            val encoding = response.headers().firstValue("Content-Encoding").orElse("").lowercase()
            val raw = response.body()
            val bytes =
                when (encoding) {
                    "gzip" -> java.util.zip.GZIPInputStream(raw.inputStream()).use { it.readBytes() }
                    "deflate" -> java.util.zip.InflaterInputStream(raw.inputStream()).use { it.readBytes() }
                    else -> raw
                }
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            log.warn("HTTP fetch failed for {}: {}", url, e.message)
            null
        }
    }

    private fun scrapeCompletedAuctions(keyword: String): List<DailyItem> {
        Thread.sleep(requestDelayMs.random())

        val encoded = URLEncoder.encode(keyword, "UTF-8")
        val url = "https://www.ebay.com/sch/i.html?_nkw=$encoded&LH_Complete=1&LH_Sold=1&LH_Auction=1&_pgn=1&_ipg=60&rt=nc"

        val html = fetchHtml(url, desktopUa, referer = "https://www.ebay.com/")
        val rows = html?.let { EbayParser.parseSerpRows(it) }.orEmpty()

        if (rows.isEmpty()) {
            log.warn("SERP returned no list rows for '{}' (blocked or layout change)", keyword)
            return emptyList()
        }

        val results = mutableListOf<DailyItem>()
        val seen = mutableSetOf<String>()

        for (el in rows) {
            try {
                val title = EbayParser.extractSerpTitle(el)
                if (title.isBlank() || title == "Shop on eBay") continue

                val href = EbayParser.extractSerpItemHref(el) ?: continue
                val itemId = Regex("/itm/(\\d+)").find(href)?.groupValues?.get(1) ?: continue
                if (itemId in seen) continue

                val priceText =
                    el.selectFirst(
                        ".s-card__price, .s-item__price, span[class*='s-card__price'], span[class*='s-item__price']",
                    )?.text() ?: continue
                val soldPrice = EbayParser.parsePrice(priceText) ?: continue
                if (soldPrice <= 0.0) continue

                val imageUrl = EbayParser.firstImageUrlFromImg(el.selectFirst("img")) ?: continue

                val bidCount = bidCountRegex.find(el.text())?.groupValues?.get(1)?.toIntOrNull() ?: continue
                if (bidCount < minBidCount) continue

                seen.add(itemId)
                val entity = DailyItem()
                entity.ebayItemId = itemId
                entity.title = title
                entity.imageUrl = imageUrl
                entity.soldPrice = soldPrice
                entity.bidCount = bidCount
                entity.itemUrl = "https://www.ebay.com/itm/$itemId?orig_cvip=true"
                entity.saleDate = EbayParser.extractSerpSaleDate(el)
                results.add(entity)
            } catch (_: Exception) {
                continue
            }
        }

        if (results.isEmpty()) {
            log.warn("SERP had {} raw rows for '{}' but none passed filters", rows.size, keyword)
        } else {
            log.info("Found {} eligible items for keyword '{}'", results.size, keyword)
        }
        return results
    }
}
