package com.example.data

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object AvitoLinkHelper {
    private const val BASE_CLOTHING_URL = "https://www.avito.ru/rossiya/odezhda_obuv_aksessuary"

    fun buildAvitoSearchUrl(query: String): String {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return BASE_CLOTHING_URL
        val encoded = URLEncoder.encode(trimmed, StandardCharsets.UTF_8.name())
        return "$BASE_CLOTHING_URL?q=$encoded"
    }
}
