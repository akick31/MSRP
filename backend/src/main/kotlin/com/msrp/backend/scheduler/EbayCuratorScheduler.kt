package com.msrp.backend.scheduler

import com.msrp.backend.service.ebay.EbayService
import com.msrp.backend.util.Logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class EbayCuratorScheduler(
    private val ebayService: EbayService,
) {

    @Scheduled(cron = "0 0 2 * * ?")
    fun curateDailyItems() {
        Logger.info("Starting daily eBay item curation")
        try {
            ebayService.curateDailyItems()
        } catch (e: Exception) {
            Logger.error("Error during daily curation: {}", e.message)
        }
    }
}
