package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AvitoLinkHelper
import com.example.domain.model.DesignerItem
import com.example.domain.model.FashionBrand
import com.example.domain.model.GarmentType
import com.example.domain.model.WeatherCondition
import com.example.ui.DesignerFeedUiState
import com.example.ui.components.BrandPickerSheet
import com.example.ui.components.CategoryPickerSheet
import com.example.ui.components.DesignerGarmentImage
import com.example.ui.components.WeatherPickerSheet

@Composable
fun FeedScreen(
    uiState: DesignerFeedUiState,
    onKeywordChange: (String) -> Unit,
    onSelectBrand: (FashionBrand?) -> Unit,
    onSelectCategory: (GarmentType?) -> Unit,
    onSelectWeather: (WeatherCondition) -> Unit,
    onResetFilters: () -> Unit,
    onToggleLike: (String) -> Unit,
    onItemClick: (DesignerItem) -> Unit,
    onShareItem: (DesignerItem) -> Unit,
    onOpenAvitoUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showBrandPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showWeatherPicker by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val isWide = maxWidth >= 640.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp),
        ) {
            FiltersHeaderSection(
                uiState = uiState,
                onKeywordChange = onKeywordChange,
                onSearchSubmit = { focusManager.clearFocus() },
                onOpenBrandPicker = { showBrandPicker = true },
                onOpenCategoryPicker = { showCategoryPicker = true },
                onOpenWeatherPicker = { showWeatherPicker = true },
            )

            if (isWide) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 260.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 6.dp,
                        bottom = 116.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("feed_list"),
                ) {
                    if (uiState.feedItems.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyFeedCard(
                                query = uiState.filters.keywordQuery,
                                onResetFilters = onResetFilters,
                                onOpenAvitoUrl = onOpenAvitoUrl,
                            )
                        }
                    } else {
                        items(uiState.feedItems, key = { it.id }) { item ->
                            DesignerFeedCard(
                                item = item,
                                isLiked = item.id in uiState.likedIds,
                                onToggleLike = { onToggleLike(item.id) },
                                onShare = { onShareItem(item) },
                                onClick = { onItemClick(item) },
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 6.dp,
                        bottom = 116.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("feed_list"),
                ) {
                    if (uiState.feedItems.isEmpty()) {
                        item(key = "empty_feed") {
                            EmptyFeedCard(
                                query = uiState.filters.keywordQuery,
                                onResetFilters = onResetFilters,
                                onOpenAvitoUrl = onOpenAvitoUrl,
                            )
                        }
                    } else {
                        items(uiState.feedItems, key = { it.id }) { item ->
                            DesignerFeedCard(
                                item = item,
                                isLiked = item.id in uiState.likedIds,
                                onToggleLike = { onToggleLike(item.id) },
                                onShare = { onShareItem(item) },
                                onClick = { onItemClick(item) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBrandPicker) {
        BrandPickerSheet(
            brands = uiState.availableBrands,
            selectedBrand = uiState.filters.selectedBrand,
            onSelectBrand = onSelectBrand,
            onDismiss = { showBrandPicker = false },
        )
    }

    if (showCategoryPicker) {
        CategoryPickerSheet(
            categories = uiState.availableCategories,
            selectedCategory = uiState.filters.selectedCategory,
            onSelectCategory = onSelectCategory,
            onDismiss = { showCategoryPicker = false },
        )
    }

    if (showWeatherPicker) {
        WeatherPickerSheet(
            selectedWeather = uiState.filters.selectedWeather,
            onSelectWeather = onSelectWeather,
            onDismiss = { showWeatherPicker = false },
        )
    }
}

@Composable
private fun FiltersHeaderSection(
    uiState: DesignerFeedUiState,
    onKeywordChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onOpenBrandPicker: () -> Unit,
    onOpenCategoryPicker: () -> Unit,
    onOpenWeatherPicker: () -> Unit,
) {
    val filters = uiState.filters
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.filters_header),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.8.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag("filters_header_text"),
        )

        OutlinedTextField(
            value = filters.keywordQuery,
            onValueChange = onKeywordChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.search_keyword_hint),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                if (filters.keywordQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onKeywordChange("") },
                        modifier = Modifier.testTag("clear_keyword_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.search_clear_cd),
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("keyword_search_input"),
        )

        // 3 wide rounded filter tiles (Бренд, Категория, Погода)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val brandLabel = filters.selectedBrand?.displayName
                ?: stringResource(R.string.filter_all_brands)
            SketchFilterTile(
                title = stringResource(R.string.filter_tile_brand_title),
                value = brandLabel,
                isActive = filters.selectedBrand != null,
                onClick = onOpenBrandPicker,
                modifier = Modifier
                    .weight(1f)
                    .testTag("filter_tile_brand"),
            )

            val categoryLabel = filters.selectedCategory?.let { stringResource(it.labelRes) }
                ?: stringResource(R.string.filter_all_categories)
            SketchFilterTile(
                title = stringResource(R.string.filter_tile_category_title),
                value = categoryLabel,
                isActive = filters.selectedCategory != null,
                onClick = onOpenCategoryPicker,
                modifier = Modifier
                    .weight(1f)
                    .testTag("filter_tile_category"),
            )

            val weatherLabel = when (filters.selectedWeather) {
                WeatherCondition.ALL -> stringResource(R.string.filter_all_weather)
                WeatherCondition.HOT -> stringResource(R.string.chip_weather_hot)
                WeatherCondition.MILD -> stringResource(R.string.chip_weather_mild)
                WeatherCondition.COLD -> stringResource(R.string.chip_weather_cold)
            }
            SketchFilterTile(
                title = stringResource(R.string.filter_tile_weather_title),
                value = weatherLabel,
                isActive = filters.selectedWeather != WeatherCondition.ALL,
                onClick = onOpenWeatherPicker,
                modifier = Modifier
                    .weight(1f)
                    .testTag("filter_tile_weather"),
            )
        }
    }
}

@Composable
private fun SketchFilterTile(
    title: String,
    value: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .height(74.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp),
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DesignerFeedCard(
    item: DesignerItem,
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit,
) {
    val primaryWeather = item.weatherTags.firstOrNull() ?: WeatherCondition.MILD
    val weatherBadgeText = when (primaryWeather) {
        WeatherCondition.ALL -> stringResource(R.string.filter_all_weather)
        WeatherCondition.HOT -> stringResource(R.string.chip_weather_hot)
        WeatherCondition.MILD -> stringResource(R.string.chip_weather_mild)
        WeatherCondition.COLD -> stringResource(R.string.chip_weather_cold)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .testTag("feed_item_${item.id}"),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.84f),
            ) {
                DesignerGarmentImage(
                    item = item,
                    contentDescription = "${item.brand.displayName} ${item.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                ) {
                    Text(
                        text = weatherBadgeText.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }

                IconButton(
                    onClick = onToggleLike,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("feed_like_${item.id}"),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(
                                if (isLiked) R.string.liked_remove_cd else R.string.like_cd,
                            ),
                            tint = if (isLiked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.brand.displayName.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(item.type.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(androidx.compose.ui.graphics.Color(item.colorHex)),
                            )
                            Text(
                                text = item.colorName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("feed_share_${item.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = stringResource(R.string.detail_share),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFeedCard(
    query: String,
    onResetFilters: () -> Unit,
    onOpenAvitoUrl: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_feed_card"),
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
                    imageVector = Icons.Outlined.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }

            Text(
                text = stringResource(R.string.feed_empty_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.feed_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onResetFilters,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("empty_reset_filters_button"),
                ) {
                    Text(stringResource(R.string.feed_reset_filters))
                }
                Button(
                    onClick = { onOpenAvitoUrl(AvitoLinkHelper.buildAvitoSearchUrl(query)) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("empty_open_avito_button"),
                ) {
                    Text(stringResource(R.string.detail_search_avito))
                }
            }
        }
    }
}
