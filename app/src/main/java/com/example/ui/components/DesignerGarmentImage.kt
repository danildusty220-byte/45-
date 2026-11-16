package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.DesignerItem

@Composable
fun DesignerGarmentImage(
    item: DesignerItem,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val usesRemoteUrl = item.imageUrl.isNotBlank() &&
        !item.imageUrl.startsWith("https://cdn.garderob.example/")

    if (!usesRemoteUrl) {
        Image(
            painter = painterResource(id = item.fallbackImageRes),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    } else {
        val context = LocalContext.current
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(item.imageUrl)
                .crossfade(true)
                .placeholder(item.fallbackImageRes)
                .error(item.fallbackImageRes)
                .fallback(item.fallbackImageRes)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    }
}
