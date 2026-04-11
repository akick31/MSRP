package com.msrp.backend.dto

data class VerifyRequest(
    val itemId: Long,
    val guess: Double,
)
