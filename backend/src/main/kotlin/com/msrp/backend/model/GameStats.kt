package com.msrp.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    name = "game_stats",
    uniqueConstraints = [UniqueConstraint(columnNames = ["game_date"])],
)
class GameStats(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "game_date", nullable = false)
    var gameDate: LocalDate,
    @Column(name = "high_score", nullable = true)
    var highScore: Int? = null,
    @Column(name = "low_score", nullable = true)
    var lowScore: Int? = null,
)
