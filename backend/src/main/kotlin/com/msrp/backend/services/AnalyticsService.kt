package com.msrp.backend.services

import com.msrp.backend.model.Analytics
import com.msrp.backend.model.GameStats
import com.msrp.backend.repositories.AnalyticsRepository
import com.msrp.backend.repositories.GameStatsRepository
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class AnalyticsService(
    private val analyticsRepository: AnalyticsRepository,
    private val gameStatsRepository: GameStatsRepository,
) {
    private val log = LoggerFactory.getLogger(AnalyticsService::class.java)

    companion object {
        private val ZONE: ZoneId = ZoneId.of("America/New_York")
        private val ALLOWED_EVENTS = setOf("UNIQUE_VISITORS", "GAMES_PLAYED", "REPLAY_PLAYED")
        private const val MAX_SCORE = 500
        private const val MIN_SCORE = 0

        fun today(): LocalDate = ZonedDateTime.now(ZONE).toLocalDate()
    }

    @Transactional
    fun record(eventType: String) {
        require(eventType in ALLOWED_EVENTS) { "Unknown event type: $eventType" }
        val date = today()
        val updated = analyticsRepository.increment(date, eventType)
        if (updated == 0) {
            try {
                analyticsRepository.save(Analytics(eventDate = date, eventType = eventType, count = 1))
            } catch (_: Exception) {
                analyticsRepository.increment(date, eventType)
            }
        }
        log.debug("Analytics recorded: {}", eventType)
    }

    fun recordFromRequest(body: Map<String, String>): ResponseEntity<Any> {
        return try {
            val eventType =
                body["eventType"]
                    ?: return ResponseEntity.badRequest().body(mapOf("error" to "eventType required"))
            record(eventType)
            ResponseEntity.ok(mapOf("ok" to true))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("error" to e.message))
        }
    }

    @Transactional
    fun submitScore(body: Map<String, String>): ResponseEntity<Any> {
        val scoreStr =
            body["score"]
                ?: return ResponseEntity.badRequest().body(mapOf("error" to "score required"))
        val dateStr =
            body["date"]
                ?: return ResponseEntity.badRequest().body(mapOf("error" to "date required"))
        val score =
            scoreStr.toIntOrNull()
                ?: return ResponseEntity.badRequest().body(mapOf("error" to "score must be an integer"))
        if (score < MIN_SCORE || score > MAX_SCORE) {
            return ResponseEntity.badRequest().body(mapOf("error" to "score must be between $MIN_SCORE and $MAX_SCORE"))
        }
        val gameDate =
            runCatching { LocalDate.parse(dateStr) }.getOrElse {
                return ResponseEntity.badRequest().body(mapOf("error" to "invalid date format"))
            }
        val stats = gameStatsRepository.findByGameDate(gameDate) ?: GameStats(gameDate = gameDate)
        if (stats.highScore == null || score > stats.highScore!!) stats.highScore = score
        if (stats.lowScore == null || score < stats.lowScore!!) stats.lowScore = score
        stats.totalScore += score
        stats.scoreCount += 1
        gameStatsRepository.save(stats)
        return ResponseEntity.ok(mapOf("ok" to true))
    }

    fun getGameStats(dateStr: String): ResponseEntity<Any> {
        val gameDate = runCatching { LocalDate.parse(dateStr) }.getOrElse {
            return ResponseEntity.badRequest().body(mapOf("error" to "invalid date format"))
        }
        val stats = gameStatsRepository.findByGameDate(gameDate)
        val avg = if (stats != null && stats.scoreCount > 0) stats.totalScore.toDouble() / stats.scoreCount else null
        return ResponseEntity.ok(
            mapOf(
                "date" to gameDate.toString(),
                "highScore" to stats?.highScore,
                "lowScore" to stats?.lowScore,
                "avgScore" to avg?.let { Math.round(it).toInt() },
                "playerCount" to (stats?.scoreCount ?: 0),
            ),
        )
    }

    fun getSummary(days: Int): ResponseEntity<Any> {
        val end = today()
        val start = end.minusDays(days.toLong())
        val events =
            analyticsRepository.findByEventDateBetween(start, end)
                .sortedWith(compareBy({ it.eventDate }, { it.eventType }))
                .map { mapOf("date" to it.eventDate.toString(), "eventType" to it.eventType, "count" to it.count) }
        val gameStats =
            gameStatsRepository.findAll()
                .sortedBy { it.gameDate }
                .map { mapOf("date" to it.gameDate.toString(), "highScore" to it.highScore, "lowScore" to it.lowScore) }
        return ResponseEntity.ok(
            mapOf(
                "events" to events,
                "gameStats" to gameStats,
            ),
        )
    }
}
