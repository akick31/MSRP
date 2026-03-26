package com.msrp.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
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
    @JsonProperty("id")
    var id: Long = 0

    @Column(nullable = false)
    @JsonProperty("ebay_item_id")
    var ebayItemId: String = ""

    @Column(nullable = false)
    @JsonProperty("game_date")
    var gameDate: LocalDate = LocalDate.now()

    @Column(nullable = false)
    @JsonProperty("title")
    var title: String = ""

    @Column(nullable = false, length = 1024)
    @JsonProperty("image_url")
    var imageUrl: String = ""

    @Column(nullable = false)
    @JsonProperty("bid_count")
    var bidCount: Int = 0

    @Column(nullable = false)
    @JsonProperty("sold_price")
    var soldPrice: Double = 0.0
}
