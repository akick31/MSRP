package com.msrp.backend.dto

data class DailyItemResponse(
    val id: Long,
    val ebayItemId: String,
    val gameDate: String,
    val title: String,
    val imageUrl: String,
    val bidCount: Int,
    val itemUrl: String,
    val priceChoices: List<Double>,
)
