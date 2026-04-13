package com.msrp.backend.scheduler

import com.msrp.backend.service.ebay.EbayService
import com.msrp.backend.util.Logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EbayCuratorScheduler(
    private val ebayService: EbayService,
) {

    @Scheduled(cron = "0 0 21 * * ?")
    fun curateDailyItems() {
        Logger.info("Starting daily eBay item curation")
        for (daysAhead in 1..2) {
            val targetDate = LocalDate.now().plusDays(daysAhead.toLong())
            try {
                Logger.info("Curating items for {}", targetDate)
                ebayService.curateDailyItems(targetDate)
            } catch (e: Exception) {
                Logger.error("Error during curation for {}: {}", targetDate, e.message)
            }
        }
    }
}
