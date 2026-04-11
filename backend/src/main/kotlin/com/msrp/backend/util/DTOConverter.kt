package com.msrp.backend.util

import com.msrp.backend.dto.DailyItemResponse
import com.msrp.backend.model.DailyItem
import org.springframework.stereotype.Component

@Component
class DTOConverter {

    fun convertToDailyItemResponse(item: DailyItem): DailyItemResponse {
        return DailyItemResponse(
            id = item.id,
            ebayItemId = item.ebayItemId,
            title = item.title,
            imageUrl = item.imageUrl,
            bidCount = item.bidCount,
        )
    }
}
