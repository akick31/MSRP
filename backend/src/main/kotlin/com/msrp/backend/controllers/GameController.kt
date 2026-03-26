package com.msrp.backend.controllers

import com.msrp.backend.dto.DailyItemResponse
import com.msrp.backend.dto.VerifyRequest
import com.msrp.backend.dto.VerifyResponse
import com.msrp.backend.service.ebay.EbayService
import com.msrp.backend.util.DTOConverter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping(ApiConstants.FULL_PATH)
class GameController(
    private val ebayService: EbayService,
    private val dtoConverter: DTOConverter,
) {

    @GetMapping("/today")
    fun getTodayItems(): ResponseEntity<List<DailyItemResponse>> {
        val items = ebayService.getTodayItems()
        val response = items.map { dtoConverter.convertToDailyItemResponse(it) }
        return ResponseEntity.ok(response)
    }

    @PostMapping("/verify")
    fun verifyGuess(
        @RequestBody request: VerifyRequest,
    ): ResponseEntity<VerifyResponse> {
        val response = ebayService.verifyGuess(request.itemId, request.guess)
        return ResponseEntity.ok(response)
    }
}
