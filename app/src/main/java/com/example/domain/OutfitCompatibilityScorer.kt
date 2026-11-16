package com.example.domain

import android.graphics.Color
import com.example.domain.model.CompatibilityVerdict
import com.example.domain.model.DesignerItem
import com.example.domain.model.FashionBrand
import com.example.domain.model.GarmentType
import com.example.domain.model.OutfitCompatibility
import com.example.domain.model.OutfitRequirementsStatus
import com.example.domain.model.WeatherCondition
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

object OutfitCompatibilityScorer {

    enum class OutfitExclusiveSlot {
        TSHIRT,
        HOODIE,
        JACKET,
        BOTTOM,
        SNEAKERS,
        BELT,
        BACKPACK,
        HEADWEAR,
    }

    fun exclusiveSlotOf(type: GarmentType): OutfitExclusiveSlot = when (type) {
        GarmentType.TSHIRT -> OutfitExclusiveSlot.TSHIRT
        GarmentType.HOODIE -> OutfitExclusiveSlot.HOODIE
        GarmentType.JACKET -> OutfitExclusiveSlot.JACKET
        GarmentType.JEANS, GarmentType.SHORTS -> OutfitExclusiveSlot.BOTTOM
        GarmentType.SNEAKERS -> OutfitExclusiveSlot.SNEAKERS
        GarmentType.BELT -> OutfitExclusiveSlot.BELT
        GarmentType.BACKPACK -> OutfitExclusiveSlot.BACKPACK
        GarmentType.CAP, GarmentType.BEANIE -> OutfitExclusiveSlot.HEADWEAR
    }

    fun findConflictingItem(
        selectedItems: List<DesignerItem>,
        candidate: DesignerItem,
    ): DesignerItem? {
        val candidateSlot = exclusiveSlotOf(candidate.type)
        return selectedItems.firstOrNull { existing ->
            existing.id != candidate.id && exclusiveSlotOf(existing.type) == candidateSlot
        }
    }

    fun hasConflictingSlots(items: List<DesignerItem>): Boolean =
        items.groupingBy { exclusiveSlotOf(it.type) }.eachCount().values.any { it > 1 }

    fun validateRequirements(items: List<DesignerItem>): OutfitRequirementsStatus {
        val hasSneakers = items.any { it.type == GarmentType.SNEAKERS }
        val hasBottom = items.any { it.type == GarmentType.JEANS || it.type == GarmentType.SHORTS }
        val hasTop = items.any { it.type == GarmentType.HOODIE || it.type == GarmentType.TSHIRT }
        val noConflicts = !hasConflictingSlots(items)
        return OutfitRequirementsStatus(
            hasSneakers = hasSneakers,
            hasBottom = hasBottom,
            hasTop = hasTop,
            noConflictingSlots = noConflicts,
        )
    }

    fun evaluate(items: List<DesignerItem>): OutfitCompatibility {
        if (items.size < 2) {
            return OutfitCompatibility(
                score = 0,
                verdict = CompatibilityVerdict.PENDING,
                colorScore = 0,
                silhouetteScore = 0,
                seasonScore = 0,
                styleScore = 0,
                summaryReason = "Добавьте кроссовки, шорты/джинсы и кофту/футболку",
            )
        }

        val colorScore = computeColorHarmony(items)
        val (silhouetteScore, duplicateNote) = computeSilhouetteBalance(items)
        val seasonScore = computeSeasonOverlap(items)
        val styleScore = computeStyleSynergy(items)

        val weighted = (
            colorScore * 0.35 +
                silhouetteScore * 0.30 +
                seasonScore * 0.20 +
                styleScore * 0.15
            ).roundToInt().coerceIn(0, 100)

        val verdict = when {
            weighted >= 85 -> CompatibilityVerdict.IDEAL
            weighted >= 65 -> CompatibilityVerdict.GOOD
            weighted >= 45 -> CompatibilityVerdict.BOLD
            else -> CompatibilityVerdict.CLASH
        }

        val reason = buildSummaryReason(
            items = items,
            colorScore = colorScore,
            silhouetteScore = silhouetteScore,
            seasonScore = seasonScore,
            duplicateNote = duplicateNote,
        )

        return OutfitCompatibility(
            score = weighted,
            verdict = verdict,
            colorScore = colorScore,
            silhouetteScore = silhouetteScore,
            seasonScore = seasonScore,
            styleScore = styleScore,
            summaryReason = reason,
        )
    }

