package com.msrp.backend.service.ebay

import com.msrp.backend.dto.VerifyResponse
import com.msrp.backend.model.DailyItem
import com.msrp.backend.repositories.DailyItemRepository
import com.msrp.backend.util.ItemNotFoundException
import com.msrp.backend.util.Logger
import com.msrp.backend.util.NoItemsAvailableException
import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import java.net.CookieManager
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

@Service
class EbayService(
    private val dailyItemRepository: DailyItemRepository,
) {

    companion object {
        private const val ITEMS_PER_DAY = 5
        private const val MIN_BID_COUNT = 5
        private const val MAX_SERP_PAGES = 3
        private val BID_COUNT_REGEX = Regex("(\\d+)\\s+bid", RegexOption.IGNORE_CASE)

        /** Real desktop Chrome on macOS — eBay serves SSR listings to it. */
        private const val DESKTOP_UA =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

        /** iPhone Safari — m.ebay.com responds with very lightweight, server-rendered cards. */
        private const val MOBILE_UA =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15 " +
                "(KHTML, like Gecko) Version/17.4 Mobile/15E148 Safari/604.1"

        private val HTTP_REQUEST_TIMEOUT: Duration = Duration.ofSeconds(20)
        private val HTTP_CONNECT_TIMEOUT: Duration = Duration.ofSeconds(10)

        /** SERP cards across desktop list/magazine + mobile layouts. */
        private val SERP_CARD_SELECTORS = listOf(
            "ul.srp-results > li.s-card",
            "ul.srp-results > li.s-item",
            "ul.srp-results li.s-card",
            "ul.srp-results li.s-item",
            "li.s-card",
            "li.s-item",
            "li[data-listingid]",
            ".srp-river-results li.s-card",
            ".srp-river-results li.s-item",
            "div.s-card",
            // Mobile site
            "li.brwrvr__item-card",
            "div.brwrvr__item-card",
        )

        private val SALE_DATE_FORMATS = listOf(
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH),
        )

        private val SEARCH_CATEGORIES = listOf(
            "baseball",
            "basketball",
            "football",
            "soccer",
            "hockey",
            "golf",
            "tennis",
            "boxing",
            "wrestling",
            "cycling",
            "volleyball",
            "softball",
            "lacrosse",
            "rugby",
            "cricket",
            "archery",
            "bowling",
            "skateboarding",
            "snowboarding",
            "skiing",
            "surfing",
            "swimming",
            "running",
            "track and field",
            "gymnastics",
            "weightlifting",
            "martial arts",
            "badminton",
            "rowing",
            "climbing",
            "fencing",
            "polo",
            "equestrian",
            "motocross",
            "paintball",
            "airsoft",
            "billiards",
            "darts",
            "curling",
            "table tennis",
            "fishing",
            "hunting",
            "camping",
            "hiking",
            "kayaking",
            "canoeing",
            "scuba diving",
            "rock climbing",
            "birdwatching",
            "metal detecting",
            "guitar",
            "bass guitar",
            "electric guitar",
            "acoustic guitar",
            "drum kit",
            "trumpet",
            "saxophone",
            "violin",
            "piano keyboard",
            "synthesizer",
            "mandolin",
            "banjo",
            "ukulele",
            "harmonica",
            "accordion",
            "cello",
            "flute",
            "clarinet",
            "trombone",
            "tuba",
            "guitar amplifier",
            "trading cards",
            "pokemon cards",
            "magic the gathering",
            "comic books",
            "vinyl records",
            "coins",
            "stamps",
            "sports memorabilia",
            "autographs",
            "vintage posters",
            "vintage signs",
            "military memorabilia",
            "political memorabilia",
            "movie memorabilia",
            "hot wheels",
            "diecast cars",
            "action figures",
            "lego",
            "model trains",
            "video games",
            "retro video games",
            "nintendo",
            "playstation",
            "xbox",
            "sega",
            "atari",
            "vintage electronics",
            "vintage radio",
            "ham radio",
            "vintage television",
            "vintage computer",
            "turntable",
            "vintage stereo",
            "oscilloscope",
            "vintage typewriter",
            "vintage calculator",
            "film camera",
            "vintage camera",
            "camera lens",
            "darkroom equipment",
            "jewelry",
            "watches",
            "vintage watches",
            "pocket watch",
            "sterling silver",
            "vintage handbags",
            "sneakers",
            "vintage clothing",
            "vintage denim",
            "vintage band tee",
            "antique furniture",
            "vintage lamps",
            "vintage clocks",
            "vintage kitchenware",
            "vintage barware",
            "pottery",
            "vintage glassware",
            "vintage porcelain",
            "vintage cast iron",
            "vintage sewing machines",
            "vintage tools",
            "vintage locks",
            "board games",
            "pinball machines",
            "arcade machines",
            "jukeboxes",
            "remote control cars",
            "vintage toys",
            "vintage barbie",
            "oil paintings",
            "sculpture",
            "native american art",
            "taxidermy",
            "vintage maps",
            "telescopes",
            "microscopes",
            "scientific instruments",
            "golf clubs",
            "bicycles",
            "motorcycle parts",
            "reading books first edition",
            "vintage medical equipment",
            "super bowl",
            "world series",
            "nba finals",
            "stanley cup",
            "world cup soccer",
            "olympics",
            "nascar",
            "kentucky derby",
            "masters golf",
            "wimbledon",
            "us open tennis",
            "tour de france",
            "world wrestling entertainment",
            "boxing championship",
            "ufc",
            "college football championship",
            "march madness",
            "world series of poker",
            "coachella",
            "woodstock",
            "grateful dead",
            "rolling stones",
            "beatles",
            "elvis presley",
            "michael jackson",
            "concert tour",
            "grammy awards",
            "burning man",
            "oktoberfest",
            "mardi gras",
            "world expo",
            "world fair",
            "presidential inauguration",
            "space shuttle",
            "apollo moon landing",
            "world war ii",
            "civil war",
            "telescope",
            "astronomy",
            "astrophotography",
            "binoculars",
            "star atlas",
            "planetarium",
            "space memorabilia",
            "nasa",
            "cat",
            "dog",
            "aquarium",
            "reptile",
            "bird cage",
            "horse tack",
            "pet memorabilia",
            "taxidermy fish",
            "home brewing",
            "beer brewing",
            "wine making",
            "whiskey decanter",
            "vintage beer",
            "barware",
            "cocktail shaker",
            "vintage wine",
            "distilling equipment",
            "bicycle",
            "road bike",
            "mountain bike",
            "bmx bike",
            "vintage bicycle",
            "cycling equipment",
            "gardening",
            "bonsai",
            "vintage garden",
            "bird feeder",
            "insect collection",
            "fossil",
            "mineral specimen",
            "crystal",
            "meteorite",
            "cast iron cookware",
            "vintage kitchen",
            "copper cookware",
            "vintage cookbook",
            "espresso machine",
            "coffee grinder",
            "woodworking",
            "hand planes",
            "vintage drill press",
            "lathe",
            "vintage saw",
            "vintage sunglasses",
            "vintage belt",
            "vintage boots",
            "vintage scarf",
            "vintage wallet",
            "luxury handbag",
            "vintage luggage",
            "vintage globe",
            "vintage map",
            "travel memorabilia",
            "vintage compass",
            "first edition book",
            "vintage magazine",
            "signed book",
            "pulp fiction magazine",
            "vintage newspaper",
            "vintage puzzle",
            "chess set",
            "vintage card game",
            "tabletop rpg",
            "dungeons and dragons",
            "vintage car parts",
            "porsche parts",
            "mustang parts",
            "corvette parts",
            "vintage hood ornament",
            "gas station memorabilia",
            "vintage license plate",
            "vintage microscope",
            "vintage chemistry set",
            "vintage slide rule",
            "vintage globe",
            "vintage anatomy",
            "vintage religious",
            "tibetan singing bowl",
            "vintage tarot",
            "native american pottery",
            "african art",
            "japanese woodblock print",
        )
    }

    /** HTTP client shared across calls; auto-follows 30x and keeps cookies (eBay sets consent + session). */
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(HTTP_CONNECT_TIMEOUT)
        .cookieHandler(CookieManager())
        .build()

    fun getTodayItems(): List<DailyItem> {
        return getItemsForDate(LocalDate.now())
    }

    fun getItemsForDate(date: LocalDate): List<DailyItem> {
        val items = dailyItemRepository.findByGameDate(date)
        if (items.isEmpty()) {
            throw NoItemsAvailableException()
        }
        return items
    }

    fun verifyGuess(itemId: Long, guess: Double): VerifyResponse {
        val item = dailyItemRepository.findById(itemId).orElseThrow {
            ItemNotFoundException()
        }

        val percentageOff = (abs(guess - item.soldPrice) / item.soldPrice) * 100.0
        val roundedPercentageOff = Math.round(percentageOff * 100.0) / 100.0
        if (Math.abs(guess - item.soldPrice) <= 1.0) {
            return VerifyResponse(itemId = item.id, guess = guess, actualPrice = item.soldPrice, percentageOff = roundedPercentageOff, score = 100)
        }
        val ratio = if (guess > 0) maxOf(guess / item.soldPrice, item.soldPrice / guess) else Double.MAX_VALUE
        // Penalties tuned so large multiplicative misses (e.g. $35 vs ~$5.60) score much lower than ~50.
        val logPenalty = 16.0 * Math.log(ratio)
        val scalePenalty = 13.0 * Math.abs(guess - item.soldPrice) / (10.0 + 0.15 * item.soldPrice)
        val rawScore = 100.0 - logPenalty - scalePenalty
        // No "95+ rounds to 100" bump — close misses (e.g. $155 vs $165) should stay in the mid‑90s, not perfect.
        val score = rawScore.toInt().coerceIn(0, 100)

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
        if (existing.size >= ITEMS_PER_DAY) {
            Logger.info("Items for {} already curated, skipping", targetDate)
            return
        }

        val usedIds = existing.map { it.ebayItemId }.toMutableSet()
        val curatedItems = existing.toMutableList()
        val shuffledCategories = SEARCH_CATEGORIES.shuffled().toMutableList()

        while (curatedItems.size < ITEMS_PER_DAY && shuffledCategories.isNotEmpty()) {
            val keyword = shuffledCategories.removeFirst()
            val pageNumber = (1..MAX_SERP_PAGES).random()
            Logger.info(
                "Scraping '{}' page {}/{} ({}/{})",
                keyword,
                pageNumber,
                MAX_SERP_PAGES,
                curatedItems.size + 1,
                ITEMS_PER_DAY,
            )
            val pageItems = scrapeCompletedAuctions(keyword, pageNumber)
            val usable = pageItems.filter { it.ebayItemId !in usedIds }
            if (usable.isEmpty()) {
                if (pageItems.isNotEmpty()) {
                    Logger.warn(
                        "Keyword '{}' page {} had {} eligible items but all were already used",
                        keyword,
                        pageNumber,
                        pageItems.size,
                    )
                } else {
                    Logger.warn("No eligible SERP rows for keyword '{}' on page {}", keyword, pageNumber)
                }
                continue
            }
            Logger.info("{} eligible items on page {} for '{}' — picking one at random", usable.size, pageNumber, keyword)

            val candidate = usable.random()
            candidate.gameDate = targetDate
            curatedItems.add(candidate)
            usedIds.add(candidate.ebayItemId)
            Logger.info(
                "Selected item: {} at {} ({} bids)",
                candidate.title,
                candidate.soldPrice,
                candidate.bidCount,
            )
        }

        val newItems = curatedItems.filter { it.id == 0L }
        if (newItems.isNotEmpty()) {
            dailyItemRepository.saveAll(newItems)
            Logger.info("Saved {} items for {}", newItems.size, targetDate)
        }
        if (curatedItems.size < ITEMS_PER_DAY) {
            Logger.warn("Only curated {}/{} items for {}", curatedItems.size, ITEMS_PER_DAY, targetDate)
        }
    }

    /** Fetch SERP HTML over plain HTTP (no headless Chrome, no hangs, no crashes). */
    private fun fetchHtml(url: String, userAgent: String, referer: String?): String? {
        val builder = HttpRequest.newBuilder(URI.create(url))
            .timeout(HTTP_REQUEST_TIMEOUT)
            .GET()
            .header("User-Agent", userAgent)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.9")
            .header("Accept-Encoding", "gzip, deflate")
            .header("Upgrade-Insecure-Requests", "1")
            .header("Cache-Control", "no-cache")
            .header("Pragma", "no-cache")
            .header("DNT", "1")
        if (referer != null) builder.header("Referer", referer)

        return try {
            val response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray())
            if (response.statusCode() !in 200..299) {
                Logger.warn("HTTP {} fetching {}", response.statusCode(), url)
                return null
            }
            val encoding = response.headers().firstValue("Content-Encoding").orElse("").lowercase()
            val raw = response.body()
            val bytes = when (encoding) {
                "gzip" -> java.util.zip.GZIPInputStream(raw.inputStream()).use { it.readBytes() }
                "deflate" -> java.util.zip.InflaterInputStream(raw.inputStream()).use { it.readBytes() }
                else -> raw
            }
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Logger.warn("HTTP fetch failed for {}: {}", url, e.message)
            null
        }
    }

    private fun scrapeCompletedAuctions(keyword: String, pageNumber: Int): List<DailyItem> {
        val encoded = URLEncoder.encode(keyword, "UTF-8")

        // Try desktop first — it's the richest markup. Fall back to mobile if eBay returns a shell/bot page.
        val desktopUrl =
            "https://www.ebay.com/sch/i.html?_nkw=$encoded&LH_Complete=1&LH_Sold=1&LH_Auction=1&_pgn=$pageNumber&_ipg=60&rt=nc"
        val mobileUrl =
            "https://www.ebay.com/sch/i.html?_nkw=$encoded&LH_Complete=1&LH_Sold=1&LH_Auction=1&_pgn=$pageNumber&_ipg=60&_sop=13"

        var html = fetchHtml(desktopUrl, DESKTOP_UA, referer = "https://www.ebay.com/")
        var rows = html?.let { parseSerpRows(it) }.orEmpty()

        if (rows.isEmpty()) {
            Logger.info(
                "Desktop SERP empty for '{}' page {} — retrying as mobile",
                keyword,
                pageNumber,
            )
            html = fetchHtml(mobileUrl, MOBILE_UA, referer = "https://m.ebay.com/")
            rows = html?.let { parseSerpRows(it) }.orEmpty()
        }

        if (rows.isEmpty()) {
            Logger.warn(
                "SERP returned no list rows for '{}' page {} (layout change, bot/captcha, or empty results)",
                keyword,
                pageNumber,
            )
            return emptyList()
        }

        val results = mutableListOf<DailyItem>()
        val seenItemIds = mutableSetOf<String>()

        for (el in rows) {
            try {
                val title = extractSerpTitle(el)
                if (title.isBlank() || title == "Shop on eBay") continue

                val href = extractSerpItemHref(el) ?: continue
                val itemId = Regex("/itm/(\\d+)").find(href)?.groupValues?.get(1) ?: continue
                if (itemId in seenItemIds) continue

                val priceText = el.selectFirst(
                    ".s-card__price, .s-item__price, span[class*='s-card__price'], span[class*='s-item__price']",
                )?.text() ?: continue
                val soldPrice = parsePrice(priceText) ?: continue
                if (soldPrice <= 0.0) continue

                val imageUrl = firstImageUrlFromImg(el.selectFirst("img")) ?: continue

                val cardBidCount = BID_COUNT_REGEX.find(el.text())?.groupValues?.get(1)?.toIntOrNull()
                    ?: continue
                if (cardBidCount < MIN_BID_COUNT) continue

                seenItemIds.add(itemId)
                val entity = DailyItem()
                entity.ebayItemId = itemId
                entity.title = title
                entity.imageUrl = imageUrl
                entity.soldPrice = soldPrice
                entity.bidCount = cardBidCount
                entity.itemUrl = "https://www.ebay.com/itm/$itemId?orig_cvip=true"
                entity.saleDate = extractSerpSaleDate(el)
                results.add(entity)
            } catch (_: Exception) {
                continue
            }
        }

        if (results.isEmpty()) {
            Logger.warn(
                "SERP had {} raw rows for '{}' page {} but none passed filters (price/title/link/image/bid)",
                rows.size,
                keyword,
                pageNumber,
            )
        }
        Logger.info("Found {} eligible items for keyword '{}' page {}", results.size, keyword, pageNumber)
        return results
    }

    private fun parseSerpRows(html: String): List<org.jsoup.nodes.Element> {
        return collectSerpListingElements(Jsoup.parse(html))
    }

    private fun collectSerpListingElements(doc: org.jsoup.nodes.Document): List<org.jsoup.nodes.Element> {
        val seen = java.util.IdentityHashMap<org.jsoup.nodes.Element, Boolean>()
        val out = mutableListOf<org.jsoup.nodes.Element>()
        for (sel in SERP_CARD_SELECTORS) {
            for (el in doc.select(sel)) {
                if (el.selectFirst("a[href*='/itm/']") == null) continue
                if (seen.put(el, true) != null) continue
                out.add(el)
            }
        }
        return out
    }

    private fun extractSerpTitle(el: org.jsoup.nodes.Element): String {
        val raw = el.selectFirst(
            ".s-card__title, .s-item__title, [class*='s-card__title'], [class*='s-item__title'], div[role=heading].s-card__title, h3.s-card__title",
        )?.text()
            ?: el.selectFirst("a[href*='/itm/']")?.attr("aria-label")?.trim()
            ?: ""
        return raw.replace("Opens in a new window or tab", "", ignoreCase = true).trim()
    }

    private fun extractSerpSaleDate(el: org.jsoup.nodes.Element): LocalDate? {
        val raw = el.selectFirst(
            ".s-item__ended-date, .s-item__caption--signal.POSITIVE, span.POSITIVE, .s-item__caption--signal",
        )?.text() ?: return null
        val cleaned = raw.replace("Sold", "", ignoreCase = true).trim()
        for (fmt in SALE_DATE_FORMATS) {
            try {
                return LocalDate.parse(cleaned, fmt)
            } catch (_: DateTimeParseException) {
                continue
            }
        }
        return null
    }

    private fun extractSerpItemHref(el: org.jsoup.nodes.Element): String? {
        val a = el.selectFirst("a.s-card__link, a.s-item__link, a[href*='/itm/']") ?: return null
        var href = a.attr("href").trim()
        if (href.isEmpty()) return null
        if (href.startsWith("/itm/")) href = "https://www.ebay.com$href"
        if (!href.contains("/itm/")) return null
        return href
    }

    private fun normalizeMediaUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val t = raw.trim()
        return when {
            t.startsWith("http://", ignoreCase = true) || t.startsWith("https://", ignoreCase = true) -> t
            t.startsWith("//") -> "https:$t"
            t.startsWith("/images/") -> "https://i.ebayimg.com$t"
            else -> null
        }
    }

    /** eBay CDN paths use `/s-lNNN` for width; SERP often uses 140–500px — we bump after picking the best candidate. */
    private fun ebayImagePathPixelHint(url: String): Int =
        Regex("""/s-l(\d+)""", RegexOption.IGNORE_CASE).find(url)?.groupValues?.get(1)?.toIntOrNull() ?: 0

    /** Request gallery-sized image when the URL is on eBay's image CDN (same image id, larger file). */
    private fun upgradeEbayImageUrl(url: String): String {
        if (!url.contains("ebayimg", ignoreCase = true)) return url
        return url.replace(Regex("""/s-l\d+""", RegexOption.IGNORE_CASE), "/s-l1600")
    }

    private fun firstImageUrlFromImg(imgEl: org.jsoup.nodes.Element?): String? {
        if (imgEl == null) return null
        var bestUrl: String? = null
        var bestScore = -1

        fun consider(raw: String?) {
            val u = normalizeMediaUrl(raw) ?: return
            val s = ebayImagePathPixelHint(u)
            if (s > bestScore) {
                bestScore = s
                bestUrl = u
            }
        }

        // High-res hints first, then lazy-load / fallback src.
        consider(imgEl.attr("data-zoom-src"))
        consider(imgEl.attr("data-original"))
        consider(imgEl.attr("data-src"))
        consider(imgEl.attr("src"))

        val srcset = imgEl.attr("srcset").trim()
        if (srcset.isNotEmpty()) {
            for (segment in srcset.split(',')) {
                val t = segment.trim()
                if (t.isEmpty()) continue
                val urlToken = t.substringBefore(' ').trim()
                val rest = t.removePrefix(urlToken).trim()
                val widthFromDescriptor = when {
                    rest.endsWith("w", ignoreCase = true) ->
                        rest.dropLast(1).trim().toIntOrNull() ?: 0
                    else -> 0
                }
                val u = normalizeMediaUrl(urlToken) ?: continue
                val s = maxOf(widthFromDescriptor, ebayImagePathPixelHint(u))
                if (s > bestScore) {
                    bestScore = s
                    bestUrl = u
                }
            }
        }

        val picked = bestUrl ?: return null
        return upgradeEbayImageUrl(picked)
    }

    private fun parsePrice(text: String): Double? {
        if (text.contains(" to ")) return null
        val cleaned = text.replace(Regex("[^0-9.]"), "")
        return cleaned.toDoubleOrNull()
    }
}
