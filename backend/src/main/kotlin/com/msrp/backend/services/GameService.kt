package com.msrp.backend.services

import com.msrp.backend.model.dto.DailyItemResponse
import com.msrp.backend.model.dto.VerifyRequest
import com.msrp.backend.model.dto.VerifyResponse
import com.msrp.backend.repositories.DailyItemRepository
import com.msrp.backend.services.ebay.EbayService
import com.msrp.backend.util.DTOConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId

private val EARLIEST_DATE: LocalDate = LocalDate.of(2025, 1, 1)
private val EST: ZoneId = ZoneId.of("America/New_York")

@Service
class GameService(
    private val ebayService: EbayService,
    private val dailyItemRepository: DailyItemRepository,
    @Qualifier("curationExecutor") private val curationExecutor: ThreadPoolTaskExecutor,
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
            dailyItemRepository.findDistinctGameDatesBefore(LocalDate.now(EST))
                .map { it.toString() }
        return ResponseEntity.ok(dates)
    }

    fun triggerCuration(
        dateParam: String?,
        startDateParam: String?,
        endDateParam: String?,
    ): ResponseEntity<Map<String, String>> {
        return try {
            if (startDateParam != null && endDateParam != null) {
                val startDate = LocalDate.parse(startDateParam)
                val endDate = LocalDate.parse(endDateParam)
                require(!endDate.isBefore(startDate)) { "endDate must not be before startDate" }
                curationExecutor.execute { ebayService.curateDateRange(startDate, endDate) }
                ResponseEntity.accepted().body(mapOf("message" to "Bulk curation started for $startDate through $endDate"))
            } else {
                val date = if (dateParam != null) LocalDate.parse(dateParam) else LocalDate.now(EST).plusDays(1)
                ebayService.curateDailyItems(date)
                ResponseEntity.ok(mapOf("message" to "Curation triggered for $date"))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid request")))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("error" to (e.message ?: "Curation failed")))
        }
    }

    fun storeImage(
        itemId: String,
        bytes: ByteArray,
    ): ResponseEntity<Map<String, String>> =
        try {
            ebayService.storeImage(itemId, bytes)
            ResponseEntity.ok(mapOf("message" to "Image stored for $itemId"))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("error" to (e.message ?: "Failed to store image")))
        }

    private fun parseDateParam(dateParam: String?): LocalDate {
        if (dateParam == null) return LocalDate.now(EST)
        val parsed =
            runCatching { LocalDate.parse(dateParam) }
                .getOrElse { throw IllegalArgumentException("Invalid date format: $dateParam") }
        require(parsed >= EARLIEST_DATE) { "Date is before earliest available game" }
        require(parsed <= LocalDate.now(EST)) { "Date is in the future" }
        return parsed
    }
}
