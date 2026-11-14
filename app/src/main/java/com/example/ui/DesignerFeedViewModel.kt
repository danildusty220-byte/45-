package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DesignerCatalog
import com.example.data.LikedItemsRepository
import com.example.data.UserProfile
import com.example.data.UserProfileRepository
import com.example.domain.model.DesignerItem
import com.example.domain.model.FashionBrand
import com.example.domain.model.GarmentType
import com.example.domain.model.WeatherCondition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

data class FilterState(
    val keywordQuery: String = "",
    val selectedBrand: FashionBrand? = null,
    val selectedCategory: GarmentType? = null,
    val selectedWeather: WeatherCondition = WeatherCondition.ALL,
)

data class DesignerFeedUiState(
    val filters: FilterState = FilterState(),
    val allItems: List<DesignerItem> = DesignerCatalog.items,
    val feedItems: List<DesignerItem> = DesignerCatalog.items,
    val likedItems: List<DesignerItem> = emptyList(),
    val userProfile: UserProfile = UserProfile(),
) {
    val likedIds: Set<String>
        get() = likedItems.mapTo(HashSet()) { it.id }

    val availableBrands: List<FashionBrand>
        get() = allItems.map { it.brand }.distinct()

    val availableCategories: List<GarmentType>
        get() = allItems.map { it.type }.distinct()

    fun colorVariantsFor(item: DesignerItem): List<DesignerItem> =
        allItems.filter { it.modelGroupId == item.modelGroupId }
}

class DesignerFeedViewModel(application: Application) : AndroidViewModel(application) {

    private val likedRepository = LikedItemsRepository(application)
    private val profileRepository = UserProfileRepository(application)
    private val catalogById = DesignerCatalog.items.associateBy { it.id }

    private val filterState = MutableStateFlow(FilterState())

    val uiState: StateFlow<DesignerFeedUiState> = combine(
        filterState,
        likedRepository.likedIds,
        profileRepository.profile,
    ) { filters, likedIdList, profile ->
        val filtered = filterItems(DesignerCatalog.items, filters)
        val resolvedLikes = likedIdList.mapNotNull { catalogById[it] }.reversed()

        DesignerFeedUiState(
            filters = filters,
            allItems = DesignerCatalog.items,
            feedItems = filtered,
            likedItems = resolvedLikes,
            userProfile = profile,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DesignerFeedUiState(),
    )

    fun updateKeyword(query: String) {
        filterState.value = filterState.value.copy(keywordQuery = query)
    }

    fun selectBrand(brand: FashionBrand?) {
        filterState.value = filterState.value.copy(selectedBrand = brand)
    }

    fun selectCategory(category: GarmentType?) {
        filterState.value = filterState.value.copy(selectedCategory = category)
    }

    fun selectWeather(weather: WeatherCondition) {
        filterState.value = filterState.value.copy(selectedWeather = weather)
    }

    fun resetFilters() {
        filterState.value = FilterState()
    }

    fun toggleLike(itemId: String) {
        likedRepository.toggleLike(itemId)
    }

    fun removeLike(itemId: String) {
        likedRepository.removeLike(itemId)
    }

    fun clearAllLikes() {
        likedRepository.clearAll()
    }

    fun updateProfile(
        displayName: String,
        username: String,
        bio: String,
        avatarEmoji: String?,
        gradientStartArgb: Long,
        gradientEndArgb: Long,
        useGradientBackground: Boolean,
    ) {
        profileRepository.updateProfile(
            displayName = displayName,
            username = username,
            bio = bio,
            avatarEmoji = avatarEmoji,
            gradientStartArgb = gradientStartArgb,
            gradientEndArgb = gradientEndArgb,
            useGradientBackground = useGradientBackground,
        )
    }

    fun saveAvatarFromUri(uri: Uri) {
        profileRepository.saveAvatarFromUri(uri)
    }

    fun clearAvatarPhoto() {
        profileRepository.clearAvatarPhoto()
    }

    fun saveCoverFromUri(uri: Uri) {
        profileRepository.saveCoverFromUri(uri)
    }

    fun clearCoverPhoto() {
        profileRepository.clearCoverPhoto()
    }

    private fun filterItems(source: List<DesignerItem>, filters: FilterState): List<DesignerItem> {
        val tokens = filters.keywordQuery
            .trim()
            .lowercase(Locale.ROOT)
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        return source.filter { item ->
            val brandOk = filters.selectedBrand == null || item.brand == filters.selectedBrand
            val categoryOk = filters.selectedCategory == null || item.type == filters.selectedCategory
            val weatherOk = filters.selectedWeather == WeatherCondition.ALL ||
                filters.selectedWeather in item.weatherTags
            val keywordOk = tokens.isEmpty() || matchesAllTokens(item, tokens)
            brandOk && categoryOk && weatherOk && keywordOk
        }
    }

    private fun matchesAllTokens(item: DesignerItem, tokens: List<String>): Boolean {
        val searchable = buildString {
            append(item.brand.displayName)
            append(' ')
            append(item.title)
            append(' ')
            append(item.description)
            append(' ')
            append(item.material)
            append(' ')
            append(item.colorName)
            append(' ')
            append(garmentTypeKeywords(item.type))
        }.lowercase(Locale.ROOT)

        return tokens.all { token -> token in searchable }
    }

    private fun garmentTypeKeywords(type: GarmentType): String = when (type) {
        GarmentType.CAP -> "кепка бейсболка тракер cap hat"
        GarmentType.HOODIE -> "худи зипка зип-худи толстовка кофта hoodie"
        GarmentType.BEANIE -> "шапка бини beanie hat"
        GarmentType.JEANS -> "джинсы штаны брюки деним багги jeans denim"
        GarmentType.TSHIRT -> "футболка тишка майка tee tshirt t-shirt"
        GarmentType.SNEAKERS -> "кроссовки кеды сникеры обувь sneakers"
        GarmentType.BACKPACK -> "рюкзак портфель сумка backpack bag"
        GarmentType.BELT -> "ремень пояс пряжка belt"
    }
}
