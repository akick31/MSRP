package com.msrp.backend.repositories

import com.msrp.backend.model.DailyItem
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface DailyItemRepository : CrudRepository<DailyItem, Long> {
    fun findByGameDate(gameDate: LocalDate): List<DailyItem>
}
