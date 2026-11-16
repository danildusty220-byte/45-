package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.OutfitCompatibilityScorer
import com.example.domain.model.CompatibilityVerdict
import com.example.domain.model.DesignerItem
import com.example.domain.model.GarmentType
import com.example.domain.model.Outfit
import com.example.domain.model.OutfitCompatibility

@Composable
fun compatibilityAccentColor(verdict: CompatibilityVerdict): Color = when (verdict) {
    CompatibilityVerdict.IDEAL -> Color(0xFF10B981)
    CompatibilityVerdict.GOOD -> MaterialTheme.colorScheme.primary
    CompatibilityVerdict.BOLD -> Color(0xFFF59E0B)
    CompatibilityVerdict.CLASH -> MaterialTheme.colorScheme.error
    CompatibilityVerdict.PENDING -> MaterialTheme.colorScheme.outline
}

@Composable
fun compatibilityVerdictLabel(verdict: CompatibilityVerdict): String = when (verdict) {
    CompatibilityVerdict.IDEAL -> stringResource(R.string.compatibility_verdict_ideal)
    CompatibilityVerdict.GOOD -> stringResource(R.string.compatibility_verdict_good)
    CompatibilityVerdict.BOLD -> stringResource(R.string.compatibility_verdict_bold)
    CompatibilityVerdict.CLASH -> stringResource(R.string.compatibility_verdict_clash)
    CompatibilityVerdict.PENDING -> stringResource(R.string.compatibility_verdict_pending)
}

@Composable
fun CompatibilityScoreBanner(
    compatibility: OutfitCompatibility,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val targetAccent = compatibilityAccentColor(compatibility.verdict)
    val accentColor by animateColorAsState(targetValue = targetAccent, label = "compatAccent")
    val animatedScore by animateIntAsState(
        targetValue = compatibility.score,
        animationSpec = spring(),
        label = "compatScore",
    )
    val animatedProgress by animateFloatAsState(
        targetValue = (compatibility.score / 100f).coerceIn(0f, 1f),
        animationSpec = spring(),
        label = "compatProgress",
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = accentColor.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.compatibility_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = compatibilityVerdictLabel(compatibility.verdict),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor,
                ) {
                    Text(
                        text = if (compatibility.verdict == CompatibilityVerdict.PENDING) {
                            "—"
                        } else {
                            "$animatedScore%"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("compatibility_score_badge"),
                    )
                }
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.18f),
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50)),
            )

            Text(
                text = compatibility.summaryReason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!compact && compatibility.verdict != CompatibilityVerdict.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CompatibilitySubMetric(
                        label = stringResource(R.string.compatibility_color),
                        value = compatibility.colorScore,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f),
                    )
                    CompatibilitySubMetric(
                        label = stringResource(R.string.compatibility_silhouette),
                        value = compatibility.silhouetteScore,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f),
                    )
                    CompatibilitySubMetric(
                        label = stringResource(R.string.compatibility_season),
                        value = compatibility.seasonScore,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CompatibilitySubMetric(
    label: String,
    value: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$value%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            color = accentColor,
            trackColor = accentColor.copy(alpha = 0.16f),
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(50)),
        )
    }
}

