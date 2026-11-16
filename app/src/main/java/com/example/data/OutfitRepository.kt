package com.example.data

import android.content.Context
import com.example.domain.model.Outfit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class OutfitRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    private val _outfits = MutableStateFlow(loadInitialOutfits())
    val outfits: StateFlow<List<Outfit>> = _outfits.asStateFlow()

    private val _favoriteOutfitId = MutableStateFlow(loadInitialFavoriteId())
    val favoriteOutfitId: StateFlow<String?> = _favoriteOutfitId.asStateFlow()

    private fun loadInitialOutfits(): List<Outfit> {
        val raw = prefs.getString(KEY_OUTFITS_JSON, null).orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id")
                    val name = obj.optString("name")
                    val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    val idsArray = obj.optJSONArray("itemIds") ?: JSONArray()
                    val itemIds = buildList {
                        for (j in 0 until idsArray.length()) {
                            val itemId = idsArray.optString(j)
                            if (itemId.isNotBlank()) add(itemId)
                        }
                    }
                    if (id.isNotBlank() && itemIds.isNotEmpty()) {
                        add(Outfit(id = id, name = name, itemIds = itemIds, createdAt = createdAt))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun loadInitialFavoriteId(): String? {
        val savedId = prefs.getString(KEY_FAVORITE_OUTFIT_ID, null)?.takeIf { it.isNotBlank() }
        if (savedId == null) return null
        return savedId.takeIf { id -> _outfits.value.any { it.id == id } }
    }

    fun createOutfit(name: String, itemIds: List<String>): Outfit {
        val distinctIds = itemIds.distinct()
        val cleanName = name.trim().ifBlank { "Аутфит #${_outfits.value.size + 1}" }
        val outfit = Outfit(
            id = UUID.randomUUID().toString(),
            name = cleanName,
            itemIds = distinctIds,
            createdAt = System.currentTimeMillis(),
        )
        persistOutfits(listOf(outfit) + _outfits.value)
        return outfit
    }

    fun updateOutfit(outfitId: String, name: String, itemIds: List<String>) {
        val distinctIds = itemIds.distinct()
        val cleanName = name.trim().ifBlank { "Аутфит" }
        val updated = _outfits.value.map { existing ->
            if (existing.id == outfitId) {
                existing.copy(name = cleanName, itemIds = distinctIds)
            } else {
                existing
            }
        }
        persistOutfits(updated)
    }

    fun deleteOutfit(outfitId: String) {
        val updated = _outfits.value.filterNot { it.id == outfitId }
        persistOutfits(updated)
        if (_favoriteOutfitId.value == outfitId) {
            setFavoriteOutfit(null)
        }
    }

    fun toggleFavoriteOutfit(outfitId: String) {
        val target = if (_favoriteOutfitId.value == outfitId) null else outfitId
        setFavoriteOutfit(target)
    }

    fun setFavoriteOutfit(outfitId: String?) {
        val validId = outfitId?.takeIf { id -> _outfits.value.any { it.id == id } }
        _favoriteOutfitId.value = validId
        if (validId == null) {
            prefs.edit().remove(KEY_FAVORITE_OUTFIT_ID).apply()
        } else {
            prefs.edit().putString(KEY_FAVORITE_OUTFIT_ID, validId).apply()
        }
    }

    fun clearAll() {
        persistOutfits(emptyList())
        setFavoriteOutfit(null)
    }

    private fun persistOutfits(list: List<Outfit>) {
        _outfits.value = list
        val array = JSONArray()
        list.forEach { outfit ->
            val obj = JSONObject().apply {
                put("id", outfit.id)
                put("name", outfit.name)
                put("createdAt", outfit.createdAt)
                put("itemIds", JSONArray(outfit.itemIds))
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_OUTFITS_JSON, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "user_outfits_prefs"
        private const val KEY_OUTFITS_JSON = "outfits_json_v1"
        private const val KEY_FAVORITE_OUTFIT_ID = "favorite_outfit_id_v1"
    }
}
