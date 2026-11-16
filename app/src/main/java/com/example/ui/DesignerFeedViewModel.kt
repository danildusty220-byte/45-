package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DesignerCatalog
import com.example.data.LikedItemsRepository
import com.example.data.OutfitRepository
import com.example.data.SavingsGoalRepository
import com.example.data.UserProfile
import com.example.data.UserProfileRepository
import com.example.domain.OutfitCompatibilityScorer
import com.example.domain.model.DesignerItem
import com.example.domain.model.FashionBrand
import com.example.domain.model.GarmentType
import com.example.domain.model.GoalTargetType
import com.example.domain.model.Outfit
import com.example.domain.model.OutfitCompatibility
import com.example.domain.model.SavingsGoal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import kotlin.math.roundToInt

data class FilterState(
    val keywordQuery: String = "",
    val selectedBrand: FashionBrand? = null,
    val selectedCategory: GarmentType? = null,
)

data class ResolvedOutfit(
    val outfit: Outfit,
    val items: List<DesignerItem>,
    val compatibility: OutfitCompatibility,
) {
    val id: String get() = outfit.id
    val name: String get() = outfit.name
}

data class ResolvedSavingsGoal(
    val goal: SavingsGoal,
    val targetItem: DesignerItem? = null,
    val targetOutfit: ResolvedOutfit? = null,
    val snapshotItems: List<DesignerItem> = emptyList(),
) {
    val id: String get() = goal.id
    val targetType: GoalTargetType get() = goal.targetType
    val targetAmountRub: Long get() = goal.targetAmountRub
    val savedAmountRub: Long get() = goal.savedAmountRub
    val remainingRub: Long get() = (goal.targetAmountRub - goal.savedAmountRub).coerceAtLeast(0L)
    val progressFraction: Float
        get() = if (goal.targetAmountRub <= 0L) {
            0f
        } else {
            (goal.savedAmountRub.toDouble() / goal.targetAmountRub.toDouble())
                .toFloat()
                .coerceIn(0f, 1f)
        }
    val progressPercent: Int get() = (progressFraction * 100f).roundToInt().coerceIn(0, 100)
    val isCompleted: Boolean
        get() = goal.targetAmountRub > 0L && goal.savedAmountRub >= goal.targetAmountRub

    val displayItems: List<DesignerItem>
        get() = when (goal.targetType) {
            GoalTargetType.ITEM -> listOfNotNull(targetItem)
            GoalTargetType.OUTFIT -> targetOutfit?.items ?: snapshotItems
        }

    val displayTitle: String
        get() = when (goal.targetType) {
            GoalTargetType.ITEM -> targetItem?.title ?: goal.title
            GoalTargetType.OUTFIT -> targetOutfit?.name ?: goal.title
        }
}