private enum class BuilderFilter {
    ALL,
    LIKED,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitBuilderSheet(
    allItems: List<DesignerItem>,
    likedIds: Set<String>,
    editingOutfit: Outfit?,
    onDismiss: () -> Unit,
    onSaveOutfit: (name: String, itemIds: List<String>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var outfitName by remember(editingOutfit?.id) { mutableStateOf(editingOutfit?.name.orEmpty()) }
    val selectedIds = remember(editingOutfit?.id) {
        mutableStateListOf<String>().apply {
            editingOutfit?.itemIds?.let { addAll(it) }
        }
    }
    var builderFilter by remember { mutableStateOf(BuilderFilter.ALL) }
    var selectedCategory by remember { mutableStateOf<GarmentType?>(null) }
    var showSilhouettePreview by remember { mutableStateOf(false) }
    var slotConflictMessage by remember { mutableStateOf<String?>(null) }

    val itemsById = remember(allItems) { allItems.associateBy { it.id } }
    val selectedItems by remember {
        derivedStateOf { selectedIds.mapNotNull { itemsById[it] } }
    }
    val requirementsStatus by remember {
        derivedStateOf { OutfitCompatibilityScorer.validateRequirements(selectedItems) }
    }
    val liveCompatibility by remember {
        derivedStateOf { OutfitCompatibilityScorer.evaluate(selectedItems) }
    }

    val filteredCatalog by remember(allItems, likedIds, builderFilter, selectedCategory) {
        derivedStateOf {
            allItems.filter { item ->
                val likedOk = builderFilter == BuilderFilter.ALL || item.id in likedIds
                val categoryOk = selectedCategory == null || item.type == selectedCategory
                likedOk && categoryOk
            }
        }
    }

    val availableCategories = remember(allItems) { allItems.map { it.type }.distinct() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .navigationBarsPadding(),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(
                            if (editingOutfit == null) {
                                R.string.outfit_builder_create_title
                            } else {
                                R.string.outfit_builder_edit_title
                            },
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        // Live realistic mannequin dressing up in real time as user picks clothes
                        OutfitSilhouetteCollage(
                            items = selectedItems,
                            onItemClick = { clickedItem ->
                                selectedIds.remove(clickedItem.id)
                                slotConflictMessage = null
                            },
                            mini = true,
                            modifier = Modifier
                                .width(136.dp)
                                .clickable { showSilhouettePreview = !showSilhouettePreview },
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = outfitName,
                                onValueChange = { outfitName = it },
                                label = { Text(stringResource(R.string.outfit_name_label)) },
                                placeholder = { Text(stringResource(R.string.outfit_name_placeholder)) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("outfit_name_input"),
                            )

                            CompatibilityScoreBanner(
                                compatibility = liveCompatibility,
                                compact = true,
                                modifier = Modifier.testTag("outfit_builder_compatibility"),
                            )

                            Text(
                                text = stringResource(R.string.outfit_requirements_title),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                RequirementPill(
                                    label = stringResource(R.string.outfit_req_sneakers),
                                    satisfied = requirementsStatus.hasSneakers,
                                    onClick = {
                                        showSilhouettePreview = false
                                        builderFilter = BuilderFilter.ALL
                                        selectedCategory = GarmentType.SNEAKERS
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("outfit_req_sneakers"),
                                )
                                RequirementPill(
                                    label = stringResource(R.string.outfit_req_bottom),
                                    satisfied = requirementsStatus.hasBottom,
                                    onClick = {
                                        showSilhouettePreview = false
                                        builderFilter = BuilderFilter.ALL
                                        selectedCategory = if (allItems.any { it.type == GarmentType.SHORTS }) {
                                            if (selectedCategory == GarmentType.JEANS) GarmentType.SHORTS else GarmentType.JEANS
                                        } else {
                                            GarmentType.JEANS
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("outfit_req_bottom"),
                                )
                                RequirementPill(
                                    label = stringResource(R.string.outfit_req_top),
                                    satisfied = requirementsStatus.hasTop,
                                    onClick = {
                                        showSilhouettePreview = false
                                        builderFilter = BuilderFilter.ALL
                                        selectedCategory = if (allItems.any { it.type == GarmentType.HOODIE }) {
                                            if (selectedCategory == GarmentType.TSHIRT) GarmentType.HOODIE else GarmentType.TSHIRT
                                        } else {
                                            GarmentType.TSHIRT
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("outfit_req_top"),
                                )
                            }
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = !showSilhouettePreview,
                            onClick = { showSilhouettePreview = false },
                            label = { Text(stringResource(R.string.outfit_builder_tab_pick)) },
                            modifier = Modifier.testTag("builder_tab_pick"),
                        )
                        FilterChip(
                            selected = showSilhouettePreview,
                            onClick = { showSilhouettePreview = true },
                            label = {
                                Text(stringResource(R.string.outfit_builder_tab_silhouette, selectedItems.size))
                            },
                            modifier = Modifier.testTag("builder_tab_silhouette"),
                        )
                    }
                }

                if (selectedItems.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            selectedItems.forEach { selectedItem ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .heightIn(min = 40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedIds.remove(selectedItem.id)
                                            slotConflictMessage = null
                                        },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            text = selectedItem.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.width(120.dp),
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (showSilhouettePreview) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        OutfitSilhouetteCollage(
                            items = selectedItems,
                            onItemClick = { clickedItem ->
                                selectedIds.remove(clickedItem.id)
                                slotConflictMessage = null
                            },
                        )
                    }
                } else {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.outfit_pick_items_hint, selectedIds.size),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                FilterChip(
                                    selected = builderFilter == BuilderFilter.ALL && selectedCategory == null,
                                    onClick = {
                                        builderFilter = BuilderFilter.ALL
                                        selectedCategory = null
                                    },
                                    label = { Text(stringResource(R.string.outfit_filter_all)) },
                                )
                                FilterChip(
                                    selected = builderFilter == BuilderFilter.LIKED,
                                    onClick = {
                                        builderFilter = if (builderFilter == BuilderFilter.LIKED) {
                                            BuilderFilter.ALL
                                        } else {
                                            BuilderFilter.LIKED
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Favorite,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                    label = { Text(stringResource(R.string.outfit_filter_liked)) },
                                )
                                availableCategories.forEach { category ->
                                    FilterChip(
                                        selected = selectedCategory == category,
                                        onClick = {
                                            selectedCategory = if (selectedCategory == category) {
                                                null
                                            } else {
                                                category
                                            }
                                        },
                                        label = { Text(stringResource(category.labelRes)) },
                                    )
                                }
                            }
                        }
                    }

                    items(filteredCatalog, key = { it.id }) { item ->
                        val isSelected = item.id in selectedIds
                        val conflictingItem = remember(selectedItems, item, isSelected) {
                            if (isSelected) null else OutfitCompatibilityScorer.findConflictingItem(selectedItems, item)
                        }
                        val isLocked = conflictingItem != null
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                isLocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                else -> MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isLocked -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                },
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (isSelected) {
                                        selectedIds.remove(item.id)
                                        slotConflictMessage = null
                                    } else if (conflictingItem != null) {
                                        val conflictSlot = OutfitCompatibilityScorer.exclusiveSlotOf(item.type)
                                        slotConflictMessage = when (conflictSlot) {
                                            OutfitCompatibilityScorer.OutfitExclusiveSlot.BOTTOM ->
                                                context.getString(
                                                    R.string.outfit_slot_conflict_bottom,
                                                    conflictingItem.title,
                                                )

                                            OutfitCompatibilityScorer.OutfitExclusiveSlot.HEADWEAR ->
                                                context.getString(
                                                    R.string.outfit_slot_conflict_headwear,
                                                    conflictingItem.title,
                                                )

                                            else ->
                                                context.getString(
                                                    R.string.outfit_slot_conflict_generic,
                                                    conflictingItem.title,
                                                )
                                        }
                                    } else {
                                        selectedIds.add(item.id)
                                        slotConflictMessage = null
                                    }
                                }
                                .testTag("outfit_pick_item_${item.id}"),
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.95f)
                                        .background(Color.White),
                                ) {
                                    DesignerGarmentImage(
                                        item = item,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                            .alpha(if (isLocked) 0.50f else 1f),
                                    )
                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .size(26.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            }
                                        }
                                    } else if (isLocked) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp),
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Lock,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.size(12.dp),
                                                )
                                                Text(
                                                    text = stringResource(R.string.outfit_slot_locked_badge),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                )
                                            }
                                        }
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .alpha(if (isLocked) 0.65f else 1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        text = item.brand.displayName.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    slotConflictMessage?.let { conflictMsg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("outfit_slot_conflict_banner"),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = conflictMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(
                                    onClick = { slotConflictMessage = null },
                                    modifier = Modifier.size(28.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                    if (!requirementsStatus.isComplete) {
                        Text(
                            text = stringResource(R.string.outfit_requirements_missing_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("outfit_requirements_hint"),
                        )
                    }
                    Button(
                        onClick = {
                            onSaveOutfit(outfitName, selectedIds.toList())
                            onDismiss()
                        },
                        enabled = requirementsStatus.isComplete,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("outfit_save_button"),
                    ) {
                        Text(
                            text = stringResource(R.string.outfit_save_btn),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequirementPill(
    label: String,
    satisfied: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeColor = Color(0xFF10B981)
    Surface(
        shape = RoundedCornerShape(50),
        color = if (satisfied) {
            activeColor.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (satisfied) activeColor else MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = modifier
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = if (satisfied) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = if (satisfied) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (satisfied) FontWeight.Bold else FontWeight.Medium,
                color = if (satisfied) activeColor else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
