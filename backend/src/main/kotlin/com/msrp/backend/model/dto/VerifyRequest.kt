package com.msrp.backend.model.dto

data class VerifyRequest(
    val itemId: Long,
    val guess: Double,
)
