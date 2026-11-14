package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.UserProfile
import com.example.ui.theme.ProfileGradientPreset
import com.example.ui.theme.ProfilePalette
import java.io.File

private val AVATAR_EMOJI_PRESETS = listOf("⚡", "🔥", "💎", "🦇", "🕶️", "❄️")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCustomizeSheet(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onPickAvatarUri: (Uri) -> Unit,
    onClearAvatarPhoto: () -> Unit,
    onPickCoverUri: (Uri) -> Unit,
    onClearCoverPhoto: () -> Unit,
    onSave: (
        displayName: String,
        username: String,
        bio: String,
        avatarEmoji: String?,
        gradientStartArgb: Long,
        gradientEndArgb: Long,
        useGradientBackground: Boolean,
    ) -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var username by remember(profile.username) { mutableStateOf(profile.username) }
    var bio by remember(profile.bio) { mutableStateOf(profile.bio) }
    var selectedEmoji by remember(profile.avatarEmoji) { mutableStateOf(profile.avatarEmoji) }
    var gradientStartArgb by remember(profile.gradientStartArgb) {
        mutableLongStateOf(profile.gradientStartArgb)
    }
    var gradientEndArgb by remember(profile.gradientEndArgb) {
        mutableLongStateOf(profile.gradientEndArgb)
    }
    var useGradientBg by remember(profile.useGradientBackground) {
        mutableStateOf(profile.useGradientBackground)
    }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            selectedEmoji = null
            onPickAvatarUri(uri)
        }
    }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            onPickCoverUri(uri)
        }
    }

    val previewTopTint = ProfilePalette.surfaceTopTint(gradientStartArgb)
    val previewBottomTint = ProfilePalette.surfaceBottomTint(gradientEndArgb)
    val previewBannerColors = ProfilePalette.bannerColors(gradientStartArgb, gradientEndArgb)
    val hasCoverFile = !profile.coverFilePath.isNullOrBlank() && File(profile.coverFilePath).exists()

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
                text = stringResource(R.string.profile_edit_title),
                style = MaterialTheme.typography.headlineSmall,
            )

            // Live Profile Card Preview
            Surface(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (useGradientBg) {
                                Modifier.background(
                                    Brush.verticalGradient(listOf(previewTopTint, previewBottomTint)),
                                )
                            } else {
                                Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                            },
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(86.dp)
                            .background(Brush.linearGradient(previewBannerColors)),
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
                    }

                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .offset(y = (-28).dp),
                    ) {
                        ProfileAvatarCircle(
                            avatarFilePath = profile.avatarFilePath,
                            avatarEmoji = selectedEmoji,
                            displayName = displayName,
                            ringColor = if (useGradientBg) {
                                previewTopTint
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            },
                            accentColor = Color(gradientStartArgb),
                            size = 68.dp,
                        )
                    }

                    val previewTextColor = if (useGradientBg) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .offset(y = (-16).dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = displayName.ifBlank { profile.displayName },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = previewTextColor,
                        )
                        Text(
                            text = username.ifBlank { profile.username },
                            style = MaterialTheme.typography.labelMedium,
                            color = previewTextColor.copy(alpha = 0.78f),
                        )
                    }
                }
            }

            // Cover Photo Picker + Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        coverPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("profile_pick_cover_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_change_cover))
                }

                if (hasCoverFile) {
                    OutlinedButton(
                        onClick = onClearCoverPhoto,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("profile_clear_cover_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(R.string.profile_reset_cover),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // Avatar Photo Picker + Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        avatarPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("profile_pick_photo_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_change_avatar))
                }

                if (profile.avatarFilePath != null) {
                    OutlinedButton(
                        onClick = onClearAvatarPhoto,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("profile_clear_photo_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(R.string.profile_reset_avatar),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // Quick emoji avatar badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AVATAR_EMOJI_PRESETS.forEach { emoji ->
                    val isSelected = selectedEmoji == emoji
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = if (isSelected) {
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            null
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                selectedEmoji = if (isSelected) null else emoji
                            },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text(stringResource(R.string.profile_name_label)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_edit_name_input"),
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.profile_username_label)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_edit_username_input"),
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text(stringResource(R.string.profile_bio_label)) },
                maxLines = 2,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_edit_bio_input"),
            )

            // Gradient Presets + Custom Two-Color Picker
            Text(
                text = stringResource(R.string.profile_gradient_section),
                style = MaterialTheme.typography.titleSmall,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProfileGradientPreset.entries.forEach { preset ->
                    val isSelected = gradientStartArgb == preset.startArgb &&
                        gradientEndArgb == preset.endArgb
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .width(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                gradientStartArgb = preset.startArgb
                                gradientEndArgb = preset.endArgb
                            }
                            .padding(4.dp)
                            .testTag("gradient_preset_${preset.id.lowercase()}"),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        ProfilePalette.bannerColors(preset.startArgb, preset.endArgb),
                                    ),
                                )
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape,
                                        )
                                    } else {
                                        Modifier
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        Text(
                            text = preset.title,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            ColorPickerRow(
                label = stringResource(R.string.profile_gradient_color1),
                selectedArgb = gradientStartArgb,
                onSelectArgb = { gradientStartArgb = it },
                tagPrefix = "color1",
            )

            ColorPickerRow(
                label = stringResource(R.string.profile_gradient_color2),
                selectedArgb = gradientEndArgb,
                onSelectArgb = { gradientEndArgb = it },
                tagPrefix = "color2",
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.profile_gradient_bg_toggle),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = useGradientBg,
                    onCheckedChange = { useGradientBg = it },
                    modifier = Modifier.testTag("profile_gradient_bg_switch"),
                )
            }

            Button(
                onClick = {
                    onSave(
                        displayName,
                        username,
                        bio,
                        selectedEmoji,
                        gradientStartArgb,
                        gradientEndArgb,
                        useGradientBg,
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("profile_save_button"),
            ) {
                Text(
                    text = stringResource(R.string.profile_save_btn),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ColorPickerRow(
    label: String,
    selectedArgb: Long,
    onSelectArgb: (Long) -> Unit,
    tagPrefix: String,
) {
    val activeColor = Color(selectedArgb)
    var sliderHue by remember(selectedArgb) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(activeColor.toArgb(), hsv)
        mutableFloatStateOf(hsv[0])
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(activeColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProfilePalette.swatchColors.forEachIndexed { idx, argb ->
                val isSelected = selectedArgb == argb
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onSelectArgb(argb) }
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(Color(argb))
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 2.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape,
                                )
                            } else {
                                Modifier.border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape,
                                )
                            },
                        )
                        .testTag("${tagPrefix}_swatch_$idx"),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        // Spectrum hue slider for arbitrary custom color selection
        Slider(
            value = sliderHue,
            onValueChange = { hue ->
                sliderHue = hue
                val customColor = Color.hsv(hue.coerceIn(0f, 360f), 0.80f, 0.90f)
                onSelectArgb(customColor.toArgb().toLong() and 0xFFFFFFFFL)
            },
            valueRange = 0f..360f,
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("${tagPrefix}_hue_slider"),
        )
    }
}

@Composable
fun ProfileAvatarCircle(
    avatarFilePath: String?,
    avatarEmoji: String?,
    displayName: String,
    ringColor: Color,
    accentColor: Color,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val initial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "G"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(accentColor)
            .border(4.dp, ringColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            !avatarFilePath.isNullOrBlank() && File(avatarFilePath).exists() -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(avatarFilePath))
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.profile_avatar_cd),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            !avatarEmoji.isNullOrBlank() -> {
                Text(
                    text = avatarEmoji,
                    fontSize = (size.value * 0.42f).sp,
                )
            }
            else -> {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}
