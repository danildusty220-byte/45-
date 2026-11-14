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
}

enum class GarmentType(@param:StringRes val labelRes: Int) {
    CAP(R.string.type_cap),
    HOODIE(R.string.type_hoodie),
    BEANIE(R.string.type_beanie),
    JEANS(R.string.type_jeans),
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
)
