package com.msrp.backend.repositories

import com.msrp.backend.model.Analytics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Repository
interface AnalyticsRepository : JpaRepository<Analytics, Long> {
    fun findByEventDateBetween(
        start: LocalDate,
        end: LocalDate,
    ): List<Analytics>

    @Modifying
    @Transactional
    @Query("UPDATE Analytics a SET a.count = a.count + 1 WHERE a.eventDate = :date AND a.eventType = :type")
    fun increment(
        date: LocalDate,
        type: String,
    ): Int
}
