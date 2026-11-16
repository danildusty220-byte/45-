package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.DesignerItem
import com.example.domain.model.GoalTargetType
import com.example.ui.ResolvedOutfit
import com.example.ui.ResolvedSavingsGoal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun formatRub(amount: Long): String {
    val symbols = DecimalFormatSymbols(Locale("ru", "RU")).apply {
        groupingSeparator = ' '
    }
    val formatted = DecimalFormat("#,###", symbols).format(amount.coerceAtLeast(0L))
    return "$formatted ₽"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalSheet(
    allItems: List<DesignerItem>,
    outfits: List<ResolvedOutfit>,
    initialItem: DesignerItem? = null,
    initialOutfit: ResolvedOutfit? = null,
    initialMode: GoalTargetType = if (initialOutfit != null) GoalTargetType.OUTFIT else GoalTargetType.ITEM,
    onDismiss: () -> Unit,
    onGoToOutfits: () -> Unit,
    onCreateItemGoal: (itemId: String, targetAmountRub: Long) -> Unit,
    onCreateOutfitGoal: (outfitId: String, targetAmountRub: Long) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var mode by remember(initialMode, initialItem, initialOutfit) {
        mutableStateOf(
            when {
                initialOutfit != null -> GoalTargetType.OUTFIT
                initialItem != null -> GoalTargetType.ITEM
                else -> initialMode
            },
        )
    }
    var searchQuery by remember { mutableStateOf("") }
    var selectedItemId by remember(initialItem) {
        mutableStateOf(initialItem?.id ?: allItems.firstOrNull()?.id)
    }
    var selectedOutfitId by remember(initialOutfit, outfits) {
        mutableStateOf(initialOutfit?.id ?: outfits.firstOrNull()?.id)
    }
    var priceInput by remember { mutableStateOf("") }

    val filteredItems = remember(allItems, searchQuery) {
        val q = searchQuery.trim().lowercase(Locale.ROOT)
        if (q.isBlank()) {
            allItems
        } else {
            allItems.filter { item ->
                q in item.title.lowercase(Locale.ROOT) ||
                    q in item.brand.displayName.lowercase(Locale.ROOT) ||
                    q in item.colorName.lowercase(Locale.ROOT)
            }
        }
    }

    val parsedAmount = priceInput.toLongOrNull() ?: 0L
    val canSubmit = parsedAmount > 0L && when (mode) {
        GoalTargetType.ITEM -> selectedItemId != null
        GoalTargetType.OUTFIT -> selectedOutfitId != null && outfits.any { it.id == selectedOutfitId }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.goal_create_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            // Mode selector: Single item vs Whole outfit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    GoalTargetType.ITEM to stringResource(R.string.goal_mode_item),
                    GoalTargetType.OUTFIT to stringResource(R.string.goal_mode_outfit),
                ).forEach { (tabMode, label) ->
                    val active = mode == tabMode
                    val bg by animateColorAsState(
                        targetValue = if (active) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        },
                        label = "goalModeTabBg",
                    )
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = bg,
                        border = BorderStroke(
                            1.dp,
                            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { mode = tabMode }
                            .testTag("goal_mode_${tabMode.name.lowercase()}"),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                color = if (active) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            if (mode == GoalTargetType.ITEM) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.goal_search_item_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.search_clear_cd),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_item_search_input"),
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 230.dp),
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val isSelected = item.id == selectedItemId
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            },
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedItemId = item.id }
                                .testTag("goal_pick_item_${item.id}"),
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White),
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.brand.displayName.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = item.colorName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (outfits.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_no_outfits_notice"),
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Checkroom,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp),
                            )
                            Text(
                                text = stringResource(R.string.goal_no_outfits_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = stringResource(R.string.goal_no_outfits_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            FilledTonalButton(
                                onClick = {
                                    onDismiss()
                                    onGoToOutfits()
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(48.dp),
                            ) {
                                Text(stringResource(R.string.goal_go_to_outfits))
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 230.dp),
                    ) {
                        items(outfits, key = { it.id }) { outfit ->
                            val isSelected = outfit.id == selectedOutfitId
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                },
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedOutfitId = outfit.id }
                                    .testTag("goal_pick_outfit_${outfit.id}"),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = outfit.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = stringResource(R.string.outfit_items_count, outfit.items.size),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            }
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        outfit.items.forEach { item ->
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color.White),
                                            ) {
                                                DesignerGarmentImage(
                                                    item = item,
                                                    contentDescription = item.title,
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(3.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Price target input + preset chips
            OutlinedTextField(
                value = priceInput,
                onValueChange = { raw ->
                    priceInput = raw.filter { it.isDigit() }.take(9)
                },
                label = { Text(stringResource(R.string.goal_target_price_label)) },
                placeholder = { Text(stringResource(R.string.goal_target_price_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_target_price_input"),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(15_000L, 30_000L, 50_000L, 100_000L).forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { priceInput = preset.toString() },
                    ) {
                        Text(
                            text = formatRub(preset),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            Button(
                onClick = {
                    when (mode) {
                        GoalTargetType.ITEM -> {
                            val itemId = selectedItemId ?: return@Button
                            onCreateItemGoal(itemId, parsedAmount)
                            onDismiss()
                        }

                        GoalTargetType.OUTFIT -> {
                            val outfitId = selectedOutfitId ?: return@Button
                            onCreateOutfitGoal(outfitId, parsedAmount)
                            onDismiss()
                        }
                    }
                },
                enabled = canSubmit,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("goal_create_submit_button"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Savings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.goal_create_submit),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDepositSheet(
    goal: ResolvedSavingsGoal,
    onDismiss: () -> Unit,
    onAddDeposit: (amountRub: Long) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountInput by remember { mutableStateOf("") }
    val parsedAmount = amountInput.toLongOrNull() ?: 0L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.goal_deposit_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = goal.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "${formatRub(goal.savedAmountRub)} / ${formatRub(goal.targetAmountRub)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "${goal.progressPercent}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { goal.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                    )
                }
            }

            if (goal.remainingRub > 0L) {
                OutlinedButton(
                    onClick = {
                        onAddDeposit(goal.remainingRub)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("goal_fill_remaining_button"),
                ) {
                    Text(
                        text = stringResource(R.string.goal_deposit_fill_remaining, formatRub(goal.remainingRub)),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(500L, 1_000L, 2_500L, 5_000L, 10_000L).forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { amountInput = preset.toString() },
                    ) {
                        Text(
                            text = "+${formatRub(preset)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amountInput,
                onValueChange = { raw ->
                    amountInput = raw.filter { it.isDigit() }.take(9)
                },
                label = { Text(stringResource(R.string.goal_deposit_custom_label)) },
                placeholder = { Text(stringResource(R.string.goal_deposit_custom_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_deposit_amount_input"),
            )

            Button(
                onClick = {
                    if (parsedAmount > 0L) {
                        onAddDeposit(parsedAmount)
                        onDismiss()
                    }
                },
                enabled = parsedAmount > 0L,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("goal_deposit_submit_button"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.goal_deposit_submit),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalSheet(
    goal: ResolvedSavingsGoal,
    onDismiss: () -> Unit,
    onSaveAmounts: (targetAmountRub: Long, savedAmountRub: Long) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var targetInput by remember(goal.id) { mutableStateOf(goal.targetAmountRub.toString()) }
    var savedInput by remember(goal.id) { mutableStateOf(goal.savedAmountRub.toString()) }

    val parsedTarget = targetInput.toLongOrNull() ?: 0L
    val parsedSaved = savedInput.toLongOrNull() ?: 0L
    val canSave = parsedTarget > 0L && parsedSaved >= 0L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.goal_edit_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = goal.displayTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            OutlinedTextField(
                value = targetInput,
                onValueChange = { raw -> targetInput = raw.filter { it.isDigit() }.take(9) },
                label = { Text(stringResource(R.string.goal_target_price_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_edit_target_input"),
            )

            OutlinedTextField(
                value = savedInput,
                onValueChange = { raw -> savedInput = raw.filter { it.isDigit() }.take(9) },
                label = { Text(stringResource(R.string.goal_edit_saved_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_edit_saved_input"),
            )

            Button(
                onClick = {
                    if (canSave) {
                        onSaveAmounts(parsedTarget, parsedSaved)
                        onDismiss()
                    }
                },
                enabled = canSave,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("goal_edit_save_button"),
            ) {
                Text(
                    text = stringResource(R.string.goal_save_changes),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
