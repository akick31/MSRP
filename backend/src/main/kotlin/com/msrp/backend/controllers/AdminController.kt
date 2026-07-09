package com.msrp.backend.controllers

import com.msrp.backend.services.AnalyticsService
import com.msrp.backend.services.GameService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("\${api.base-path}/admin")
class AdminController(
    private val gameService: GameService,
    private val analyticsService: AnalyticsService,
) {
    @PostMapping("/images/{itemId}", consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun storeImage(
        @PathVariable itemId: String,
        @RequestBody bytes: ByteArray,
    ): ResponseEntity<*> = gameService.storeImage(itemId, bytes)

    @PostMapping("/curate")
    fun triggerCuration(
        @RequestParam(required = false) date: String?,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
    ): ResponseEntity<*> = gameService.triggerCuration(date, startDate, endDate)

    @GetMapping("/analytics")
    fun getAnalytics(
        @RequestParam(defaultValue = "30") days: Int,
    ): ResponseEntity<Any> = analyticsService.getSummary(days)
}