    private fun computeColorHarmony(items: List<DesignerItem>): Int {
        val hsvList = items.map { item ->
            val hsv = FloatArray(3)
            Color.colorToHSV(item.colorHex.toInt(), hsv)
            hsv
        }

        val chromatic = hsvList.filter { hsv ->
            val s = hsv[1]
            val v = hsv[2]
            s >= 0.22f && v >= 0.20f && !(v > 0.88f && s < 0.25f)
        }

        if (chromatic.isEmpty()) {
            // Pure monochrome / neutral palette (black, white, graphite, cream)
            return 94
        }
        if (chromatic.size == 1) {
            // One accent color anchored by neutral pieces
            return 92
        }

        var pairSum = 0
        var pairCount = 0
        for (i in 0 until chromatic.size) {
            for (j in i + 1 until chromatic.size) {
                val rawDiff = abs(chromatic[i][0] - chromatic[j][0])
                val hueDiff = min(rawDiff, 360f - rawDiff)
                val pairScore = when {
                    hueDiff <= 32f -> 94 // Analogous / tonal
                    hueDiff in 150f..180f -> 86 // Complementary contrast
                    hueDiff in 105f..149f -> 76 // Triadic
                    hueDiff in 33f..65f -> 68 // Warm/cool shift
                    else -> 54 // Mild clash
                }
                pairSum += pairScore
                pairCount++
            }
        }
        val neutralBonus = if (hsvList.size > chromatic.size) 4 else 0
        return ((pairSum.toDouble() / pairCount).roundToInt() + neutralBonus).coerceIn(30, 99)
    }

    private fun computeSilhouetteBalance(items: List<DesignerItem>): Pair<Int, String?> {
        val counts = items.groupingBy { exclusiveSlotOf(it.type) }.eachCount()

        val duplicateFootwear = (counts[OutfitExclusiveSlot.SNEAKERS] ?: 0) > 1
        val duplicateBottom = (counts[OutfitExclusiveSlot.BOTTOM] ?: 0) > 1
        val duplicateOtherSlot = counts.any { (slot, count) ->
            slot != OutfitExclusiveSlot.SNEAKERS && slot != OutfitExclusiveSlot.BOTTOM && count > 1
        }

        var score = 70
        val distinctVisualSlots = buildSet {
            items.forEach { item ->
                add(
                    when (exclusiveSlotOf(item.type)) {
                        OutfitExclusiveSlot.TSHIRT, OutfitExclusiveSlot.HOODIE -> "TOP"
                        OutfitExclusiveSlot.JACKET -> "JACKET"
                        OutfitExclusiveSlot.BOTTOM -> "BOTTOM"
                        OutfitExclusiveSlot.SNEAKERS -> "SNEAKERS"
                        OutfitExclusiveSlot.HEADWEAR -> "HEADWEAR"
                        OutfitExclusiveSlot.BELT, OutfitExclusiveSlot.BACKPACK -> item.type.name
                    },
                )
            }
        }.size
        score += (distinctVisualSlots - 1).coerceAtLeast(0) * 9

        val hasTop = counts.containsKey(OutfitExclusiveSlot.TSHIRT) || counts.containsKey(OutfitExclusiveSlot.HOODIE)
        val hasBottom = counts.containsKey(OutfitExclusiveSlot.BOTTOM)
        val hasFootwear = counts.containsKey(OutfitExclusiveSlot.SNEAKERS)
        val hasMainCore = (hasFootwear && hasBottom) || (hasTop && hasBottom) || (hasTop && hasFootwear)
        if (hasMainCore) score += 8

        var duplicateNote: String? = null
        if (duplicateFootwear) {
            score -= 28
            duplicateNote = "Дублируется обувь — оставьте одну пару кроссовок"
        } else if (duplicateBottom) {
            score -= 25
            duplicateNote = "Нельзя надеть джинсы и шорты или две пары низа одновременно"
        } else if (duplicateOtherSlot) {
            score -= 18
            duplicateNote = "Уберите вторую вещь из одного слота"
        }

        return score.coerceIn(20, 100) to duplicateNote
    }

