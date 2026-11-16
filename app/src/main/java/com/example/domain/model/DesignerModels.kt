package com.example.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.R

enum class FashionBrand(val displayName: String) {
    ROBINS_JEAN("Robin's Jean"),
    RAF_SIMONS("Raf Simons"),
    GALLERY_DEPT("Gallery Dept."),
    STONE_ISLAND("Stone Island"),
    CP_COMPANY("C.P. Company"),
    OFF_WHITE("Off-White"),
    ARMANI_JEANS("Armani Jeans"),
    MCM("MCM"),
    DSQUARED2("Dsquared2"),
    BALENCIAGA("Balenciaga"),
    SALVATORE_FERRAGAMO("Salvatore Ferragamo"),
    AAPE("AAPE"),
    GUTTA("Gutta"),
    PALM_ANGELS("Palm Angels"),
    AMIRI("Amiri"),
    AMBUSH("AMBUSH"),
    GUCCI("Gucci"),
    NIKE("Nike"),
    JORDAN("Jordan"),
    SUPREME("Supreme"),
    MONCLER("Moncler"),
    CHROME_HEARTS("Chrome Hearts"),
    JEREMY_SCOTT("Jeremy Scott"),
}

enum class GarmentType(@param:StringRes val labelRes: Int) {
    CAP(R.string.type_cap),
    HOODIE(R.string.type_hoodie),
    JACKET(R.string.type_jacket),
    BEANIE(R.string.type_beanie),
    JEANS(R.string.type_jeans),
    SHORTS(R.string.type_shorts),
    TSHIRT(R.string.type_tshirt),
    SNEAKERS(R.string.type_sneakers),
    BACKPACK(R.string.type_backpack),
    BELT(R.string.type_belt),
}

enum class WeatherCondition(@param:StringRes val labelRes: Int) {
    ALL(R.string.weather_all),
    HOT(R.string.weather_hot),
    MILD(R.string.weather_mild),
    COLD(R.string.weather_cold),
}

data class DesignerItem(
    val id: String,
    val modelGroupId: String = id,
    val brand: FashionBrand,
    val title: String,
    val type: GarmentType,
    val weatherTags: Set<WeatherCondition>,
    val material: String,
    val colorName: String,
    val colorHex: Long = 0xFF18181BL,
    val description: String,
    val imageUrl: String,
    @param:DrawableRes val fallbackImageRes: Int,
    val avitoUrl: String? = null,
)

data class Outfit(
    val id: String,
    val name: String,
    val itemIds: List<String>,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class CompatibilityVerdict {
    PENDING,
    IDEAL,
    GOOD,
    BOLD,
    CLASH,
}

data class OutfitCompatibility(
    val score: Int,
    val verdict: CompatibilityVerdict,
    val colorScore: Int,
    val silhouetteScore: Int,
    val seasonScore: Int,
    val styleScore: Int,
    val summaryReason: String,
)

data class OutfitRequirementsStatus(
    val hasSneakers: Boolean,
    val hasBottom: Boolean,
    val hasTop: Boolean,
    val noConflictingSlots: Boolean = true,
) {
    val isComplete: Boolean
        get() = hasSneakers && hasBottom && hasTop && noConflictingSlots
}

enum class GoalTargetType {
    ITEM,
    OUTFIT,
}

data class SavingsGoal(
    val id: String,
    val targetType: GoalTargetType,
    val targetId: String,
    val title: String,
    val targetAmountRub: Long,
    val savedAmountRub: Long = 0L,
    val itemIdsSnapshot: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

