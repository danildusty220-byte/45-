package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.DesignerItem
import com.example.domain.model.GoalTargetType
import com.example.ui.DesignerFeedUiState
import com.example.ui.ResolvedSavingsGoal
import com.example.ui.components.DesignerGarmentImage
import com.example.ui.components.EditGoalSheet
import com.example.ui.components.GoalDepositSheet
import com.example.ui.components.formatRub

private enum class GoalsFilter {
    ALL,
    ACTIVE,
    COMPLETED,
}

@Composable
fun GoalsScreen(
    uiState: DesignerFeedUiState,
    onBackToFeed: () -> Unit,
    onOpenCreateGoal: (GoalTargetType) -> Unit,
    onAddDeposit: (goalId: String, amountRub: Long) -> Unit,
    onUpdateGoalAmounts: (goalId: String, targetAmountRub: Long, savedAmountRub: Long) -> Unit,
    onDeleteGoal: (goalId: String) -> Unit,
    onItemClick: (DesignerItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackToFeed)

    var activeFilter by remember { mutableStateOf(GoalsFilter.ALL) }
    var depositingGoal by remember { mutableStateOf<ResolvedSavingsGoal?>(null) }
    var editingGoal by remember { mutableStateOf<ResolvedSavingsGoal?>(null) }

    val filteredGoals = remember(uiState.goals, activeFilter) {
        when (activeFilter) {
            GoalsFilter.ALL -> uiState.goals
            GoalsFilter.ACTIVE -> uiState.goals.filter { !it.isCompleted }
            GoalsFilter.COMPLETED -> uiState.goals.filter { it.isCompleted }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp)
                .testTag("goals_screen"),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.goals_header),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.8.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Button(
                    onClick = { onOpenCreateGoal(GoalTargetType.ITEM) },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("create_goal_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.goals_create_btn),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 116.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (uiState.goals.isEmpty()) {
                    item(key = "goals_empty") {
                        EmptyGoalsCard(
                            onPickItemClick = { onOpenCreateGoal(GoalTargetType.ITEM) },
                            onPickOutfitClick = { onOpenCreateGoal(GoalTargetType.OUTFIT) },
                        )
                    }
                } else {
                    item(key = "goals_summary") {
                        GoalsSummaryCard(goals = uiState.goals)
                    }

                    item(key = "goals_filter_row") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                GoalsFilter.ALL to stringResource(R.string.goals_filter_all),
                                GoalsFilter.ACTIVE to stringResource(R.string.goals_filter_active),
                                GoalsFilter.COMPLETED to stringResource(R.string.goals_filter_completed),
                            ).forEach { (filter, label) ->
                                val selected = activeFilter == filter
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { activeFilter = filter }
                                        .testTag("goals_filter_${filter.name.lowercase()}"),
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                    )
                                }
                            }
                        }
                    }

                    if (filteredGoals.isEmpty()) {
                        item(key = "goals_filter_empty") {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = stringResource(R.string.goals_filter_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(24.dp),
                                )
                            }
                        }
                    } else {
                        items(filteredGoals, key = { it.id }) { goal ->
                            GoalCard(
                                goal = goal,
                                onQuickDeposit = { amount -> onAddDeposit(goal.id, amount) },
                                onOpenDepositSheet = { depositingGoal = goal },
                                onOpenEditSheet = { editingGoal = goal },
                                onDelete = { onDeleteGoal(goal.id) },
                                onItemClick = onItemClick,
                            )
                        }
                    }
                }
            }
        }
    }

    val activeDeposit = depositingGoal?.let { target ->
        uiState.goals.firstOrNull { it.id == target.id }
    }
    if (activeDeposit != null) {
        GoalDepositSheet(
            goal = activeDeposit,
            onDismiss = { depositingGoal = null },
            onAddDeposit = { amountRub -> onAddDeposit(activeDeposit.id, amountRub) },
        )
    }

    val activeEdit = editingGoal?.let { target ->
        uiState.goals.firstOrNull { it.id == target.id }
    }
    if (activeEdit != null) {
        EditGoalSheet(
            goal = activeEdit,
            onDismiss = { editingGoal = null },
            onSaveAmounts = { targetRub, savedRub ->
                onUpdateGoalAmounts(activeEdit.id, targetRub, savedRub)
            },
        )
    }
}

