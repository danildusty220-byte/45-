package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LikedItemsRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    private val _likedIds = MutableStateFlow(loadInitialLikedIds())
    val likedIds: Flow<List<String>> = _likedIds.asStateFlow()

    private fun loadInitialLikedIds(): List<String> {
        val raw = prefs.getString(KEY_LIKED_IDS, "").orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split(",").filter { it.isNotBlank() }.distinct()
    }

    fun addLike(itemId: String) {
        val updated = (_likedIds.value.filterNot { it == itemId } + itemId)
        persistLikes(updated)
    }

    fun removeLike(itemId: String) {
        val updated = _likedIds.value.filterNot { it == itemId }
        persistLikes(updated)
    }

    fun toggleLike(itemId: String) {
        if (itemId in _likedIds.value) removeLike(itemId) else addLike(itemId)
    }

    fun clearAll() {
        persistLikes(emptyList())
    }

    private fun persistLikes(ids: List<String>) {
        _likedIds.value = ids
        prefs.edit().putString(KEY_LIKED_IDS, ids.joinToString(",")).apply()
    }

    companion object {
        private const val PREFS_NAME = "designer_likes_prefs"
        private const val KEY_LIKED_IDS = "liked_item_ids_v3"
    }
}
