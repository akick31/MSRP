package com.msrp.backend.service.ebay

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.msrp.backend.dto.VerifyResponse
import com.msrp.backend.model.DailyItem
import com.msrp.backend.repositories.DailyItemRepository
import com.msrp.backend.util.ItemNotFoundException
import com.msrp.backend.util.ItemNotFromTodayException
import com.msrp.backend.util.Logger
import com.msrp.backend.util.NoItemsAvailableException
import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@Service
class EbayService(
    private val dailyItemRepository: DailyItemRepository,
) {

    companion object {
        private const val ITEMS_PER_DAY = 5
        private const val MIN_BID_COUNT = 5
        private const val MAX_ITEM_PAGE_CHECKS = 5
        private val BID_COUNT_REGEX = Regex("(\\d+)\\s+bid", RegexOption.IGNORE_CASE)

        private val SEARCH_CATEGORIES = listOf(
            // Individual sports
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
            // Outdoor hobbies
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
            // Musical instruments
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
            // Collecting hobbies
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
            // Video games
            "video games",
            "retro video games",
            "nintendo",
            "playstation",
            "xbox",
            "sega",
            "atari",
            // Electronics & tech
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
            // Cameras & photography
            "film camera",
            "vintage camera",
            "camera lens",
            "darkroom equipment",
            // Jewelry & accessories
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
            // Home & antiques
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
            // Toys & games
            "board games",
            "pinball machines",
            "arcade machines",
            "jukeboxes",
            "remote control cars",
            "vintage toys",
            "vintage barbie",
            // Art & crafts
            "oil paintings",
            "sculpture",
            "native american art",
            "taxidermy",
            "vintage maps",
            // Misc hobbies
            "telescopes",
            "microscopes",
            "scientific instruments",
            "golf clubs",
            "bicycles",
            "motorcycle parts",
            "reading books first edition",
            "vintage medical equipment",
            // Major sporting events
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
            // Music & cultural events
            "coachella",
            "woodstock",
            "grateful dead",
            "rolling stones",
            "beatles",
            "elvis presley",
            "michael jackson",
            "concert tour",
            "grammy awards",
            // Cultural events & festivals
            "burning man",
            "oktoberfest",
            "mardi gras",
            "world expo",
            "world fair",
            // Other major events
            "presidential inauguration",
            "space shuttle",
            "apollo moon landing",
            "world war ii",
            "civil war",
            // Astronomy & space
            "telescope",
            "astronomy",
            "astrophotography",
            "binoculars",
            "star atlas",
            "planetarium",
            "space memorabilia",
            "nasa",
            // Pets & animals
            "cat",
            "dog",
            "aquarium",
            "reptile",
            "bird cage",
            "horse tack",
            "pet memorabilia",
            "taxidermy fish",
            // Brewing & spirits
            "home brewing",
            "beer brewing",
            "wine making",
            "whiskey decanter",
            "vintage beer",
            "barware",
            "cocktail shaker",
            "vintage wine",
            "distilling equipment",
            // Cycling & biking
            "bicycle",
            "road bike",
            "mountain bike",
            "bmx bike",
            "vintage bicycle",
            "cycling equipment",
            // Nature & gardening
            "gardening",
            "bonsai",
            "vintage garden",
            "bird feeder",
            "insect collection",
            "fossil",
            "mineral specimen",
            "crystal",
            "meteorite",
            // Cooking & food
            "cast iron cookware",
            "vintage kitchen",
            "copper cookware",
            "vintage cookbook",
            "espresso machine",
            "coffee grinder",
            // Woodworking & making
            "woodworking",
            "hand planes",
            "vintage drill press",
            "lathe",
            "vintage saw",
            // Fashion & style
            "vintage sunglasses",
            "vintage belt",
            "vintage boots",
            "vintage scarf",
            "vintage wallet",
            "luxury handbag",
            // Travel & adventure
            "vintage luggage",
            "vintage globe",
            "vintage map",
            "travel memorabilia",
            "vintage compass",
            // Reading & literature
            "first edition book",
            "vintage magazine",
            "signed book",
            "pulp fiction magazine",
            "vintage newspaper",
            // Gaming & puzzles
            "vintage puzzle",
            "chess set",
            "vintage card game",
            "tabletop rpg",
            "dungeons and dragons",
            // Cars & automotive
            "vintage car parts",
            "porsche parts",
            "mustang parts",
            "corvette parts",
            "vintage hood ornament",
            "gas station memorabilia",
            "vintage license plate",
            // Science & education
            "vintage microscope",
            "vintage chemistry set",
            "vintage slide rule",
            "vintage globe",
            "vintage anatomy",
            // Spirituality & culture
            "vintage religious",
            "tibetan singing bowl",
            "vintage tarot",
            "native american pottery",
            "african art",
            "japanese woodblock print",
        )
    }

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

        if (item.gameDate != LocalDate.now()) {
            throw ItemNotFromTodayException()
        }

        val percentageOff = (abs(guess - item.soldPrice) / item.soldPrice) * 100.0
        val roundedPercentageOff = Math.round(percentageOff * 100.0) / 100.0
        val score = max(0, (100 - percentageOff).roundToInt())

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

        Playwright.create().use { playwright ->
            playwright.chromium().launch(
                BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(listOf(
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-gpu",
                        "--disable-extensions",
                        "--disable-background-networking",
                        "--disable-sync",
                        "--no-first-run",
                        "--js-flags=--max-old-space-size=256",
                        "--renderer-process-limit=1",
                    ))
            ).use { browser ->
                while (curatedItems.size < ITEMS_PER_DAY && shuffledCategories.isNotEmpty()) {
                    val keyword = shuffledCategories.removeFirst()
                    val pageNumber = (1..3).random()
                    Logger.info("Scraping '{}' page {} ({}/{})", keyword, pageNumber, curatedItems.size + 1, ITEMS_PER_DAY)

                    val candidates = scrapeCompletedAuctions(browser, keyword, pageNumber)
                    if (candidates.isEmpty()) {
                        Logger.warn("No eligible items found for keyword '{}' page {}", keyword, pageNumber)
                        continue
                    }

                    var picked = false
                    var itemPageChecks = 0
                    for (candidate in candidates.shuffled()) {
                        if (candidate.ebayItemId in usedIds) continue
                        if (itemPageChecks >= MAX_ITEM_PAGE_CHECKS) break
                        itemPageChecks++
                        val bidCount = scrapeItemBidCount(browser, candidate.itemUrl)
                        if (bidCount < MIN_BID_COUNT) {
                            Logger.info("Skipping '{}' — only {} bids", candidate.title.take(50), bidCount)
                            continue
                        }
                        candidate.bidCount = bidCount
                        candidate.gameDate = targetDate
                        curatedItems.add(candidate)
                        usedIds.add(candidate.ebayItemId)
                        Logger.info("Selected item: {} at {} ({} bids)", candidate.title, candidate.soldPrice, bidCount)
                        picked = true
                        break
                    }
                    if (!picked) {
                        Logger.warn("No item with >= {} bids found for '{}'", MIN_BID_COUNT, keyword)
                    }
                }
            }
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

    private fun scrapeCompletedAuctions(browser: com.microsoft.playwright.Browser, keyword: String, pageNumber: Int): List<DailyItem> {
        val encoded = URLEncoder.encode(keyword, "UTF-8")
        val url = "https://www.ebay.com/sch/i.html?_nkw=$encoded&LH_Complete=1&LH_Sold=1&LH_Auction=1&_pgn=$pageNumber&_ipg=60"

        val html = try {
            val page = browser.newPage()
            page.use {
                it.navigate(url)
                it.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE)
                val content = it.content()
                content
            }
        } catch (e: Exception) {
            Logger.error("Playwright failed for keyword '{}': {}", keyword, e.message)
            return emptyList()
        }

        val doc = Jsoup.parse(html)
        val allItems = doc.select("ul.srp-results li, li.s-card, li.s-item")

        val results = mutableListOf<DailyItem>()

        for (el in allItems) {
            try {
                val title = (el.selectFirst(".s-card__title, .s-item__title")?.text() ?: "")
                    .replace("Opens in a new window or tab", "")
                    .trim()
                if (title.isBlank() || title == "Shop on eBay") continue

                val itemUrl = el.selectFirst("a.s-card__link, a.s-item__link")?.attr("href") ?: continue
                val itemId = Regex("/itm/(\\d+)").find(itemUrl)?.groupValues?.get(1) ?: continue

                val priceText = el.selectFirst(".s-card__price, .s-item__price")?.text() ?: continue
                val soldPrice = parsePrice(priceText) ?: continue
                if (soldPrice <= 0.0) continue

                val imgEl = el.selectFirst("img")
                val imageUrl = imgEl?.attr("src")?.takeIf { it.startsWith("http") }
                    ?: imgEl?.attr("data-src")?.takeIf { it.startsWith("http") }
                    ?: continue

                // Pre-filter: if bid count is visible in the card and below threshold, skip without loading item page
                val cardBidCount = BID_COUNT_REGEX.find(el.text())?.groupValues?.get(1)?.toIntOrNull()
                if (cardBidCount != null && cardBidCount < MIN_BID_COUNT) continue

                val entity = DailyItem()
                entity.ebayItemId = itemId
                entity.title = title
                entity.imageUrl = imageUrl
                entity.soldPrice = soldPrice
                entity.bidCount = cardBidCount ?: 0
                entity.itemUrl = "https://www.ebay.com/itm/$itemId?orig_cvip=true"
                results.add(entity)
            } catch (e: Exception) {
                continue
            }
        }

        Logger.info("Found {} eligible items for keyword '{}'", results.size, keyword)
        return results
    }

    private fun scrapeItemBidCount(browser: com.microsoft.playwright.Browser, itemUrl: String): Int {
        return try {
            val page = browser.newPage()
            page.use {
                it.navigate(itemUrl)
                it.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE)
                val html = it.content()
                val doc = Jsoup.parse(html)

                val bidText = doc.selectFirst(
                    ".ux-labels-values--bids .ux-labels-values__values-content, " +
                    "[data-testid='x-bid-count'], " +
                    ".vi-bidBox-bid-cnt, " +
                    "#w1-16 .ux-textspans"
                )?.text() ?: ""
                Regex("(\\d+)").find(bidText)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            }
        } catch (e: Exception) {
            Logger.warn("Failed to scrape bid count for {}: {}", itemUrl, e.message)
            0
        }
    }

    private fun parsePrice(text: String): Double? {
        if (text.contains(" to ")) return null
        val cleaned = text.replace(Regex("[^0-9.]"), "")
        return cleaned.toDoubleOrNull()
    }
}
