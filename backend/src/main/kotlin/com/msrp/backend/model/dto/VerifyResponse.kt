package com.msrp.backend.model.dto

data class VerifyResponse(
    val itemId: Long,
    val guess: Double,
    val actualPrice: Double,
    val percentageOff: Double,
    val score: Int,
)
