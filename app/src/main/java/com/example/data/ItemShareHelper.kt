package com.example.data

import android.content.Intent
import android.net.Uri
import com.example.domain.model.DesignerItem

object ItemShareHelper {
    private const val SCHEME = "garderob"
    private const val HOST = "item"

    fun buildDeepLink(itemId: String): String = "$SCHEME://$HOST/$itemId"

    fun parseItemId(uri: Uri?): String? {
        if (uri == null) return null
        if (!uri.scheme.equals(SCHEME, ignoreCase = true)) return null
        if (!uri.host.equals(HOST, ignoreCase = true)) return null
        return uri.lastPathSegment?.takeIf { it.isNotBlank() }
    }

    fun buildShareText(item: DesignerItem): String {
        val link = buildDeepLink(item.id)
        return "Зацени вещь в «Гардероб»: ${item.brand.displayName} — ${item.title}\n$link"
    }

    fun buildShareIntent(item: DesignerItem, chooserTitle: String): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${item.brand.displayName} — ${item.title}")
            putExtra(Intent.EXTRA_TEXT, buildShareText(item))
        }
        return Intent.createChooser(sendIntent, chooserTitle)
    }
}
