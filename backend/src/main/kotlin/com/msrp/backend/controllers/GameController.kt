package com.msrp.backend.controllers

import com.msrp.backend.model.dto.ContactRequest
import com.msrp.backend.model.dto.DailyItemResponse
import com.msrp.backend.model.dto.VerifyRequest
import com.msrp.backend.model.dto.VerifyResponse
import com.msrp.backend.services.AnalyticsService
import com.msrp.backend.services.ContactService
import com.msrp.backend.services.GameService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiConstants.FULL_PATH)
class GameController(
    private val gameService: GameService,
    private val analyticsService: AnalyticsService,
    private val contactService: ContactService,
) {
    @GetMapping("/today")
    fun getTodayItems(
        @RequestParam(required = false) date: String?,
    ): ResponseEntity<List<DailyItemResponse>> = gameService.getItemsForDate(date)

    @PostMapping("/verify")
    fun verifyGuess(
        @RequestBody request: VerifyRequest,
    ): ResponseEntity<VerifyResponse> = gameService.verifyGuess(request)

    @GetMapping("/available-dates")
    fun getAvailableDates(): ResponseEntity<List<String>> = gameService.getAvailableDates()

    @PostMapping("/analytics")
    fun recordAnalytics(
        @RequestBody body: Map<String, String>,
    ): ResponseEntity<Any> = analyticsService.recordFromRequest(body)

    @PostMapping("/score")
    fun submitScore(
        @RequestBody body: Map<String, String>,
    ): ResponseEntity<Any> = analyticsService.submitScore(body)

    @PostMapping("/contact")
    fun contact(
        @RequestBody request: ContactRequest,
    ): ResponseEntity<Any> = contactService.send(request)

    @PostMapping("/admin/curate")
    fun triggerCuration(
        @RequestParam(required = false) date: String?,
    ): ResponseEntity<Map<String, String>> = gameService.triggerCuration(date)

    @GetMapping("/admin/analytics")
    fun getAnalytics(
        @RequestParam(defaultValue = "30") days: Int,
    ): ResponseEntity<Any> = analyticsService.getSummary(days)
}
