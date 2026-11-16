package com.example.data

import android.content.Context
import android.net.Uri
import com.example.ui.theme.ProfileGradientPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import kotlin.random.Random

data class UserProfile(
    val displayName: String = UserProfileRepository.randomDefaultDisplayName(),
    val username: String = "@garderob",
    val bio: String = "Архив и редкий стритвир",
    val avatarFilePath: String? = null,
    val avatarEmoji: String? = null,
    val coverFilePath: String? = null,
    val gradientStartArgb: Long = ProfileGradientPreset.CYBER_BLURPLE.startArgb,
    val gradientEndArgb: Long = ProfileGradientPreset.CYBER_BLURPLE.endArgb,
    val useGradientBackground: Boolean = true,
)

class UserProfileRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(loadInitialProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _feedColumns = MutableStateFlow(loadInitialFeedColumns())
    val feedColumns: StateFlow<Int> = _feedColumns.asStateFlow()

    private fun loadInitialProfile(): UserProfile {
        val savedName = prefs.getString(KEY_DISPLAY_NAME, null)?.takeIf { it.isNotBlank() }
        val resolvedName = savedName ?: randomDefaultDisplayName().also { generated ->
            prefs.edit().putString(KEY_DISPLAY_NAME, generated).apply()
        }
        val savedAvatar = prefs.getString(KEY_AVATAR_PATH, null)?.takeIf { File(it).exists() }
        val savedCover = prefs.getString(KEY_COVER_PATH, null)?.takeIf { File(it).exists() }
        return UserProfile(
            displayName = resolvedName,
            username = prefs.getString(KEY_USERNAME, "@garderob").orEmpty().ifBlank { "@garderob" },
            bio = prefs.getString(KEY_BIO, "Архив и редкий стритвир").orEmpty(),
            avatarFilePath = savedAvatar,
            avatarEmoji = prefs.getString(KEY_AVATAR_EMOJI, null),
            coverFilePath = savedCover,
            gradientStartArgb = prefs.getLong(
                KEY_GRADIENT_START,
                ProfileGradientPreset.CYBER_BLURPLE.startArgb,
            ),
            gradientEndArgb = prefs.getLong(
                KEY_GRADIENT_END,
                ProfileGradientPreset.CYBER_BLURPLE.endArgb,
            ),
            useGradientBackground = prefs.getBoolean(KEY_USE_GRADIENT_BG, true),
        )
    }

    private fun loadInitialFeedColumns(): Int =
        prefs.getInt(KEY_FEED_COLUMNS, DEFAULT_FEED_COLUMNS).coerceIn(MIN_FEED_COLUMNS, MAX_FEED_COLUMNS)

    fun setFeedColumns(columns: Int) {
        val coerced = columns.coerceIn(MIN_FEED_COLUMNS, MAX_FEED_COLUMNS)
        _feedColumns.value = coerced
        prefs.edit().putInt(KEY_FEED_COLUMNS, coerced).apply()
    }

    fun updateProfile(
        displayName: String,
        username: String,
        bio: String,
        avatarEmoji: String?,
        gradientStartArgb: Long,
        gradientEndArgb: Long,
        useGradientBackground: Boolean,
    ) {
        val cleanName = displayName.trim().ifBlank { _profile.value.displayName }
        val rawHandle = username.trim().ifBlank { "garderob" }
        val cleanHandle = if (rawHandle.startsWith("@")) rawHandle else "@$rawHandle"
        val cleanBio = bio.trim()

        val updated = _profile.value.copy(
            displayName = cleanName,
            username = cleanHandle,
            bio = cleanBio,
            avatarEmoji = avatarEmoji,
            gradientStartArgb = gradientStartArgb,
            gradientEndArgb = gradientEndArgb,
            useGradientBackground = useGradientBackground,
        )
        persist(updated)
    }

    fun saveAvatarFromUri(uri: Uri): Boolean =
        copyImageToInternalStorage(uri = uri, subDir = "avatars", oldPath = _profile.value.avatarFilePath) { path ->
            persist(_profile.value.copy(avatarFilePath = path, avatarEmoji = null))
        }

    fun clearAvatarPhoto() {
        val oldPath = _profile.value.avatarFilePath
        if (oldPath != null) runCatching { File(oldPath).delete() }
        persist(_profile.value.copy(avatarFilePath = null))
    }

    fun saveCoverFromUri(uri: Uri): Boolean =
        copyImageToInternalStorage(uri = uri, subDir = "covers", oldPath = _profile.value.coverFilePath) { path ->
            persist(_profile.value.copy(coverFilePath = path))
        }

    fun clearCoverPhoto() {
        val oldPath = _profile.value.coverFilePath
        if (oldPath != null) runCatching { File(oldPath).delete() }
        persist(_profile.value.copy(coverFilePath = null))
    }

    private fun copyImageToInternalStorage(
        uri: Uri,
        subDir: String,
        oldPath: String?,
        onSaved: (String) -> Unit,
    ): Boolean {
        return runCatching {
            val dir = File(appContext.filesDir, subDir).apply { mkdirs() }
            val target = File(dir, "img_${System.currentTimeMillis()}.jpg")

            appContext.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return false

            if (oldPath != null && oldPath != target.absolutePath) {
                runCatching { File(oldPath).delete() }
            }
            onSaved(target.absolutePath)
            true
        }.getOrDefault(false)
    }

    private fun persist(profile: UserProfile) {
        _profile.value = profile
        prefs.edit()
            .putString(KEY_DISPLAY_NAME, profile.displayName)
            .putString(KEY_USERNAME, profile.username)
            .putString(KEY_BIO, profile.bio)
            .putString(KEY_AVATAR_PATH, profile.avatarFilePath)
            .putString(KEY_AVATAR_EMOJI, profile.avatarEmoji)
            .putString(KEY_COVER_PATH, profile.coverFilePath)
            .putLong(KEY_GRADIENT_START, profile.gradientStartArgb)
            .putLong(KEY_GRADIENT_END, profile.gradientEndArgb)
            .putBoolean(KEY_USE_GRADIENT_BG, profile.useGradientBackground)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "user_profile_prefs"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_USERNAME = "username"
        private const val KEY_BIO = "bio"
        private const val KEY_AVATAR_PATH = "avatar_file_path"
        private const val KEY_AVATAR_EMOJI = "avatar_emoji"
        private const val KEY_COVER_PATH = "cover_file_path"
        private const val KEY_GRADIENT_START = "gradient_start_argb"
        private const val KEY_GRADIENT_END = "gradient_end_argb"
        private const val KEY_USE_GRADIENT_BG = "use_gradient_bg"
        private const val KEY_FEED_COLUMNS = "feed_columns"

        const val DEFAULT_FEED_COLUMNS = 1
        const val MIN_FEED_COLUMNS = 1
        const val MAX_FEED_COLUMNS = 3

        private val DEFAULT_RANDOM_NAMES = listOf(
            "Архивариус",
            "Неон Лис",
            "Стрит Вайб",
            "Лук Номад",
            "Винтаж Дроп",
            "Опиум Кот",
            "Деним Волк",
            "Трейл Рейдер",
            "Ретро Сет",
            "Хайп Гость",
            "Кибер Бро",
            "Фэшн Фантом",
        )

        fun randomDefaultDisplayName(random: Random = Random.Default): String {
            val base = DEFAULT_RANDOM_NAMES[random.nextInt(DEFAULT_RANDOM_NAMES.size)]
            val number = random.nextInt(10, 100)
            return "$base #$number"
        }
    }
}