data class DesignerFeedUiState(
    val filters: FilterState = FilterState(),
    val feedColumns: Int = UserProfileRepository.DEFAULT_FEED_COLUMNS,
    val allItems: List<DesignerItem> = DesignerCatalog.items,
    val feedItems: List<DesignerItem> = DesignerCatalog.items,
    val likedItems: List<DesignerItem> = emptyList(),
    val outfits: List<ResolvedOutfit> = emptyList(),
    val favoriteOutfitId: String? = null,
    val goals: List<ResolvedSavingsGoal> = emptyList(),
    val userProfile: UserProfile = UserProfile(),
) {
    val likedIds: Set<String>
        get() = likedItems.mapTo(HashSet()) { it.id }

    val favoriteOutfit: ResolvedOutfit?
        get() = favoriteOutfitId?.let { targetId -> outfits.firstOrNull { it.id == targetId } }

    val activeGoalsCount: Int
        get() = goals.count { !it.isCompleted }

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
    private val outfitRepository = OutfitRepository(application)
    private val savingsGoalRepository = SavingsGoalRepository(application)
    private val catalogById = DesignerCatalog.items.associateBy { it.id }

    private val filterState = MutableStateFlow(FilterState())
    private val orderedItemIds = MutableStateFlow(buildInterleavedOrder(DesignerCatalog.items))

    private val wardrobeStateFlow = combine(
        outfitRepository.outfits,
        outfitRepository.favoriteOutfitId,
        savingsGoalRepository.goals,
    ) { outfits, favoriteId, goals -> Triple(outfits, favoriteId, goals) }

    private val feedInputFlow = combine(
        filterState,
        orderedItemIds,
    ) { filters, orderedIds -> filters to orderedIds }

    val uiState: StateFlow<DesignerFeedUiState> = combine(
        feedInputFlow,
        likedRepository.likedIds,
        profileRepository.profile,
        profileRepository.feedColumns,
        wardrobeStateFlow,
    ) { (filters, orderedIds), likedIdList, profile, columns, (rawOutfits, favoriteId, rawGoals) ->
        val orderedCatalog = orderedIds.mapNotNull { catalogById[it] }
        val filtered = filterItems(orderedCatalog, filters)
        val resolvedLikes = likedIdList.mapNotNull { catalogById[it] }.reversed()
        val resolvedOutfits = rawOutfits.mapNotNull { outfit ->
            val items = outfit.itemIds.mapNotNull { catalogById[it] }
            if (items.isEmpty()) {
                null
            } else {
                ResolvedOutfit(
                    outfit = outfit,
                    items = items,
                    compatibility = OutfitCompatibilityScorer.evaluate(items),
                )
            }
        }
        val outfitsById = resolvedOutfits.associateBy { it.id }
        val resolvedGoals = rawGoals.mapNotNull { goal ->
            when (goal.targetType) {
                GoalTargetType.ITEM -> {
                    val item = catalogById[goal.targetId] ?: return@mapNotNull null
                    ResolvedSavingsGoal(
                        goal = goal,
                        targetItem = item,
                    )
                }

                GoalTargetType.OUTFIT -> {
                    val outfit = outfitsById[goal.targetId]
                    val snapshot = goal.itemIdsSnapshot.mapNotNull { catalogById[it] }
                    if (outfit == null && snapshot.isEmpty()) {
                        null
                    } else {
                        ResolvedSavingsGoal(
                            goal = goal,
                            targetOutfit = outfit,
                            snapshotItems = snapshot,
                        )
                    }
                }
            }
        }

        DesignerFeedUiState(
            filters = filters,
            feedColumns = columns,
            allItems = DesignerCatalog.items,
            feedItems = filtered,
            likedItems = resolvedLikes,
            outfits = resolvedOutfits,
            favoriteOutfitId = favoriteId,
            goals = resolvedGoals,
            userProfile = profile,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DesignerFeedUiState(
            feedColumns = profileRepository.feedColumns.value,
            userProfile = profileRepository.profile.value,
        ),
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

    fun updateFeedColumns(columns: Int) {
        profileRepository.setFeedColumns(columns)
    }

    fun shuffleFeed() {
        val current = orderedItemIds.value
        val candidate = current.shuffled()
        orderedItemIds.value = if (candidate == current && current.size > 1) {
            current.drop(1) + current.first()
        } else {
            candidate
        }
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

    fun createOutfit(name: String, itemIds: List<String>): Outfit? {
        val items = itemIds.distinct().mapNotNull { catalogById[it] }
        if (!OutfitCompatibilityScorer.validateRequirements(items).isComplete) {
            return null
        }
        return outfitRepository.createOutfit(name = name, itemIds = items.map { it.id })
    }

    fun updateOutfit(outfitId: String, name: String, itemIds: List<String>): Boolean {
        val items = itemIds.distinct().mapNotNull { catalogById[it] }
        if (!OutfitCompatibilityScorer.validateRequirements(items).isComplete) {
            return false
        }
        outfitRepository.updateOutfit(outfitId = outfitId, name = name, itemIds = items.map { it.id })
        return true
    }

    fun deleteOutfit(outfitId: String) {
        outfitRepository.deleteOutfit(outfitId)
    }

    fun toggleFavoriteOutfit(outfitId: String) {
        outfitRepository.toggleFavoriteOutfit(outfitId)
    }

    fun clearFavoriteOutfit() {
        outfitRepository.setFavoriteOutfit(null)
    }

    fun clearAllOutfits() {
        outfitRepository.clearAll()
    }

    fun createItemGoal(itemId: String, targetAmountRub: Long): SavingsGoal? {
        val item = catalogById[itemId] ?: return null
        if (targetAmountRub <= 0L) return null
        return savingsGoalRepository.createGoal(
            targetType = GoalTargetType.ITEM,
            targetId = item.id,
            title = item.title,
            targetAmountRub = targetAmountRub,
            itemIdsSnapshot = listOf(item.id),
        )
    }

    fun createOutfitGoal(outfitId: String, targetAmountRub: Long): SavingsGoal? {
        if (targetAmountRub <= 0L) return null
        val outfit = outfitRepository.outfits.value.firstOrNull { it.id == outfitId } ?: return null
        return savingsGoalRepository.createGoal(
            targetType = GoalTargetType.OUTFIT,
            targetId = outfit.id,
            title = outfit.name,
            targetAmountRub = targetAmountRub,
            itemIdsSnapshot = outfit.itemIds,
        )
    }

    fun addGoalDeposit(goalId: String, amountRub: Long): Boolean =
        savingsGoalRepository.addDeposit(goalId = goalId, amountRub = amountRub)

    fun updateGoalAmounts(goalId: String, targetAmountRub: Long, savedAmountRub: Long): Boolean =
        savingsGoalRepository.updateAmounts(
            goalId = goalId,
            targetAmountRub = targetAmountRub,
            savedAmountRub = savedAmountRub,
        )

    fun deleteGoal(goalId: String) {
        savingsGoalRepository.deleteGoal(goalId)
    }

    fun clearAllGoals() {
        savingsGoalRepository.clearAll()
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
            val keywordOk = tokens.isEmpty() || matchesAllTokens(item, tokens)
            brandOk && categoryOk && keywordOk
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
        GarmentType.JACKET -> "куртка пуховик дутая верхняя одежда jacket puffer down"
        GarmentType.BEANIE -> "шапка бини beanie hat"
        GarmentType.JEANS -> "джинсы штаны брюки деним багги jeans denim"
        GarmentType.SHORTS -> "шорты бермуды бриджи shorts"
        GarmentType.TSHIRT -> "футболка тишка майка tee tshirt t-shirt"
        GarmentType.SNEAKERS -> "кроссовки кеды сникеры обувь sneakers"
        GarmentType.BACKPACK -> "рюкзак портфель сумка backpack bag"
        GarmentType.BELT -> "ремень пояс пряжка belt"
    }

    private fun buildInterleavedOrder(items: List<DesignerItem>): List<String> {
        val buckets = items.groupBy { it.type }
            .values
            .map { bucket -> ArrayDeque(bucket) }
            .toMutableList()
        val result = ArrayList<String>(items.size)
        var index = 0
        while (buckets.isNotEmpty()) {
            val currentIndex = index % buckets.size
            val queue = buckets[currentIndex]
            result.add(queue.removeFirst().id)
            if (queue.isEmpty()) {
                buckets.removeAt(currentIndex)
            } else {
                index++
            }
        }
        return result
    }
}
