package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.AvitoLinkHelper
import com.example.data.DesignerCatalog
import com.example.data.ItemShareHelper
import com.example.domain.model.FashionBrand
import com.example.domain.model.GarmentType
import com.example.domain.model.WeatherCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DesignerFeedViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `catalog contains all 17 verified items with linked colorway groups`() {
        assertEquals(17, DesignerCatalog.items.size)
        assertEquals(17, DesignerCatalog.items.map { it.id }.toSet().size)

        assertTrue(
            DesignerCatalog.items.all {
                it.imageUrl.startsWith("https://") &&
                    it.fallbackImageRes != 0 &&
                    it.title.isNotBlank() &&
                    it.weatherTags.isNotEmpty()
            },
        )
        assertEquals(
            "raf_simons_rt2_white_black",
            DesignerCatalog.findById("raf_simons_rt2_white_black")?.id,
        )
        assertNull(DesignerCatalog.findById("unknown_id"))
    }

    @Test
    fun `item share helper builds and parses garderob deep links`() {
        val item = DesignerCatalog.items.first()
        val deepLink = ItemShareHelper.buildDeepLink(item.id)
        assertEquals("garderob://item/${item.id}", deepLink)
        assertEquals(item.id, ItemShareHelper.parseItemId(Uri.parse(deepLink)))
        assertNull(ItemShareHelper.parseItemId(Uri.parse("https://example.com/item/${item.id}")))
        assertTrue(deepLink in ItemShareHelper.buildShareText(item))
    }

    @Test
    fun `avito link helper builds encoded search url`() {
        val url = AvitoLinkHelper.buildAvitoSearchUrl("Off-White Odsy-1000")
        assertTrue(url.startsWith("https://www.avito.ru/rossiya/odezhda_obuv_aksessuary?q="))
        assertTrue("Off-White" in url)
    }

    @Test
    fun `filters, colorway swapping, likes, and custom gradient profile work`() = runTest(testDispatcher) {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = DesignerFeedViewModel(app)
        val collectJob = backgroundScope.launch(testDispatcher) {
            vm.uiState.collect()
        }
        vm.clearAllLikes()
        vm.resetFilters()

        assertEquals(17, vm.uiState.value.feedItems.size)
        assertEquals(6, vm.uiState.value.availableBrands.size)
        assertEquals(6, vm.uiState.value.availableCategories.size)

        // Brand filter: MCM (4 items: 2 backpacks + 2 belts)
        vm.selectBrand(FashionBrand.MCM)
        assertEquals(4, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.brand == FashionBrand.MCM })

        // Brand filter: Armani Jeans (1 item)
        vm.selectBrand(FashionBrand.ARMANI_JEANS)
        assertEquals(1, vm.uiState.value.feedItems.size)
        assertEquals("armani_jeans_dark_blue", vm.uiState.value.feedItems.first().id)

        // Category filter: Рюкзаки (2 colorways)
        vm.resetFilters()
        vm.selectCategory(GarmentType.BACKPACK)
        assertEquals(2, vm.uiState.value.feedItems.size)

        // Weather filter: Жара / Солнце
        vm.resetFilters()
        vm.selectWeather(WeatherCondition.HOT)
        assertTrue(vm.uiState.value.feedItems.isNotEmpty())
        assertTrue(vm.uiState.value.feedItems.all { WeatherCondition.HOT in it.weatherTags })

        // Keyword matching: Response Trail (3 colorways)
        vm.resetFilters()
        vm.updateKeyword("Response Trail")
        assertEquals(3, vm.uiState.value.feedItems.size)

        // Likes
        val targetId = vm.uiState.value.feedItems.first().id
        vm.toggleLike(targetId)
        assertTrue(targetId in vm.uiState.value.likedIds)
        vm.toggleLike(targetId)
        assertFalse(targetId in vm.uiState.value.likedIds)

        // Colorway swapping groups (each colorway is a separate item)
        val rt2Black = vm.uiState.value.allItems.first { it.id == "raf_simons_adidas_response_trail_2" }
        assertEquals(3, vm.uiState.value.colorVariantsFor(rt2Black).size)

        val blackOdsy = vm.uiState.value.allItems.first { it.id == "offwhite_odsy_1000_trail" }
        assertEquals(3, vm.uiState.value.colorVariantsFor(blackOdsy).size)

        val whiteAf1 = vm.uiState.value.allItems.first { it.id == "offwhite_af1_mid_white" }
        assertEquals(2, vm.uiState.value.colorVariantsFor(whiteAf1).size)

        val starkCognac = vm.uiState.value.allItems.first { it.id == "mcm_stark_backpack_cognac" }
        assertEquals(2, vm.uiState.value.colorVariantsFor(starkCognac).size)

        // Custom gradient colors
        vm.updateProfile(
            displayName = "Данил220",
            username = "danil_arch",
            bio = "Ищу архив и робины",
            avatarEmoji = "🦇",
            gradientStartArgb = 0xFFDC2626,
            gradientEndArgb = 0xFF06B6D4,
            useGradientBackground = true,
        )
        val profile = vm.uiState.value.userProfile
        assertEquals("Данил220", profile.displayName)
        assertEquals("@danil_arch", profile.username)
        assertEquals("🦇", profile.avatarEmoji)
        assertEquals(0xFFDC2626L, profile.gradientStartArgb)
        assertEquals(0xFF06B6D4L, profile.gradientEndArgb)
        assertTrue(profile.useGradientBackground)

        collectJob.cancel()
    }
}
