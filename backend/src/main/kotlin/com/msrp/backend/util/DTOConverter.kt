package com.msrp.backend.util

import com.msrp.backend.dto.DailyItemResponse
import com.msrp.backend.model.DailyItem
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
class DTOConverter {

    fun convertToDailyItemResponse(item: DailyItem): DailyItemResponse {
        return DailyItemResponse(
            id = item.id,
            ebayItemId = item.ebayItemId,
            gameDate = item.gameDate.toString(),
            title = item.title,
            imageUrl = item.imageUrl,
            bidCount = item.bidCount,
            itemUrl = item.itemUrl,
            priceChoices = generatePriceChoices(item.soldPrice),
        )
    }

    private fun generatePriceChoices(actualPrice: Double): List<Double> {
        val decoys = mutableListOf<Double>()
        val multipliers = listOf(0.25, 0.5, 2.5)
        for (m in multipliers) {
            var decoy = roundToNicePrice(actualPrice * m)
            // Avoid duplicates with actual price or each other
            if (decoy == actualPrice || decoys.contains(decoy)) {
                decoy = roundToNicePrice(actualPrice * m * 1.1)
            }
            decoys.add(decoy)
        }
        return (decoys + actualPrice).shuffled()
    }

    private fun roundToNicePrice(price: Double): Double {
        return when {
            price < 5 -> (price * 4).roundToInt() / 4.0
            price < 20 -> (price * 2).roundToInt() / 2.0
            price < 100 -> price.roundToInt().toDouble()
            price < 500 -> (price / 5).roundToInt() * 5.0
            price < 2000 -> (price / 25).roundToInt() * 25.0
            else -> (price / 100).roundToInt() * 100.0
        }
    }
}
