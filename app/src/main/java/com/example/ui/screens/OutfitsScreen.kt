package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.domain.model.Outfit
import com.example.ui.DesignerFeedUiState
import com.example.ui.ResolvedOutfit
import com.example.ui.components.CompatibilityScoreBanner
import com.example.ui.components.DesignerGarmentImage
import com.example.ui.components.OutfitBuilderSheet
import com.example.ui.components.OutfitSilhouetteCollage

@Composable
fun OutfitsScreen(
    uiState: DesignerFeedUiState,
    onBackToFeed: () -> Unit,
    onCreateOutfit: (name: String, itemIds: List<String>) -> Unit,
    onUpdateOutfit: (outfitId: String, name: String, itemIds: List<String>) -> Unit,
    onDeleteOutfit: (outfitId: String) -> Unit,
    onToggleFavoriteOutfit: (outfitId: String) -> Unit,
    onCreateGoalForOutfit: (ResolvedOutfit) -> Unit = {},
    onItemClick: (DesignerItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackToFeed)

    var showBuilderSheet by remember { mutableStateOf(false) }
    var editingOutfit by remember { mutableStateOf<Outfit?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp)
                .testTag("outfits_screen"),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.outfits_header),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.8.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Button(
                    onClick = {
                        editingOutfit = null
                        showBuilderSheet = true
                    },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("create_outfit_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.outfits_create_btn),
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
                if (uiState.outfits.isEmpty()) {
                    item(key = "outfits_empty") {
                        EmptyOutfitsCard(
                            onCreateClick = {
                                editingOutfit = null
                                showBuilderSheet = true
                            },
                        )
                    }
                } else {
                    items(uiState.outfits, key = { it.id }) { resolved ->
                        val isFavorite = uiState.favoriteOutfitId == resolved.id
                        OutfitCard(
                            resolved = resolved,
                            isFavorite = isFavorite,
                            onToggleFavorite = { onToggleFavoriteOutfit(resolved.id) },
                            onEdit = {
                                editingOutfit = resolved.outfit
                                showBuilderSheet = true
                            },
                            onDelete = { onDeleteOutfit(resolved.id) },
                            onCreateGoal = { onCreateGoalForOutfit(resolved) },
                            onItemClick = onItemClick,
                        )
                    }
                }
            }
        }
    }

    if (showBuilderSheet) {
        OutfitBuilderSheet(
            allItems = uiState.allItems,
            likedIds = uiState.likedIds,
            editingOutfit = editingOutfit,
            onDismiss = {
                showBuilderSheet = false
                editingOutfit = null
            },
            onSaveOutfit = { name, itemIds ->
                val target = editingOutfit
                if (target == null) {
                    onCreateOutfit(name, itemIds)
                } else {
                    onUpdateOutfit(target.id, name, itemIds)
                }
            },
        )
    }
}

@Composable
private fun OutfitCard(
    resolved: ResolvedOutfit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCreateGoal: () -> Unit,
    onItemClick: (DesignerItem) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (isFavorite) 2.dp else 1.dp,
            color = if (isFavorite) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("outfit_card_${resolved.id}"),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resolved.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.outfit_items_count, resolved.items.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("outfit_edit_${resolved.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.outfit_edit_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("outfit_delete_${resolved.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(R.string.outfit_delete_cd),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            var showSilhouette by remember(resolved.id) { mutableStateOf(true) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    true to stringResource(R.string.outfit_view_silhouette),
                    false to stringResource(R.string.outfit_view_strip),
                ).forEach { (isSilhouetteTab, label) ->
                    val active = showSilhouette == isSilhouetteTab
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (active) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { showSilhouette = isSilhouetteTab }
                            .testTag(
                                if (isSilhouetteTab) {
                                    "outfit_tab_silhouette_${resolved.id}"
                                } else {
                                    "outfit_tab_strip_${resolved.id}"
                                },
                            ),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        )
                    }
                }
            }

            if (showSilhouette) {
                OutfitSilhouetteCollage(
                    items = resolved.items,
                    onItemClick = onItemClick,
                )
            } else {
                // Item Thumbnails Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    resolved.items.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .width(108.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onItemClick(item) },
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .background(Color.White),
                                ) {
                                    DesignerGarmentImage(
                                        item = item,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                    )
                                }
                                Column(modifier = Modifier.padding(8.dp)) {
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
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Compatibility Indicator
            CompatibilityScoreBanner(
                compatibility = resolved.compatibility,
                compact = true,
            )

            // Favorite Outfit & Create Savings Goal Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = onToggleFavorite,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("outfit_favorite_${resolved.id}"),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(
                            if (isFavorite) R.string.outfit_is_favorite else R.string.outfit_set_favorite,
                        ),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                OutlinedButton(
                    onClick = onCreateGoal,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("outfit_goal_${resolved.id}"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Savings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.outfit_set_goal),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyOutfitsCard(
    onCreateClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_outfits_card"),
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
                    imageVector = Icons.Filled.Checkroom,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }

            Text(
                text = stringResource(R.string.outfits_empty_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.outfits_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Button(
                onClick = onCreateClick,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("empty_create_outfit_button"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.outfits_empty_cta),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
