package com.msrp.backend.repositories

import com.msrp.backend.model.DailyItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface DailyItemRepository : JpaRepository<DailyItem, Long> {
    fun findByGameDate(gameDate: LocalDate): List<DailyItem>

    @Query("SELECT DISTINCT d.gameDate FROM DailyItem d WHERE d.gameDate < :today ORDER BY d.gameDate ASC")
    fun findDistinctGameDatesBefore(today: LocalDate): List<LocalDate>
}
