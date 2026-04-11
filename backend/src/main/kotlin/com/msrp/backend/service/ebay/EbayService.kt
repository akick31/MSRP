package com.msrp.backend.service.ebay

import com.fasterxml.jackson.databind.JsonNode
import com.msrp.backend.dto.VerifyResponse
import com.msrp.backend.model.DailyItem
import com.msrp.backend.repositories.DailyItemRepository
import com.msrp.backend.util.ItemNotFoundException
import com.msrp.backend.util.ItemNotFromTodayException
import com.msrp.backend.util.Logger
import com.msrp.backend.util.NoItemsAvailableException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.net.URI
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@Service
class EbayService(
    private val webClient: WebClient,
    private val dailyItemRepository: DailyItemRepository,
    @Value("\${ebay.client-id}") private val clientId: String,
    @Value("\${ebay.finding-api-url}") private val findingApiUrl: String,
) {

    companion object {
        private const val ITEMS_PER_DAY = 5
        private const val MIN_BID_COUNT = 5
        private const val MAX_ATTEMPTS = 3

        private val SEARCH_TERMS = listOf(
            // Sports
            "baseball", "basketball", "football", "soccer", "hockey", "golf", "tennis",
            "boxing", "wrestling", "cycling", "volleyball", "softball", "lacrosse",
            "rugby", "cricket", "archery", "bowling", "fishing", "hunting", "skiing",
            "snowboarding", "surfing", "skateboarding", "gymnastics", "weightlifting",
            "martial arts", "badminton", "rowing", "kayaking", "climbing", "olympics",
            // Outdoors & camping
            "camping", "hiking", "fishing gear", "hunting gear", "archery gear",
            "binoculars", "kayak", "canoe", "survival gear",
            // Hobbies
            "model train", "model airplane", "model ship", "remote control car",
            "pinball machine", "arcade machine", "jukebox", "board game", "chess",
            "telescope", "microscope", "stamps", "coins", "postcards",
            "sewing machine", "pottery", "woodworking", "wine", "whiskey",
            // Video games
            "video game", "video game console", "retro game", "game cartridge",
            "nintendo", "playstation", "xbox", "sega", "atari", "gameboy",
            // Musical instruments
            "guitar", "bass guitar", "electric guitar", "acoustic guitar",
            "amplifier", "drum kit", "trumpet", "saxophone", "violin",
            "synthesizer", "keyboard", "mandolin", "banjo", "ukulele",
            "harmonica", "accordion", "cello", "flute", "clarinet",
            // Trading cards & collectibles
            "pokemon cards", "trading cards", "sports cards", "magic the gathering",
            "yugioh cards", "comic book", "graded card",
            // Electronics
            "laptop", "camera", "lens", "turntable", "vintage stereo",
            "headphones", "drone", "smartwatch", "vintage computer",
            "vintage radio", "vintage television",
            // Sneakers & clothing
            "sneakers", "vintage clothing", "vintage jacket", "vintage jeans",
            "vintage shoes", "vintage hat", "streetwear",
            // Jewelry & watches
            "watch", "gold ring", "silver jewelry", "diamond jewelry",
            "vintage jewelry", "necklace", "bracelet", "vintage watch",
            // Toys
            "lego", "hot wheels", "action figure", "star wars toy",
            "vintage toy", "diecast car", "antique doll", "toy train",
            // Art & memorabilia
            "signed memorabilia", "autograph", "movie poster", "concert poster",
            "original painting", "sculpture", "vintage poster", "sports memorabilia",
            // Coins & currency
            "silver coin", "gold coin", "vintage coin", "commemorative coin",
            "currency collection", "ancient coin",
            // Antiques & collectibles
            "antique furniture", "vintage pottery", "vintage glass", "vintage lamp",
            "antique clock", "vintage toy", "vintage sign", "antique tool",
            // Music & media
            "vinyl record", "sealed record", "vintage album",
            // Cameras
            "film camera", "vintage camera", "camera lens", "darkroom equipment",
        )
    }

    fun getTodayItems(): List<DailyItem> {
        val items = dailyItemRepository.findByGameDate(LocalDate.now())
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

    fun curateDailyItems(forToday: Boolean = false) {
        val targetDate = if (forToday) LocalDate.now() else LocalDate.now().plusDays(1)

        val existing = dailyItemRepository.findByGameDate(targetDate)
        if (existing.size >= ITEMS_PER_DAY) {
            Logger.info("Items for {} already curated, skipping", targetDate)
            return
        }

        val usedIds = existing.map { it.ebayItemId }.toMutableSet()
        val curatedItems = existing.toMutableList()

        repeat(MAX_ATTEMPTS) { attempt ->
            if (curatedItems.size >= ITEMS_PER_DAY) return@repeat

            val keyword = SEARCH_TERMS.random()
            val pageNumber = (1..10).random()
            Logger.info("Attempt {}: searching '{}' page {}", attempt + 1, keyword, pageNumber)

            val candidates = searchCompletedAuctions(keyword, pageNumber)
            if (candidates.isEmpty()) {
                Logger.warn("No eligible items found for keyword '{}' page {}", keyword, pageNumber)
                return@repeat
            }

            val shuffled = candidates.shuffled()
            for (item in shuffled) {
                if (curatedItems.size >= ITEMS_PER_DAY) break
                if (item.ebayItemId in usedIds) continue
                item.gameDate = targetDate
                curatedItems.add(item)
                usedIds.add(item.ebayItemId)
                Logger.info("Selected item: {} at {}", item.title, item.soldPrice)
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

    private fun searchCompletedAuctions(keyword: String, pageNumber: Int): List<DailyItem> {
        val endedFrom = ZonedDateTime.now(ZoneOffset.UTC).minusDays(30)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))

        val uri = URI.create(
            "$findingApiUrl" +
                "?OPERATION-NAME=findCompletedItems" +
                "&SERVICE-VERSION=1.0.0" +
                "&SECURITY-APPNAME=$clientId" +
                "&RESPONSE-DATA-FORMAT=JSON" +
                "&REST-PAYLOAD" +
                "&keywords=${keyword.replace(" ", "%20")}" +
                "&itemFilter(0).name=SoldItemsOnly&itemFilter(0).value=true" +
                "&itemFilter(1).name=ListingType&itemFilter(1).value=Auction" +
                "&itemFilter(2).name=MinBidCount&itemFilter(2).value=$MIN_BID_COUNT" +
                "&itemFilter(3).name=EndTimeFrom&itemFilter(3).value=$endedFrom" +
                "&sortOrder=EndTimeSoonest" +
                "&paginationInput.entriesPerPage=100" +
                "&paginationInput.pageNumber=$pageNumber",
        )

        val response = try {
            webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(JsonNode::class.java)
                .block()
        } catch (e: WebClientResponseException) {
            val body = e.responseBodyAsString
            if (body.contains("10001")) {
                Logger.warn("Rate limited by eBay for keyword '{}', backing off 30s", keyword)
                Thread.sleep(30000L)
            } else {
                Logger.error("eBay Finding API error {} for keyword '{}': {}", e.statusCode, keyword, body)
            }
            return emptyList()
        } catch (e: Exception) {
            Logger.error("eBay Finding API request failed for keyword '{}': {}", keyword, e.message)
            return emptyList()
        }

        val items = response
            ?.get("findCompletedItemsResponse")
            ?.get(0)
            ?.get("searchResult")
            ?.get(0)
            ?.get("item")
            ?: return emptyList()

        return items.filter { node ->
            val sellingState = node.get("sellingStatus")?.get(0)?.get("sellingState")?.get(0)?.asText() ?: ""
            val soldPrice = node.get("sellingStatus")?.get(0)?.get("currentPrice")?.get(0)?.get("__value__")?.asDouble() ?: 0.0
            val hasImage = !node.get("galleryURL")?.get(0)?.asText().isNullOrBlank()
            sellingState == "EndedWithSales" && soldPrice > 0.0 && hasImage
        }.map { mapToEntity(it) }
    }

    private fun mapToEntity(item: JsonNode): DailyItem {
        val entity = DailyItem()
        entity.ebayItemId = item.get("itemId")?.get(0)?.asText() ?: ""
        entity.title = item.get("title")?.get(0)?.asText() ?: "Unknown Item"
        entity.imageUrl = item.get("galleryURL")?.get(0)?.asText() ?: ""
        entity.soldPrice = item.get("sellingStatus")?.get(0)
            ?.get("currentPrice")?.get(0)
            ?.get("__value__")?.asDouble() ?: 0.0
        entity.bidCount = item.get("listingInfo")?.get(0)
            ?.get("bidCount")?.get(0)?.asInt() ?: 0
        entity.itemUrl = item.get("viewItemURL")?.get(0)?.asText() ?: ""
        return entity
    }
}
