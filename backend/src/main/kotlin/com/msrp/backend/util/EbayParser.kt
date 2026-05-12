package com.msrp.backend.util

import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object EbayParser {
    private val serpCardSelectors =
        listOf(
            "ul.srp-results > li.s-card",
            "ul.srp-results > li.s-item",
            "ul.srp-results li.s-card",
            "ul.srp-results li.s-item",
            "li.s-card",
            "li.s-item",
            "li[data-listingid]",
            ".srp-river-results li.s-card",
            ".srp-river-results li.s-item",
            "div.s-card",
            "li.brwrvr__item-card",
            "div.brwrvr__item-card",
        )

    private val saleDateFormats =
        listOf(
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH),
        )

    private val pixelHintRegex = Regex("""/s-l(\d+)""", RegexOption.IGNORE_CASE)
    private val imageUpgradeRegex = Regex("""/s-l\d+""", RegexOption.IGNORE_CASE)
    private val priceStripRegex = Regex("[^0-9.]")

    fun parseSerpRows(html: String): List<org.jsoup.nodes.Element> = collectSerpListingElements(Jsoup.parse(html))

    private fun collectSerpListingElements(doc: org.jsoup.nodes.Document): List<org.jsoup.nodes.Element> {
        val seen = java.util.IdentityHashMap<org.jsoup.nodes.Element, Boolean>()
        val out = mutableListOf<org.jsoup.nodes.Element>()
        for (sel in serpCardSelectors) {
            for (el in doc.select(sel)) {
                if (el.selectFirst("a[href*='/itm/']") == null) continue
                if (seen.put(el, true) != null) continue
                out.add(el)
            }
        }
        return out
    }

    fun extractSerpTitle(el: org.jsoup.nodes.Element): String {
        val raw =
            el.selectFirst(
                ".s-card__title, .s-item__title, [class*='s-card__title'], [class*='s-item__title'], div[role=heading].s-card__title, h3.s-card__title",
            )?.text()
                ?: el.selectFirst("a[href*='/itm/']")?.attr("aria-label")?.trim()
                ?: ""
        return raw.replace("Opens in a new window or tab", "", ignoreCase = true).trim()
    }

    fun extractSerpSaleDate(el: org.jsoup.nodes.Element): LocalDate? {
        val raw =
            el.selectFirst(
                ".s-item__ended-date, .s-item__caption--signal.POSITIVE, span.POSITIVE, .s-item__caption--signal",
            )?.text() ?: return null
        val cleaned = raw.replace("Sold", "", ignoreCase = true).trim()
        for (fmt in saleDateFormats) {
            try {
                return LocalDate.parse(cleaned, fmt)
            } catch (_: DateTimeParseException) {
                continue
            }
        }
        return null
    }

    fun extractSerpItemHref(el: org.jsoup.nodes.Element): String? {
        val a = el.selectFirst("a.s-card__link, a.s-item__link, a[href*='/itm/']") ?: return null
        var href = a.attr("href").trim()
        if (href.isEmpty()) return null
        if (href.startsWith("/itm/")) href = "https://www.ebay.com$href"
        if (!href.contains("/itm/")) return null
        return href
    }

    fun normalizeMediaUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val t = raw.trim()
        return when {
            t.startsWith("http://", ignoreCase = true) || t.startsWith("https://", ignoreCase = true) -> t
            t.startsWith("//") -> "https:$t"
            t.startsWith("/images/") -> "https://i.ebayimg.com$t"
            else -> null
        }
    }

    fun ebayImagePathPixelHint(url: String): Int = pixelHintRegex.find(url)?.groupValues?.get(1)?.toIntOrNull() ?: 0

    fun upgradeEbayImageUrl(url: String): String {
        if (!url.contains("ebayimg", ignoreCase = true)) return url
        return imageUpgradeRegex.replace(url, "/s-l1600")
    }

    fun firstImageUrlFromImg(imgEl: org.jsoup.nodes.Element?): String? {
        if (imgEl == null) return null
        var bestUrl: String? = null
        var bestScore = -1

        fun consider(raw: String?) {
            val u = normalizeMediaUrl(raw) ?: return
            val s = ebayImagePathPixelHint(u)
            if (s > bestScore) {
                bestScore = s
                bestUrl = u
            }
        }

        consider(imgEl.attr("data-zoom-src"))
        consider(imgEl.attr("data-original"))
        consider(imgEl.attr("data-src"))
        consider(imgEl.attr("src"))

        val srcset = imgEl.attr("srcset").trim()
        if (srcset.isNotEmpty()) {
            for (segment in srcset.split(',')) {
                val t = segment.trim()
                if (t.isEmpty()) continue
                val urlToken = t.substringBefore(' ').trim()
                val rest = t.removePrefix(urlToken).trim()
                val widthFromDescriptor =
                    when {
                        rest.endsWith("w", ignoreCase = true) ->
                            rest.dropLast(1).trim().toIntOrNull() ?: 0
                        else -> 0
                    }
                val u = normalizeMediaUrl(urlToken) ?: continue
                val s = maxOf(widthFromDescriptor, ebayImagePathPixelHint(u))
                if (s > bestScore) {
                    bestScore = s
                    bestUrl = u
                }
            }
        }

        val picked = bestUrl ?: return null
        return upgradeEbayImageUrl(picked)
    }

    fun parsePrice(text: String): Double? {
        if (text.contains(" to ")) return null
        val cleaned = priceStripRegex.replace(text, "")
        return cleaned.toDoubleOrNull()
    }
}
