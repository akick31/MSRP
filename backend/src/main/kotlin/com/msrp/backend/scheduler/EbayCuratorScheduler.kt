package com.msrp.backend.scheduler

import com.msrp.backend.services.ebay.EbayService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class EbayCuratorScheduler(
    private val ebayService: EbayService,
) {
    private val log = LoggerFactory.getLogger(EbayCuratorScheduler::class.java)

    @Scheduled(cron = "0 0 21 * * ?")
    fun curateDailyItems() {
        log.info("Starting daily eBay item curation")
        for (daysAhead in 1..2) {
            val targetDate = LocalDate.now().plusDays(daysAhead.toLong())
            try {
                log.info("Curating items for {}", targetDate)
                ebayService.curateDailyItems(targetDate)
            } catch (e: Exception) {
                log.error("Curation failed for {}: {}", targetDate, e.message)
            }
        }
    }
}
