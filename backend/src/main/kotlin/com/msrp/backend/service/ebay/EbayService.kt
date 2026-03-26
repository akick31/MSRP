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
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate
import java.util.Base64
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@Service
class EbayService(
    private val webClient: WebClient,
    private val dailyItemRepository: DailyItemRepository,
    @Value("\${ebay.client-id}") private val clientId: String,
    @Value("\${ebay.client-secret}") private val clientSecret: String,
    @Value("\${ebay.api-base-url}") private val apiBaseUrl: String,
    @Value("\${ebay.auth-url}") private val authUrl: String,
) {

    companion object {
        private const val ITEMS_PER_DAY = 5

        private val PRICE_BRACKETS = listOf(
            10.0 to 50.0,
            50.0 to 150.0,
            150.0 to 500.0,
            500.0 to 2000.0,
            2000.0 to 10000.0,
        )

        private val SEARCH_KEYWORDS = listOf(
            "vintage",
            "collectible",
            "rare",
            "antique",
            "limited edition",
            "signed",
            "sealed",
            "electronics",
            "sneakers",
            "trading cards",
        )
    }

    private fun authenticate(): String {
        val credentials = Base64.getEncoder()
            .encodeToString("$clientId:$clientSecret".toByteArray())

        val response = webClient.post()
            .uri(authUrl)
            .header("Authorization", "Basic $credentials")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("grant_type", "client_credentials")
                    .with("scope", "https://api.ebay.com/oauth/api_scope"),
            )
            .retrieve()
            .bodyToMono(JsonNode::class.java)
            .block() ?: throw RuntimeException("Failed to authenticate with eBay API")

        return response.get("access_token").asText()
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

    fun curateDailyItems() {
        val tomorrow = LocalDate.now().plusDays(1)

        val existing = dailyItemRepository.findByGameDate(tomorrow)
        if (existing.size >= ITEMS_PER_DAY) {
            Logger.info("Items for {} already curated, skipping", tomorrow)
            return
        }

        val token = authenticate()
        val curatedItems = mutableListOf<DailyItem>()

        for ((index, bracket) in PRICE_BRACKETS.withIndex()) {
            if (curatedItems.size >= ITEMS_PER_DAY) break

            val keyword = SEARCH_KEYWORDS[index % SEARCH_KEYWORDS.size]
            val item = searchAndSelectItem(token, keyword, bracket.first, bracket.second)

            if (item != null) {
                item.gameDate = tomorrow
                curatedItems.add(item)
                Logger.info("Selected item: {} at ${}", item.title, item.soldPrice)
            } else {
                Logger.warn("No suitable item found for bracket ${}-${}", bracket.first, bracket.second)
            }
        }

        if (curatedItems.isNotEmpty()) {
            dailyItemRepository.saveAll(curatedItems)
            Logger.info("Saved {} items for {}", curatedItems.size, tomorrow)
        } else {
            Logger.error("Failed to curate any items for {}", tomorrow)
        }
    }

    private fun searchAndSelectItem(
        token: String,
        keyword: String,
        minPrice: Double,
        maxPrice: Double,
    ): DailyItem? {
        val filterString = buildFilterString(minPrice, maxPrice)

        val response = webClient.get()
            .uri("$apiBaseUrl/buy/browse/v1/item_summary/search?q=$keyword&filter=$filterString&sort=-price&limit=50")
            .header("Authorization", "Bearer $token")
            .header("X-EBAY-C-MARKETPLACE-ID", "EBAY_US")
            .retrieve()
            .bodyToMono(JsonNode::class.java)
            .block()

        val itemSummaries = response?.get("itemSummaries") ?: return null

        val eligibleItems = itemSummaries
            .filter { node ->
                val bidCount = node.get("bidCount")?.asInt() ?: 0
                bidCount > 15
            }
            .toList()

        if (eligibleItems.isEmpty()) return null

        val selected = eligibleItems.random()
        return mapToEntity(selected)
    }

    private fun buildFilterString(minPrice: Double, maxPrice: Double): String {
        return listOf(
            "buyingOptions:{AUCTION}",
            "conditions:{NEW|USED}",
            "price:[${minPrice}..${maxPrice}]",
            "priceCurrency:USD",
            "soldItemsOnly:true",
        ).joinToString(",")
    }

    private fun mapToEntity(item: JsonNode): DailyItem {
        val entity = DailyItem()
        entity.ebayItemId = item.get("itemId")?.asText() ?: ""
        entity.title = item.get("title")?.asText() ?: "Unknown Item"
        entity.imageUrl = item.get("image")?.get("imageUrl")?.asText()
            ?: item.get("thumbnailImages")?.firstOrNull()?.get("imageUrl")?.asText()
            ?: ""
        entity.soldPrice = item.get("price")?.get("value")?.asDouble() ?: 0.0
        entity.bidCount = item.get("bidCount")?.asInt() ?: 0
        return entity
    }
}