@Composable
private fun GoalsSummaryCard(goals: List<ResolvedSavingsGoal>) {
    val totalSaved = goals.sumOf { it.savedAmountRub }
    val totalTarget = goals.sumOf { it.targetAmountRub }
    val activeCount = goals.count { !it.isCompleted }
    val doneCount = goals.count { it.isCompleted }
    val overallFraction = if (totalTarget <= 0L) {
        0f
    } else {
        (totalSaved.toDouble() / totalTarget.toDouble()).toFloat().coerceIn(0f, 1f)
    }
    val animatedProgress by animateFloatAsState(
        targetValue = overallFraction,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "goalsSummaryProgress",
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("goals_summary_card"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.goals_summary_saved),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                text = stringResource(R.string.goals_summary_active, activeCount),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                        if (doneCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Text(
                                    text = stringResource(R.string.goals_summary_done, doneCount),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = formatRub(totalSaved),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.goals_summary_of, formatRub(totalTarget)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp),
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: ResolvedSavingsGoal,
    onQuickDeposit: (Long) -> Unit,
    onOpenDepositSheet: () -> Unit,
    onOpenEditSheet: () -> Unit,
    onDelete: () -> Unit,
    onItemClick: (DesignerItem) -> Unit,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progressFraction,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "goalCardProgress",
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (goal.isCompleted) 2.dp else 1.dp,
            color = if (goal.isCompleted) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_card_${goal.id}"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (goal.targetType == GoalTargetType.OUTFIT) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                ) {
                    Text(
                        text = if (goal.targetType == GoalTargetType.OUTFIT) {
                            stringResource(R.string.goals_badge_outfit, goal.displayItems.size)
                        } else {
                            goal.targetItem?.brand?.displayName?.uppercase()
                                ?: stringResource(R.string.goals_badge_item)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (goal.targetType == GoalTargetType.OUTFIT) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (goal.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = stringResource(R.string.goals_completed_pill),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onOpenEditSheet,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("goal_edit_${goal.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.goals_edit_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("goal_delete_${goal.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(R.string.goals_delete_cd),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            if (goal.targetType == GoalTargetType.ITEM && goal.targetItem != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onItemClick(goal.targetItem) },
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White),
                    ) {
                        DesignerGarmentImage(
                            item = goal.targetItem,
                            contentDescription = goal.targetItem.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = goal.displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = goal.targetItem.colorName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = goal.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        goal.displayItems.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onItemClick(item) },
                            ) {
                                DesignerGarmentImage(
                                    item = item,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Progress readout
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = formatRub(goal.savedAmountRub),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (goal.isCompleted) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.testTag("goal_saved_text_${goal.id}"),
                        )
                        Text(
                            text = "/ ${formatRub(goal.targetAmountRub)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )
                    }
                    Text(
                        text = "${goal.progressPercent}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (goal.isCompleted) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = if (goal.isCompleted) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                )

                Text(
                    text = if (goal.isCompleted) {
                        stringResource(R.string.goals_ready_to_buy)
                    } else {
                        stringResource(R.string.goals_remaining_amount, formatRub(goal.remainingRub))
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (goal.isCompleted) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            // Quick deposit actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = { onQuickDeposit(1_000L) },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("goal_quick_1k_${goal.id}"),
                ) {
                    Text(
                        text = "+1 000 ₽",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                OutlinedButton(
                    onClick = { onQuickDeposit(5_000L) },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("goal_quick_5k_${goal.id}"),
                ) {
                    Text(
                        text = "+5 000 ₽",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                FilledTonalButton(
                    onClick = onOpenDepositSheet,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("goal_deposit_button_${goal.id}"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.goals_deposit_btn),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGoalsCard(
    onPickItemClick: () -> Unit,
    onPickOutfitClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_goals_card"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Savings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }

            Text(
                text = stringResource(R.string.goals_empty_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.goals_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onPickItemClick,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("empty_goal_item_button"),
                ) {
                    Text(
                        text = stringResource(R.string.goals_empty_cta_item),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                FilledTonalButton(
                    onClick = onPickOutfitClick,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("empty_goal_outfit_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Checkroom,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.goals_empty_cta_outfit),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
