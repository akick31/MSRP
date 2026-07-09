package com.msrp.backend.controllers

import com.msrp.backend.model.dto.VerifyRequest
import com.msrp.backend.services.GameService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("\${api.base-path}/game")
class GameController(private val gameService: GameService) {
    @GetMapping("/today")
    fun getTodayItems(
        @RequestParam(required = false) date: String?,
    ): ResponseEntity<*> = gameService.getItemsForDate(date)

    @PostMapping("/verify")
    fun verifyGuess(
        @RequestBody request: VerifyRequest,
    ): ResponseEntity<*> = gameService.verifyGuess(request)

    @GetMapping("/available-dates")
    fun getAvailableDates(): ResponseEntity<*> = gameService.getAvailableDates()
}
