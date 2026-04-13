package com.msrp.backend.controllers

import com.msrp.backend.dto.DailyItemResponse
import com.msrp.backend.dto.VerifyRequest
import com.msrp.backend.dto.VerifyResponse
import com.msrp.backend.service.ebay.EbayService
import com.msrp.backend.util.DTOConverter
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping(ApiConstants.FULL_PATH)
class GameController(
    private val ebayService: EbayService,
    private val dtoConverter: DTOConverter,
) {

    @GetMapping("/today")
    fun getTodayItems(
        @RequestParam(required = false) date: String?,
    ): ResponseEntity<List<DailyItemResponse>> {
        val gameDate = if (date != null) {
            try { LocalDate.parse(date) } catch (e: Exception) { LocalDate.now() }
        } else {
            LocalDate.now()
        }
        val items = ebayService.getItemsForDate(gameDate)
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

    @PostMapping("/admin/curate")
    fun triggerCuration(
        @RequestParam(required = false) date: String?,
    ): ResponseEntity<Map<String, String>> {
        return try {
            val targetDate = if (date != null) {
                try { LocalDate.parse(date) } catch (e: Exception) { LocalDate.now() }
            } else {
                LocalDate.now()
            }
            ebayService.curateDailyItems(targetDate)
            ResponseEntity.ok(mapOf("message" to "Curation triggered for $targetDate"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to (e.message ?: "Curation failed")))
        }
    }
}