    private fun computeSeasonOverlap(items: List<DesignerItem>): Int {
        val tagSets = items.map { it.weatherTags.filter { tag -> tag != WeatherCondition.ALL }.toSet() }
        val common = tagSets.reduce { acc, set -> acc.intersect(set) }
        if (common.isNotEmpty()) {
            return (88 + common.size * 5).coerceAtMost(100)
        }

        var overlapSum = 0.0
        var pairs = 0
        for (i in 0 until tagSets.size) {
            for (j in i + 1 until tagSets.size) {
                val unionSize = (tagSets[i] union tagSets[j]).size.coerceAtLeast(1)
                val interSize = (tagSets[i] intersect tagSets[j]).size
                overlapSum += (interSize.toDouble() / unionSize) * 100.0
                pairs++
            }
        }
        return if (pairs == 0) 60 else (overlapSum / pairs).roundToInt().coerceIn(30, 85)
    }

    private fun computeStyleSynergy(items: List<DesignerItem>): Int {
        val brands = items.map { it.brand }.toSet()
        if (brands.size == 1) return 96

        val avantArchive = setOf(
            FashionBrand.RAF_SIMONS,
            FashionBrand.OFF_WHITE,
            FashionBrand.GALLERY_DEPT,
            FashionBrand.BALENCIAGA,
            FashionBrand.GUTTA,
            FashionBrand.AMBUSH,
            FashionBrand.NIKE,
            FashionBrand.STONE_ISLAND,
            FashionBrand.CP_COMPANY,
        )
        val boldLogo = setOf(
            FashionBrand.ROBINS_JEAN,
            FashionBrand.MCM,
            FashionBrand.ARMANI_JEANS,
            FashionBrand.DSQUARED2,
            FashionBrand.SALVATORE_FERRAGAMO,
            FashionBrand.AAPE,
            FashionBrand.PALM_ANGELS,
            FashionBrand.AMIRI,
            FashionBrand.GUCCI,
            FashionBrand.JORDAN,
            FashionBrand.SUPREME,
            FashionBrand.MONCLER,
            FashionBrand.CHROME_HEARTS,
            FashionBrand.JEREMY_SCOTT,
        )

        return when {
            brands.all { it in avantArchive } -> 90
            brands.all { it in boldLogo } -> 88
            FashionBrand.OFF_WHITE in brands && FashionBrand.MCM in brands -> 84
            FashionBrand.ARMANI_JEANS in brands && FashionBrand.RAF_SIMONS in brands -> 82
            FashionBrand.NIKE in brands || FashionBrand.JORDAN in brands -> 84
            else -> 76
        }
    }

    private fun buildSummaryReason(
        items: List<DesignerItem>,
        colorScore: Int,
        silhouetteScore: Int,
        seasonScore: Int,
        duplicateNote: String?,
    ): String {
        if (duplicateNote != null) return duplicateNote

        val highlights = mutableListOf<String>()
        if (colorScore >= 85) {
            highlights.add("гармоничная палитра")
        } else if (colorScore < 65) {
            highlights.add("контрастные оттенки")
        }

        if (silhouetteScore >= 85) {
            highlights.add("сбалансированный силуэт")
        }

        if (seasonScore >= 88) {
            highlights.add("единая сезонность")
        } else if (seasonScore < 55) {
            highlights.add("вещи под разную погоду")
        }

        if (highlights.isEmpty()) {
            return "Экспериментальный микс из ${items.size} вещей"
        }
        return highlights.joinToString(" · ").replaceFirstChar { it.uppercase() }
    }
}
