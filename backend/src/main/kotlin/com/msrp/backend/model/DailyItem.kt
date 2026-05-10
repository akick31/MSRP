package com.msrp.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "daily_item")
class DailyItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(nullable = false)
    var ebayItemId: String = ""

    @Column(nullable = false)
    var gameDate: LocalDate = LocalDate.now()

    @Column(nullable = false, length = 4096)
    var title: String = ""

    @Column(nullable = false, length = 1024)
    var imageUrl: String = ""

    @Column(nullable = false)
    var bidCount: Int = 0

    @Column(nullable = false)
    var soldPrice: Double = 0.0

    @Column(nullable = false, length = 1024)
    var itemUrl: String = ""

    @Column
    var saleDate: LocalDate? = null
}
