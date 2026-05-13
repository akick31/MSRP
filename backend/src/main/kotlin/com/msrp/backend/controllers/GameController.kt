package com.msrp.backend.controllers

import com.msrp.backend.model.dto.VerifyRequest
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
@RequestMapping("\${api.base-path}")
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

    @PostMapping("/admin/images/{itemId}", consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun storeImage(
        @PathVariable itemId: String,
        @RequestBody bytes: ByteArray,
    ): ResponseEntity<*> = gameService.storeImage(itemId, bytes)

    @PostMapping("/admin/curate")
    fun triggerCuration(
        @RequestParam(required = false) date: String?,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
    ): ResponseEntity<*> = gameService.triggerCuration(date, startDate, endDate)
}
