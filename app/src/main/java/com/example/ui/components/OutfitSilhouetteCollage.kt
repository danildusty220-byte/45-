package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.domain.model.DesignerItem
import com.example.domain.model.GarmentType

private val RAISED_HOOD_JACKET_IDS = setOf(
    "supreme_fox_racing_puffer_red",
    "stone_island_supreme_hand_dyed_puffer",
    "moncler_maya_puffer_black",
    "cp_company_dd_shell_goggle_puffer",
)

private val RAISED_HOOD_HOODIE_IDS = setOf(
    "offwhite_hoodie_arrows_cream",
    "amiri_bandana_paint_hoodie_black",
    "chrome_hearts_matty_boy_blue",
    "chrome_hearts_multicolor_cross_black",
)

private data class SilhouetteGroups(
    val headwear: List<DesignerItem>,
    val outerwear: List<DesignerItem>,
    val tops: List<DesignerItem>,
    val belts: List<DesignerItem>,
    val bottoms: List<DesignerItem>,
    val backpacks: List<DesignerItem>,
    val footwear: List<DesignerItem>,
)

@Composable
fun OutfitSilhouetteCollage(
    items: List<DesignerItem>,
    onItemClick: (DesignerItem) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    mini: Boolean = false,
) {
    val groups = remember(items) {
        SilhouetteGroups(
            headwear = items.filter { it.type == GarmentType.CAP || it.type == GarmentType.BEANIE },
            outerwear = items.filter { it.type == GarmentType.JACKET },
            tops = items.filter { it.type == GarmentType.HOODIE || it.type == GarmentType.TSHIRT },
            belts = items.filter { it.type == GarmentType.BELT },
            bottoms = items.filter { it.type == GarmentType.JEANS || it.type == GarmentType.SHORTS },
            backpacks = items.filter { it.type == GarmentType.BACKPACK },
            footwear = items.filter { it.type == GarmentType.SNEAKERS },
        )
    }

    val stageHeight = when {
        mini -> 228.dp
        compact -> 336.dp
        else -> 408.dp
    }

    Surface(
        shape = RoundedCornerShape(if (mini) 18.dp else 20.dp),
        color = Color(0xFFF3F1ED),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .then(if (mini) Modifier else Modifier.fillMaxWidth())
            .testTag("outfit_silhouette_collage"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(stageHeight)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFAF9F7),
                                Color(0xFFEBE8E2),
                            ),
                        ),
                    ),
            ) {
                val stageW = maxWidth
                val stageH = maxHeight

                // Anatomical Studio Mannequin Silhouette
                MannequinFigureCanvas(
                    modifier = Modifier.fillMaxSize(),
                )

                if (items.isEmpty() && !mini) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Checkroom,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = stringResource(R.string.outfit_silhouette_empty),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                } else {
                    // 1. Backpack slung beside / behind right shoulder
                    groups.backpacks.firstOrNull()?.let { bag ->
                        val bagW = stageW * (if (mini) 0.38f else 0.28f)
                        val bagH = stageH * 0.26f
                        val xOffset = stageW * 0.5f + stageW * (if (mini) 0.06f else 0.09f)
                        val yOffset = stageH * 0.21f
                        MannequinGarmentLayer(
                            item = bag,
                            width = bagW,
                            height = bagH,
                            xOffset = xOffset,
                            yOffset = yOffset,
                            zIndex = 0.5f,
                            onClick = { onItemClick(bag) },
                        )
                    }

                    // 2. Bottoms: Full-length upright Jeans or above-knee Shorts — strictly 1 bottom
                    groups.bottoms.firstOrNull()?.let { bottom ->
                        val isShorts = bottom.type == GarmentType.SHORTS
                        val bottomW = stageW * (if (mini) 0.62f else 0.46f)
                        val bottomH = stageH * (if (isShorts) 0.24f else 0.445f)
                        val xOffset = (stageW - bottomW) / 2f
                        val yOffset = stageH * 0.430f
                        MannequinGarmentLayer(
                            item = bottom,
                            width = bottomW,
                            height = bottomH,
                            xOffset = xOffset,
                            yOffset = yOffset,
                            zIndex = 1.2f,
                            onClick = { onItemClick(bottom) },
                        )
                    }

                    // 3. Footwear pair (2 shoes cleanly framed at mannequin ankles / feet)
                    groups.footwear.firstOrNull()?.let { shoe ->
                        val shoeW = stageW * (if (mini) 0.44f else 0.34f)
                        val shoeH = stageH * 0.165f
                        val xOffset = (stageW - shoeW) / 2f
                        val yOffset = stageH * 0.775f
                        MannequinGarmentLayer(
                            item = shoe,
                            width = shoeW,
                            height = shoeH,
                            xOffset = xOffset,
                            yOffset = yOffset,
                            zIndex = 1.8f,
                            onClick = { onItemClick(shoe) },
                        )
                    }

                    // 4. Layered Upper Torso: Jacket > Hoodie > T-shirt (only outermost layer shows)
                    val jacket = groups.outerwear.firstOrNull()
                    val hoodie = groups.tops.firstOrNull { it.type == GarmentType.HOODIE }
                    val tshirt = groups.tops.firstOrNull { it.type == GarmentType.TSHIRT }
                    val visibleTorsoItem = jacket ?: hoodie ?: tshirt
                    visibleTorsoItem?.let { torsoItem ->
                        when (torsoItem.type) {
                            GarmentType.JACKET -> {
                                val hasRaisedHood = torsoItem.id in RAISED_HOOD_JACKET_IDS
                                val jacketW = stageW * when {
                                    hasRaisedHood && mini -> 0.84f
                                    hasRaisedHood -> 0.64f
                                    mini -> 0.72f
                                    else -> 0.54f
                                }
                                val jacketH = stageH * (if (hasRaisedHood) 0.445f else 0.34f)
                                val yOffset = stageH * (if (hasRaisedHood) 0.045f else 0.15f)
                                MannequinGarmentLayer(
                                    item = torsoItem,
                                    width = jacketW,
                                    height = jacketH,
                                    xOffset = (stageW - jacketW) / 2f,
                                    yOffset = yOffset,
                                    zIndex = 2.6f,
                                    onClick = { onItemClick(torsoItem) },
                                )
                            }

                            GarmentType.HOODIE -> {
                                val hasRaisedHood = torsoItem.id in RAISED_HOOD_HOODIE_IDS
                                val topW = stageW * when {
                                    hasRaisedHood && mini -> 0.80f
                                    hasRaisedHood -> 0.62f
                                    mini -> 0.70f
                                    else -> 0.52f
                                }
                                val topH = stageH * (if (hasRaisedHood) 0.425f else 0.335f)
                                val yOffset = stageH * (if (hasRaisedHood) 0.055f else 0.148f)
                                MannequinGarmentLayer(
                                    item = torsoItem,
                                    width = topW,
                                    height = topH,
                                    xOffset = (stageW - topW) / 2f,
                                    yOffset = yOffset,
                                    zIndex = 2.2f,
                                    onClick = { onItemClick(torsoItem) },
                                )
                            }

                            else -> {
                                val topW = stageW * (if (mini) 0.66f else 0.48f)
                                val topH = stageH * 0.29f
                                MannequinGarmentLayer(
                                    item = torsoItem,
                                    width = topW,
                                    height = topH,
                                    xOffset = (stageW - topW) / 2f,
                                    yOffset = stageH * 0.175f,
                                    zIndex = 2.0f,
                                    onClick = { onItemClick(torsoItem) },
                                )
                            }
                        }
                    }

                    // 5. Belt strapped across waist line
                    groups.belts.firstOrNull()?.let { belt ->
                        val beltW = stageW * (if (mini) 0.34f else 0.24f)
                        val beltH = stageH * 0.085f
                        val xOffset = (stageW - beltW) / 2f
                        val yOffset = stageH * 0.42f
                        MannequinGarmentLayer(
                            item = belt,
                            width = beltW,
                            height = beltH,
                            xOffset = xOffset,
                            yOffset = yOffset,
                            zIndex = 3.2f,
                            onClick = { onItemClick(belt) },
                        )
                    }

                    // 6. Headwear perched on mannequin head
                    groups.headwear.firstOrNull()?.let { hat ->
                        val hatW = stageW * (if (mini) 0.30f else 0.22f)
                        val hatH = stageH * 0.125f
                        val xOffset = (stageW - hatW) / 2f
                        val yOffset = stageH * 0.015f
                        MannequinGarmentLayer(
                            item = hat,
                            width = hatW,
                            height = hatH,
                            xOffset = xOffset,
                            yOffset = yOffset,
                            zIndex = 3.6f,
                            onClick = { onItemClick(hat) },
                        )
                    }
                }
            }

            // Compact clickable breakdown pills under full/compact mannequin so all layered pieces stay accessible
            if (!mini && items.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onItemClick(item) },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .padding(2.dp),
                                ) {
                                    DesignerGarmentImage(
                                        item = item,
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                                Text(
                                    text = item.brand.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MannequinGarmentLayer(
    item: DesignerItem,
    width: Dp,
    height: Dp,
    xOffset: Dp,
    yOffset: Dp,
    zIndex: Float,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .offset(x = xOffset, y = yOffset)
            .width(width)
            .height(height)
            .zIndex(zIndex)
            .clickable(onClick = onClick)
            .testTag("silhouette_item_${item.id}"),
    ) {
        DesignerGarmentImage(
            item = item,
            contentDescription = "${item.brand.displayName} ${item.title}",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun MannequinFigureCanvas(
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f

        val bodyBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFDAD6CE),
                Color(0xFFCBC6BC),
                Color(0xFFBFB9AF),
            ),
        )
        val limbBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFCFCAC1),
                Color(0xFFBDB7AD),
            ),
        )
        val outlineColor = Color(0xFF9F998E)
        val stroke = Stroke(width = (w * 0.005f).coerceIn(1.5f, 3f), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Floor shadow under mannequin feet
        drawOval(
            color = Color(0xFF000000).copy(alpha = 0.12f),
            topLeft = Offset(cx - w * 0.18f, h * 0.905f),
            size = Size(w * 0.36f, h * 0.045f),
        )

        // Center subtle mannequin stand pole (subtle vertical pedestal cue near ankles)
        drawLine(
            color = Color(0xFFB4AEA4).copy(alpha = 0.45f),
            start = Offset(cx, h * 0.53f),
            end = Offset(cx, h * 0.925f),
            strokeWidth = (w * 0.01f).coerceIn(2f, 4f),
        )
        drawOval(
            color = Color(0xFFA9A398).copy(alpha = 0.55f),
            topLeft = Offset(cx - w * 0.11f, h * 0.915f),
            size = Size(w * 0.22f, h * 0.025f),
        )

        // Left & Right Arms (angled naturally beside torso)
        val leftArm = Path().apply {
            moveTo(cx - w * 0.105f, h * 0.205f)
            quadraticTo(cx - w * 0.155f, h * 0.215f, cx - w * 0.175f, h * 0.34f)
            lineTo(cx - w * 0.195f, h * 0.485f)
            quadraticTo(cx - w * 0.195f, h * 0.525f, cx - w * 0.17f, h * 0.525f)
            lineTo(cx - w * 0.152f, h * 0.48f)
            lineTo(cx - w * 0.125f, h * 0.35f)
            lineTo(cx - w * 0.095f, h * 0.245f)
            close()
        }
        val rightArm = Path().apply {
            moveTo(cx + w * 0.105f, h * 0.205f)
            quadraticTo(cx + w * 0.155f, h * 0.215f, cx + w * 0.175f, h * 0.34f)
            lineTo(cx + w * 0.195f, h * 0.485f)
            quadraticTo(cx + w * 0.195f, h * 0.525f, cx + w * 0.17f, h * 0.525f)
            lineTo(cx + w * 0.152f, h * 0.48f)
            lineTo(cx + w * 0.125f, h * 0.35f)
            lineTo(cx + w * 0.095f, h * 0.245f)
            close()
        }
        drawPath(leftArm, brush = limbBrush)
        drawPath(leftArm, color = outlineColor, style = stroke)
        drawPath(rightArm, brush = limbBrush)
        drawPath(rightArm, color = outlineColor, style = stroke)

        // Left & Right Legs + Sculpted Feet
        val leftLeg = Path().apply {
            moveTo(cx - w * 0.095f, h * 0.46f)
            lineTo(cx - w * 0.008f, h * 0.475f)
            lineTo(cx - w * 0.024f, h * 0.675f)
            quadraticTo(cx - w * 0.025f, h * 0.77f, cx - w * 0.036f, h * 0.875f)
            lineTo(cx - w * 0.015f, h * 0.91f)
            lineTo(cx - w * 0.088f, h * 0.915f)
            quadraticTo(cx - w * 0.095f, h * 0.89f, cx - w * 0.075f, h * 0.865f)
            quadraticTo(cx - w * 0.098f, h * 0.75f, cx - w * 0.092f, h * 0.65f)
            close()
        }
        val rightLeg = Path().apply {
            moveTo(cx + w * 0.095f, h * 0.46f)
            lineTo(cx + w * 0.008f, h * 0.475f)
            lineTo(cx + w * 0.024f, h * 0.675f)
            quadraticTo(cx + w * 0.025f, h * 0.77f, cx + w * 0.036f, h * 0.875f)
            lineTo(cx + w * 0.015f, h * 0.91f)
            lineTo(cx + w * 0.088f, h * 0.915f)
            quadraticTo(cx + w * 0.095f, h * 0.89f, cx + w * 0.075f, h * 0.865f)
            quadraticTo(cx + w * 0.098f, h * 0.75f, cx + w * 0.092f, h * 0.65f)
            close()
        }
        drawPath(leftLeg, brush = limbBrush)
        drawPath(leftLeg, color = outlineColor, style = stroke)
        drawPath(rightLeg, brush = limbBrush)
        drawPath(rightLeg, color = outlineColor, style = stroke)

        // Torso & Neck (anatomical chest/waist/pelvis form)
        val torso = Path().apply {
            moveTo(cx - w * 0.026f, h * 0.155f)
            lineTo(cx + w * 0.026f, h * 0.155f)
            lineTo(cx + w * 0.032f, h * 0.185f)
            quadraticTo(cx + w * 0.085f, h * 0.195f, cx + w * 0.112f, h * 0.215f)
            lineTo(cx + w * 0.096f, h * 0.34f)
            quadraticTo(cx + w * 0.08f, h * 0.405f, cx + w * 0.086f, h * 0.445f)
            lineTo(cx + w * 0.098f, h * 0.49f)
            quadraticTo(cx, h * 0.51f, cx - w * 0.098f, h * 0.49f)
            lineTo(cx - w * 0.086f, h * 0.445f)
            quadraticTo(cx - w * 0.08f, h * 0.405f, cx - w * 0.096f, h * 0.34f)
            lineTo(cx - w * 0.112f, h * 0.215f)
            quadraticTo(cx - w * 0.085f, h * 0.195f, cx - w * 0.032f, h * 0.185f)
            close()
        }
        drawPath(torso, brush = bodyBrush)
        drawPath(torso, color = outlineColor, style = stroke)

        // Subtle torso center seam (classic tailor mannequin line)
        drawLine(
            color = outlineColor.copy(alpha = 0.45f),
            start = Offset(cx, h * 0.19f),
            end = Offset(cx, h * 0.49f),
            strokeWidth = 1.5f,
        )

        // Sculpted Mannequin Head
        val headW = w * 0.094f
        val headH = h * 0.105f
        drawRoundRect(
            brush = bodyBrush,
            topLeft = Offset(cx - headW / 2f, h * 0.055f),
            size = Size(headW, headH),
            cornerRadius = CornerRadius(headW * 0.48f, headW * 0.48f),
        )
        drawRoundRect(
            color = outlineColor,
            topLeft = Offset(cx - headW / 2f, h * 0.055f),
            size = Size(headW, headH),
            cornerRadius = CornerRadius(headW * 0.48f, headW * 0.48f),
            style = stroke,
        )
    }
}
