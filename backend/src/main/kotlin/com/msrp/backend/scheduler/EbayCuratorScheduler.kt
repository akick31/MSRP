package com.msrp.backend.scheduler

import com.msrp.backend.repositories.DailyItemRepository
import com.msrp.backend.services.ebay.EbayService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId

@Component
class EbayCuratorScheduler(
    private val ebayService: EbayService,
    private val dailyItemRepository: DailyItemRepository,
) {
    private val log = LoggerFactory.getLogger(EbayCuratorScheduler::class.java)
    private val bufferDays = 14L
    private val est = ZoneId.of("America/New_York")

    @Scheduled(cron = "0 0 21 * * ?")
    fun curateDailyItems() {
        val bufferedUntil = LocalDate.now(est).plusDays(bufferDays)
        if (dailyItemRepository.findByGameDate(bufferedUntil).size >= 5) {
            log.info("Items buffered through {}, skipping daily curation", bufferedUntil)
            return
        }
        log.info("Starting daily eBay item curation")
        for (daysAhead in 1..2) {
            val targetDate = LocalDate.now(est).plusDays(daysAhead.toLong())
            try {
                log.info("Curating items for {}", targetDate)
                ebayService.curateDailyItems(targetDate)
            } catch (e: Exception) {
                log.error("Curation failed for {}: {}", targetDate, e.message)
            }
        }
    }
}
