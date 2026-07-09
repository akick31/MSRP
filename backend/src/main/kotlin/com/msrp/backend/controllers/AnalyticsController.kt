package com.msrp.backend.controllers

import com.msrp.backend.model.dto.AnalyticsRequest
import com.msrp.backend.model.dto.SubmitScoreRequest
import com.msrp.backend.services.AnalyticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("\${api.base-path}")
class AnalyticsController(private val analyticsService: AnalyticsService) {
    @PostMapping("/analytics")
    fun recordAnalytics(
        @RequestBody request: AnalyticsRequest,
    ): ResponseEntity<Any> = analyticsService.recordFromRequest(request)

    @PostMapping("/score")
    fun submitScore(
        @RequestBody request: SubmitScoreRequest,
    ): ResponseEntity<Any> = analyticsService.submitScore(request)

    @GetMapping("/game-stats")
    fun getGameStats(
        @RequestParam date: String,
    ): ResponseEntity<Any> = analyticsService.getGameStats(date)
}
