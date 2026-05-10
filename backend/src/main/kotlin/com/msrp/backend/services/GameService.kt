package com.msrp.backend.services

import com.msrp.backend.model.dto.DailyItemResponse
import com.msrp.backend.model.dto.VerifyRequest
import com.msrp.backend.model.dto.VerifyResponse
import com.msrp.backend.repositories.DailyItemRepository
import com.msrp.backend.services.ebay.EbayService
import com.msrp.backend.util.DTOConverter
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDate

private val EARLIEST_DATE: LocalDate = LocalDate.of(2025, 1, 1)

@Service
class GameService(
    private val ebayService: EbayService,
    private val dailyItemRepository: DailyItemRepository,
) {
    fun getItemsForDate(dateParam: String?): ResponseEntity<List<DailyItemResponse>> {
        return try {
            val date = parseDateParam(dateParam)
            val items = ebayService.getItemsForDate(date)
            ResponseEntity.ok(items.map { DTOConverter.convertToDailyItemResponse(it) })
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    fun verifyGuess(request: VerifyRequest): ResponseEntity<VerifyResponse> {
        if (request.guess <= 0) return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(ebayService.verifyGuess(request.itemId, request.guess))
    }

    fun getAvailableDates(): ResponseEntity<List<String>> {
        val dates =
            dailyItemRepository.findDistinctGameDatesBefore(LocalDate.now())
                .map { it.toString() }
        return ResponseEntity.ok(dates)
    }

    fun triggerCuration(dateParam: String?): ResponseEntity<Map<String, String>> {
        return try {
            val date = if (dateParam != null) LocalDate.parse(dateParam) else LocalDate.now().plusDays(1)
            ebayService.curateDailyItems(date)
            ResponseEntity.ok(mapOf("message" to "Curation triggered for $date"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to "Invalid date format"))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("error" to (e.message ?: "Curation failed")))
        }
    }

    private fun parseDateParam(dateParam: String?): LocalDate {
        if (dateParam == null) return LocalDate.now()
        val parsed =
            runCatching { LocalDate.parse(dateParam) }
                .getOrElse { throw IllegalArgumentException("Invalid date format: $dateParam") }
        require(parsed >= EARLIEST_DATE) { "Date is before earliest available game" }
        require(parsed <= LocalDate.now()) { "Date is in the future" }
        return parsed
    }
}
