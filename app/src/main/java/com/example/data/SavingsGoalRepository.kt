package com.example.data

import android.content.Context
import com.example.domain.model.GoalTargetType
import com.example.domain.model.SavingsGoal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SavingsGoalRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    private val _goals = MutableStateFlow(loadInitialGoals())
    val goals: StateFlow<List<SavingsGoal>> = _goals.asStateFlow()

    private fun loadInitialGoals(): List<SavingsGoal> {
        val raw = prefs.getString(KEY_GOALS_JSON, null).orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id")
                    val typeName = obj.optString("targetType")
                    val targetType = runCatching { GoalTargetType.valueOf(typeName) }.getOrNull()
                    val targetId = obj.optString("targetId")
                    val title = obj.optString("title")
                    val targetAmountRub = obj.optLong("targetAmountRub", 0L)
                    val savedAmountRub = obj.optLong("savedAmountRub", 0L).coerceAtLeast(0L)
                    val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    val snapshotArray = obj.optJSONArray("itemIdsSnapshot") ?: JSONArray()
                    val itemIdsSnapshot = buildList {
                        for (j in 0 until snapshotArray.length()) {
                            val itemId = snapshotArray.optString(j)
                            if (itemId.isNotBlank()) add(itemId)
                        }
                    }
                    if (id.isNotBlank() && targetType != null && targetId.isNotBlank() && targetAmountRub > 0L) {
                        add(
                            SavingsGoal(
                                id = id,
                                targetType = targetType,
                                targetId = targetId,
                                title = title,
                                targetAmountRub = targetAmountRub,
                                savedAmountRub = savedAmountRub,
                                itemIdsSnapshot = itemIdsSnapshot,
                                createdAt = createdAt,
                            ),
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun createGoal(
        targetType: GoalTargetType,
        targetId: String,
        title: String,
        targetAmountRub: Long,
        itemIdsSnapshot: List<String> = emptyList(),
    ): SavingsGoal {
        val cleanTargetAmount = targetAmountRub.coerceAtLeast(1L)
        val goal = SavingsGoal(
            id = UUID.randomUUID().toString(),
            targetType = targetType,
            targetId = targetId,
            title = title.trim(),
            targetAmountRub = cleanTargetAmount,
            savedAmountRub = 0L,
            itemIdsSnapshot = itemIdsSnapshot.distinct(),
            createdAt = System.currentTimeMillis(),
        )
        persistGoals(listOf(goal) + _goals.value)
        return goal
    }

    fun addDeposit(goalId: String, amountRub: Long): Boolean {
        if (amountRub <= 0L) return false
        var updatedAny = false
        val updated = _goals.value.map { goal ->
            if (goal.id == goalId) {
                updatedAny = true
                goal.copy(savedAmountRub = (goal.savedAmountRub + amountRub).coerceAtLeast(0L))
            } else {
                goal
            }
        }
        if (updatedAny) {
            persistGoals(updated)
        }
        return updatedAny
    }

    fun updateAmounts(
        goalId: String,
        targetAmountRub: Long,
        savedAmountRub: Long,
    ): Boolean {
        if (targetAmountRub <= 0L || savedAmountRub < 0L) return false
        var updatedAny = false
        val updated = _goals.value.map { goal ->
            if (goal.id == goalId) {
                updatedAny = true
                goal.copy(
                    targetAmountRub = targetAmountRub,
                    savedAmountRub = savedAmountRub,
                )
            } else {
                goal
            }
        }
        if (updatedAny) {
            persistGoals(updated)
        }
        return updatedAny
    }

    fun deleteGoal(goalId: String) {
        persistGoals(_goals.value.filterNot { it.id == goalId })
    }

    fun clearAll() {
        persistGoals(emptyList())
    }

    private fun persistGoals(list: List<SavingsGoal>) {
        _goals.value = list
        val array = JSONArray()
        list.forEach { goal ->
            val obj = JSONObject().apply {
                put("id", goal.id)
                put("targetType", goal.targetType.name)
                put("targetId", goal.targetId)
                put("title", goal.title)
                put("targetAmountRub", goal.targetAmountRub)
                put("savedAmountRub", goal.savedAmountRub)
                put("itemIdsSnapshot", JSONArray(goal.itemIdsSnapshot))
                put("createdAt", goal.createdAt)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_GOALS_JSON, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "user_goals_prefs"
        private const val KEY_GOALS_JSON = "savings_goals_json_v1"
    }
}
