package com.msrp.backend.repositories

import com.msrp.backend.model.GameStats
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface GameStatsRepository : JpaRepository<GameStats, Long> {
    fun findByGameDate(gameDate: LocalDate): GameStats?
}
