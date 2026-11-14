package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.DesignerCatalog
import com.example.data.UserProfile
import com.example.domain.model.DesignerItem
import com.example.ui.DesignerFeedUiState
import com.example.ui.components.DesignerGarmentImage
import com.example.ui.components.ProfileAvatarCircle
import com.example.ui.components.ProfileCustomizeSheet
import com.example.ui.theme.ProfilePalette
import java.io.File

@Composable
fun ProfileScreen(
    uiState: DesignerFeedUiState,
    onBackToFeed: () -> Unit,
    onSaveProfile: (
        displayName: String,
        username: String,
        bio: String,
        avatarEmoji: String?,
        gradientStartArgb: Long,
        gradientEndArgb: Long,
        useGradientBackground: Boolean,
    ) -> Unit,
    onPickAvatarUri: (Uri) -> Unit,
    onClearAvatarPhoto: () -> Unit,
    onPickCoverUri: (Uri) -> Unit,
    onClearCoverPhoto: () -> Unit,
    onItemClick: (DesignerItem) -> Unit,
    onRemoveLike: (String) -> Unit,
    onClearAllLikes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackToFeed)

    var showCustomizeSheet by remember { mutableStateOf(false) }
    val profile = uiState.userProfile
    val topTint = ProfilePalette.surfaceTopTint(profile.gradientStartArgb)

    val screenModifier = if (profile.useGradientBackground) {
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        topTint.copy(alpha = 0.24f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            )
    } else {
        modifier.fillMaxSize()
    }

    BoxWithConstraints(
        modifier = screenModifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 156.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 116.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp)
                .testTag("profile_screen"),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                DiscordProfileCard(
                    profile = profile,
                    likedCount = uiState.likedItems.size,
                    catalogCount = uiState.allItems.size,
                    onOpenCustomize = { showCustomizeSheet = true },
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.profile_favorites_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (uiState.likedItems.isNotEmpty()) {
                        TextButton(
                            onClick = onClearAllLikes,
                            modifier = Modifier.testTag("profile_clear_likes_button"),
                        ) {
                            Text(stringResource(R.string.profile_clear_likes))
                        }
                    }
                }
            }

            if (uiState.likedItems.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp),
                            )
                            Text(
                                text = stringResource(R.string.profile_favorites_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            } else {
                items(uiState.likedItems, key = { "fav_${it.id}" }) { item ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onItemClick(item) }
                            .testTag("profile_liked_item_${item.id}"),
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.85f),
                            ) {
                                DesignerGarmentImage(
                                    item = item,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                IconButton(
                                    onClick = { onRemoveLike(item.id) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .minimumInteractiveComponentSize()
                                        .testTag("profile_remove_liked_${item.id}"),
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = stringResource(R.string.liked_remove_cd),
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .size(16.dp),
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.padding(10.dp),
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
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomizeSheet) {
        ProfileCustomizeSheet(
            profile = profile,
            onDismiss = { showCustomizeSheet = false },
            onPickAvatarUri = onPickAvatarUri,
            onClearAvatarPhoto = onClearAvatarPhoto,
            onPickCoverUri = onPickCoverUri,
            onClearCoverPhoto = onClearCoverPhoto,
            onSave = onSaveProfile,
        )
    }
}

@Composable
private fun DiscordProfileCard(
    profile: UserProfile,
    likedCount: Int,
    catalogCount: Int,
    onOpenCustomize: () -> Unit,
) {
    val context = LocalContext.current
    val useDarkCard = profile.useGradientBackground
    val topTint = ProfilePalette.surfaceTopTint(profile.gradientStartArgb)
    val bottomTint = ProfilePalette.surfaceBottomTint(profile.gradientEndArgb)
    val bannerColors = ProfilePalette.bannerColors(profile.gradientStartArgb, profile.gradientEndArgb)
    val cardTextColor = if (useDarkCard) Color.White else MaterialTheme.colorScheme.onSurface
    val mutedTextColor = if (useDarkCard) {
        Color.White.copy(alpha = 0.78f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val ringColor = if (useDarkCard) topTint else MaterialTheme.colorScheme.surface
    val hasCoverFile = !profile.coverFilePath.isNullOrBlank() && File(profile.coverFilePath).exists()

    Surface(
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("discord_profile_card"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (useDarkCard) {
                        Modifier.background(
                            Brush.verticalGradient(listOf(topTint, bottomTint)),
                        )
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.surface)
                    },
                ),
        ) {
            // Custom Cover Photo or Gradient Banner + Edit Profile Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(134.dp)
                    .background(Brush.linearGradient(bannerColors)),
            ) {
                if (hasCoverFile) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(profile.coverFilePath!!))
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.profile_cover_cd),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.45f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onOpenCustomize)
                        .testTag("profile_customize_button"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.profile_edit_btn),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Overlapping Avatar + Name/Bio + Stats
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 18.dp),
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = (-42).dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenCustomize)
                        .testTag("profile_avatar_tap"),
                ) {
                    ProfileAvatarCircle(
                        avatarFilePath = profile.avatarFilePath,
                        avatarEmoji = profile.avatarEmoji,
                        displayName = profile.displayName,
                        ringColor = ringColor,
                        accentColor = Color(profile.gradientStartArgb),
                        size = 86.dp,
                    )
                }

                Column(
                    modifier = Modifier.offset(y = (-26).dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = profile.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = cardTextColor,
                        modifier = Modifier.testTag("profile_display_name"),
                    )

                    Text(
                        text = profile.username,
                        style = MaterialTheme.typography.labelLarge,
                        color = mutedTextColor,
                    )

                    if (profile.bio.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = mutedTextColor,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatMiniCard(
                        value = likedCount.toString(),
                        label = stringResource(R.string.profile_stat_likes),
                        containerColor = cardTextColor.copy(alpha = 0.1f),
                        textColor = cardTextColor,
                        mutedColor = mutedTextColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_stat_likes"),
                    )
                    StatMiniCard(
                        value = catalogCount.toString(),
                        label = stringResource(R.string.profile_stat_catalog),
                        containerColor = cardTextColor.copy(alpha = 0.1f),
                        textColor = cardTextColor,
                        mutedColor = mutedTextColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_stat_catalog"),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatMiniCard(
    value: String,
    label: String,
    containerColor: Color,
    textColor: Color,
    mutedColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = mutedColor,
            )
        }
    }
}
