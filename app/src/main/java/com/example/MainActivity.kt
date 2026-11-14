package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DesignerCatalog
import com.example.data.ItemShareHelper
import com.example.domain.model.DesignerItem
import com.example.ui.DesignerFeedViewModel
import com.example.ui.components.AppDestination
import com.example.ui.components.DesignerItemDetailSheet
import com.example.ui.components.FloatingDockBar
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.GarderobTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {

    private val pendingSharedItemId = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        enableEdgeToEdge()
        setContent {
            GarderobTheme {
                GarderobApp(
                    sharedItemIdFlow = pendingSharedItemId.asStateFlow(),
                    onConsumeSharedItemId = { pendingSharedItemId.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val itemId = ItemShareHelper.parseItemId(intent?.data)
        if (itemId != null) {
            pendingSharedItemId.value = itemId
        }
    }
}

@Composable
fun GarderobApp(
    sharedItemIdFlow: StateFlow<String?>,
    onConsumeSharedItemId: () -> Unit,
    viewModel: DesignerFeedViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sharedItemId by sharedItemIdFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var currentDestination by remember { mutableStateOf(AppDestination.FEED) }
    var inspectingItemId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sharedItemId, uiState.allItems) {
        val targetId = sharedItemId ?: return@LaunchedEffect
        val matched = uiState.allItems.firstOrNull { it.id == targetId }
            ?: DesignerCatalog.findById(targetId)
        if (matched != null) {
            currentDestination = AppDestination.FEED
            inspectingItemId = matched.id
        }
        onConsumeSharedItemId()
    }

    fun openExternalUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        runCatching { context.startActivity(intent) }
    }

    fun shareItem(item: DesignerItem) {
        val chooserTitle = context.getString(R.string.share_chooser_title)
        val shareIntent = ItemShareHelper.buildShareIntent(item, chooserTitle)
        runCatching { context.startActivity(shareIntent) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (currentDestination) {
                AppDestination.FEED -> {
                    FeedScreen(
                        uiState = uiState,
                        onKeywordChange = viewModel::updateKeyword,
                        onSelectBrand = viewModel::selectBrand,
                        onSelectCategory = viewModel::selectCategory,
                        onSelectWeather = viewModel::selectWeather,
                        onResetFilters = viewModel::resetFilters,
                        onToggleLike = viewModel::toggleLike,
                        onItemClick = { item -> inspectingItemId = item.id },
                        onShareItem = ::shareItem,
                        onOpenAvitoUrl = ::openExternalUrl,
                    )
                }

                AppDestination.PROFILE -> {
                    ProfileScreen(
                        uiState = uiState,
                        onBackToFeed = { currentDestination = AppDestination.FEED },
                        onSaveProfile = viewModel::updateProfile,
                        onPickAvatarUri = viewModel::saveAvatarFromUri,
                        onClearAvatarPhoto = viewModel::clearAvatarPhoto,
                        onPickCoverUri = viewModel::saveCoverFromUri,
                        onClearCoverPhoto = viewModel::clearCoverPhoto,
                        onItemClick = { item -> inspectingItemId = item.id },
                        onRemoveLike = viewModel::removeLike,
                        onClearAllLikes = viewModel::clearAllLikes,
                    )
                }
            }

            FloatingDockBar(
                currentDestination = currentDestination,
                likedCount = uiState.likedItems.size,
                onSelectDestination = { destination -> currentDestination = destination },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    val activeInspect = inspectingItemId?.let { targetId ->
        uiState.allItems.firstOrNull { it.id == targetId }
    }
    if (activeInspect != null) {
        DesignerItemDetailSheet(
            item = activeInspect,
            colorVariants = uiState.colorVariantsFor(activeInspect),
            isLiked = activeInspect.id in uiState.likedIds,
            onDismiss = { inspectingItemId = null },
            onToggleLike = { viewModel.toggleLike(activeInspect.id) },
            onSelectVariant = { variant -> inspectingItemId = variant.id },
            onOpenAvito = ::openExternalUrl,
            onShareItem = ::shareItem,
        )
    }
}
