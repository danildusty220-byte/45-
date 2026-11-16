package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.AvitoLinkHelper
import com.example.data.DesignerCatalog
import com.example.data.ItemShareHelper
import com.example.data.SavingsGoalRepository
import com.example.data.UserProfileRepository
import com.example.domain.model.FashionBrand
import com.example.domain.model.GarmentType
import com.example.domain.model.GoalTargetType
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
    fun `catalog contains all 76 verified items with linked colorway groups`() {
        assertEquals(76, DesignerCatalog.items.size)
        assertEquals(76, DesignerCatalog.items.map { it.id }.toSet().size)

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
        assertEquals(
            GarmentType.HOODIE,
            DesignerCatalog.findById("offwhite_hoodie_winter_21ss")?.type,
        )
        assertEquals(
            GarmentType.HOODIE,
            DesignerCatalog.findById("offwhite_hoodie_arrows_cream")?.type,
        )
        assertEquals(
            GarmentType.HOODIE,
            DesignerCatalog.findById("palm_angels_kill_bear_hoodie")?.type,
        )
        assertEquals(
            GarmentType.HOODIE,
            DesignerCatalog.findById("amiri_bones_hoodie_blue")?.type,
        )
        assertEquals(
            GarmentType.HOODIE,
            DesignerCatalog.findById("amiri_bandana_paint_hoodie_black")?.type,
        )
        assertEquals(
            GarmentType.JEANS,
            DesignerCatalog.findById("dsquared2_paint_splatter_jeans")?.type,
        )
        assertEquals(
            GarmentType.SHORTS,
            DesignerCatalog.findById("gallery_dept_lanvin_paint_shorts")?.type,
        )
        assertEquals(
            GarmentType.JACKET,
            DesignerCatalog.findById("dsquared2_black_puffer_jacket")?.type,
        )
        assertEquals(
            GarmentType.JACKET,
            DesignerCatalog.findById("moncler_maya_puffer_black")?.type,
        )
        assertEquals(
            GarmentType.JACKET,
            DesignerCatalog.findById("moncler_palm_angels_puffer_black")?.type,
        )
        assertEquals(
            GarmentType.JACKET,
            DesignerCatalog.findById("cp_company_dd_shell_goggle_puffer")?.type,
        )
        assertEquals(
            GarmentType.CAP,
            DesignerCatalog.findById("gutta_snake_gothic_trucker_cap")?.type,
        )
        assertEquals(
            3,
            DesignerCatalog.items.count { it.modelGroupId == "balenciaga_track_faded" },
        )
        assertEquals(
            3,
            DesignerCatalog.items.count { it.modelGroupId == "ferragamo_gancini_belt" },
        )
        assertEquals(
            2,
            DesignerCatalog.items.count { it.modelGroupId == "gucci_interlocking_g_belt" },
        )
        assertEquals(
            2,
            DesignerCatalog.items.count { it.modelGroupId == "aape_tiedye_beanie" },
        )
        assertEquals(
            3,
            DesignerCatalog.items.count { it.modelGroupId == "aape_graphic_tee" },
        )
        assertEquals(
            5,
            DesignerCatalog.items.count { it.modelGroupId == "ambush_multicord_hoodie" },
        )
        assertEquals(
            2,
            DesignerCatalog.items.count { it.modelGroupId == "nike_af1_alyx_high" },
        )
        assertEquals(
            2,
            DesignerCatalog.items.count { it.modelGroupId == "nike_af1_low_07" },
        )
        assertEquals(
            2,
            DesignerCatalog.items.count { it.modelGroupId == "jordan_11_retro" },
        )
        assertEquals(
            2,
            DesignerCatalog.items.count { it.modelGroupId == "jordan_12_retro" },
        )
        assertEquals(
            2,
            DesignerCatalog.items.count { it.modelGroupId == "jordan_13_retro" },
        )
        assertEquals(
            2,
            DesignerCatalog.items.count { it.modelGroupId == "jordan_19_og" },
        )
        assertEquals(
            5,
            DesignerCatalog.items.count { it.modelGroupId == "jeremy_scott_js_wings" },
        )
        assertEquals(
            2,
            DesignerCatalog.items.count { it.modelGroupId == "jeremy_scott_js_bear" },
        )
        assertEquals(
            GarmentType.HOODIE,
            DesignerCatalog.findById("chrome_hearts_horseshoe_white")?.type,
        )
        assertEquals(
            GarmentType.HOODIE,
            DesignerCatalog.findById("chrome_hearts_matty_boy_blue")?.type,
        )
        assertEquals(
            GarmentType.HOODIE,
            DesignerCatalog.findById("chrome_hearts_multicolor_cross_black")?.type,
        )
        assertEquals(
            GarmentType.SNEAKERS,
            DesignerCatalog.findById("jeremy_scott_zebra_tail")?.type,
        )
        assertEquals(
            GarmentType.SNEAKERS,
            DesignerCatalog.findById("jeremy_scott_gorilla")?.type,
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
    fun `default profile name is randomly generated and persisted`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val firstRepo = UserProfileRepository(app)
        val initialName = firstRepo.profile.value.displayName
        assertTrue(initialName.isNotBlank())
        assertTrue(initialName.contains("#"))

        val secondRepo = UserProfileRepository(app)
        assertEquals(initialName, secondRepo.profile.value.displayName)
    }

    @Test
    fun `filters, feed columns, colorway swapping, likes, and custom gradient profile work`() = runTest(testDispatcher) {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = DesignerFeedViewModel(app)
        val collectJob = backgroundScope.launch(testDispatcher) {
            vm.uiState.collect()
        }
        vm.clearAllLikes()
        vm.resetFilters()

        assertEquals(76, vm.uiState.value.feedItems.size)
        assertEquals(23, vm.uiState.value.availableBrands.size)
        assertEquals(10, vm.uiState.value.availableCategories.size)
        assertEquals(1, vm.uiState.value.feedColumns)

        // Default feed mixes garment categories rather than grouping them consecutively
        val firstSixTypes = vm.uiState.value.feedItems.take(6).map { it.type }.toSet()
        assertTrue(firstSixTypes.size >= 4)

        // Shuffling feed preserves all 76 items while changing order
        val beforeShuffleIds = vm.uiState.value.feedItems.map { it.id }
        vm.shuffleFeed()
        val afterShuffleIds = vm.uiState.value.feedItems.map { it.id }
        assertEquals(76, afterShuffleIds.size)
        assertEquals(beforeShuffleIds.toSet(), afterShuffleIds.toSet())
        assertTrue(beforeShuffleIds != afterShuffleIds)

        // Feed layout columns customization (1..3)
        vm.updateFeedColumns(2)
        assertEquals(2, vm.uiState.value.feedColumns)
        vm.updateFeedColumns(3)
        assertEquals(3, vm.uiState.value.feedColumns)
        vm.updateFeedColumns(99)
        assertEquals(3, vm.uiState.value.feedColumns)
        vm.updateFeedColumns(1)
        assertEquals(1, vm.uiState.value.feedColumns)

        // Brand filter: MCM (4 items: 2 backpacks + 2 belts)
        vm.selectBrand(FashionBrand.MCM)
        assertEquals(4, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.brand == FashionBrand.MCM })

        // Brand filter: Armani Jeans (1 item)
        vm.selectBrand(FashionBrand.ARMANI_JEANS)
        assertEquals(1, vm.uiState.value.feedItems.size)
        assertEquals("armani_jeans_dark_blue", vm.uiState.value.feedItems.first().id)

        // Brand filter: Dsquared2 (3 items: 2 puffer jackets + paint splatter jeans)
        vm.selectBrand(FashionBrand.DSQUARED2)
        assertEquals(3, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.brand == FashionBrand.DSQUARED2 })

        // Brand filter: Palm Angels (1 hoodie) & Amiri (2 hoodies + 2 shorts = 4 items)
        vm.selectBrand(FashionBrand.PALM_ANGELS)
        assertEquals(1, vm.uiState.value.feedItems.size)
        assertEquals("palm_angels_kill_bear_hoodie", vm.uiState.value.feedItems.first().id)

        vm.selectBrand(FashionBrand.AMIRI)
        assertEquals(4, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.brand == FashionBrand.AMIRI })

        // Brand filter: Supreme & Stone Island puffers
        vm.selectBrand(FashionBrand.SUPREME)
        assertEquals(1, vm.uiState.value.feedItems.size)
        assertEquals("supreme_fox_racing_puffer_red", vm.uiState.value.feedItems.first().id)

        vm.selectBrand(FashionBrand.STONE_ISLAND)
        assertEquals(1, vm.uiState.value.feedItems.size)
        assertEquals("stone_island_supreme_hand_dyed_puffer", vm.uiState.value.feedItems.first().id)

        vm.selectBrand(FashionBrand.MONCLER)
        assertEquals(2, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.type == GarmentType.JACKET })

        vm.selectBrand(FashionBrand.CP_COMPANY)
        assertEquals(1, vm.uiState.value.feedItems.size)
        assertEquals("cp_company_dd_shell_goggle_puffer", vm.uiState.value.feedItems.first().id)

        vm.selectBrand(FashionBrand.CHROME_HEARTS)
        assertEquals(3, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.type == GarmentType.HOODIE })

        vm.selectBrand(FashionBrand.JEREMY_SCOTT)
        assertEquals(9, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.type == GarmentType.SNEAKERS })

        // Brand filter: AMBUSH (5 Multi-Cord Hoodie colorways)
        vm.selectBrand(FashionBrand.AMBUSH)
        assertEquals(5, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.modelGroupId == "ambush_multicord_hoodie" })

        // Brand filter: Balenciaga (3 Track Faded colorways)
        vm.selectBrand(FashionBrand.BALENCIAGA)
        assertEquals(3, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.brand == FashionBrand.BALENCIAGA })

        // Brand filter: Salvatore Ferragamo (3 Gancini belts)
        vm.selectBrand(FashionBrand.SALVATORE_FERRAGAMO)
        assertEquals(3, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.type == GarmentType.BELT })

        // Brand filter: Gucci (2 Interlocking G belts)
        vm.selectBrand(FashionBrand.GUCCI)
        assertEquals(2, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.type == GarmentType.BELT })

        // Brand filter: Nike (4 AF1 sneakers) & Jordan (8 retro sneakers)
        vm.selectBrand(FashionBrand.NIKE)
        assertEquals(4, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.type == GarmentType.SNEAKERS })

        vm.selectBrand(FashionBrand.JORDAN)
        assertEquals(8, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.type == GarmentType.SNEAKERS })

        // Brand filter: AAPE (3 tees + 2 tie-dye beanies = 5 items) & Gutta (1 cap)
        vm.selectBrand(FashionBrand.AAPE)
        assertEquals(5, vm.uiState.value.feedItems.size)

        vm.selectBrand(FashionBrand.GUTTA)
        assertEquals(1, vm.uiState.value.feedItems.size)
        assertEquals("gutta_snake_gothic_trucker_cap", vm.uiState.value.feedItems.first().id)

        // Category filter: Рюкзаки (2 colorways)
        vm.resetFilters()
        vm.selectCategory(GarmentType.BACKPACK)
        assertEquals(2, vm.uiState.value.feedItems.size)

        // Category filter: Худи (13 hoodies: 2 Off-White, 1 Palm Angels, 2 Amiri, 5 AMBUSH, 3 Chrome Hearts)
        vm.resetFilters()
        vm.selectCategory(GarmentType.HOODIE)
        assertEquals(13, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.type == GarmentType.HOODIE })

        // Category filter: Шорты (3 shorts) & Куртки (7 puffers)
        vm.resetFilters()
        vm.selectCategory(GarmentType.SHORTS)
        assertEquals(3, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.type == GarmentType.SHORTS })

        vm.resetFilters()
        vm.selectCategory(GarmentType.JACKET)
        assertEquals(7, vm.uiState.value.feedItems.size)
        assertTrue(vm.uiState.value.feedItems.all { it.type == GarmentType.JACKET })

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

        val trackFadedBlack = vm.uiState.value.allItems.first { it.id == "balenciaga_track_faded_black" }
        assertEquals(3, vm.uiState.value.colorVariantsFor(trackFadedBlack).size)

        val ferragamoBelt = vm.uiState.value.allItems.first { it.id == "ferragamo_gancini_belt_red_gold" }
        assertEquals(3, vm.uiState.value.colorVariantsFor(ferragamoBelt).size)

        val gucciBelt = vm.uiState.value.allItems.first { it.id == "gucci_interlocking_g_belt_black" }
        assertEquals(2, vm.uiState.value.colorVariantsFor(gucciBelt).size)

        val ambushBurgundy = vm.uiState.value.allItems.first { it.id == "ambush_multicord_hoodie_burgundy" }
        assertEquals(5, vm.uiState.value.colorVariantsFor(ambushBurgundy).size)

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

        // Outfits: create, compatibility score, single favorite in profile, edit, delete
        vm.clearAllOutfits()
        assertTrue(vm.uiState.value.outfits.isEmpty())
        assertNull(vm.uiState.value.favoriteOutfit)

        // Incomplete outfits (missing top, bottom, or sneakers) are rejected
        assertNull(
            vm.createOutfit(
                name = "Только кроссовки",
                itemIds = listOf("offwhite_odsy_1000_trail", "offwhite_af1_mid_black"),
            ),
        )
        assertNull(
            vm.createOutfit(
                name = "Без верха",
                itemIds = listOf("armani_jeans_dark_blue", "raf_simons_rt2_white_black"),
            ),
        )
        assertNull(
            vm.createOutfit(
                name = "Без кроссовок",
                itemIds = listOf("raf_simons_archive_graphic_tee", "armani_jeans_dark_blue"),
            ),
        )
        assertTrue(vm.uiState.value.outfits.isEmpty())

        val firstOutfit = vm.createOutfit(
            name = "Архивный лук",
            itemIds = listOf(
                "raf_simons_archive_graphic_tee",
                "armani_jeans_dark_blue",
                "raf_simons_rt2_white_black",
            ),
        )!!
        val secondOutfit = vm.createOutfit(
            name = "Второй лук",
            itemIds = listOf(
                "aape_camo_moonface_tee_black",
                "gallery_dept_lanvin_paint_shorts",
                "jeremy_scott_wings_money",
                "moncler_maya_puffer_black",
            ),
        )!!

        // Conflicting / duplicate slots must be rejected (no 2 of same type, no jeans + shorts)
        assertNull(
            vm.createOutfit(
                name = "Два кроссовка",
                itemIds = listOf(
                    "raf_simons_archive_graphic_tee",
                    "armani_jeans_dark_blue",
                    "offwhite_odsy_1000_trail",
                    "offwhite_af1_mid_black",
                ),
            ),
        )
        assertNull(
            vm.createOutfit(
                name = "Джинсы и шорты",
                itemIds = listOf(
                    "raf_simons_archive_graphic_tee",
                    "armani_jeans_dark_blue",
                    "gallery_dept_lanvin_paint_shorts",
                    "raf_simons_rt2_white_black",
                ),
            ),
        )
        assertNull(
            vm.createOutfit(
                name = "Две худи",
                itemIds = listOf(
                    "offwhite_hoodie_winter_21ss",
                    "offwhite_hoodie_arrows_cream",
                    "armani_jeans_dark_blue",
                    "raf_simons_rt2_white_black",
                ),
            ),
        )
        assertNull(
            vm.createOutfit(
                name = "Кепка и шапка",
                itemIds = listOf(
                    "raf_simons_archive_graphic_tee",
                    "armani_jeans_dark_blue",
                    "raf_simons_rt2_white_black",
                    "gutta_snake_gothic_trucker_cap",
                    "aape_tiedye_beanie_black_grey",
                ),
            ),
        )

        assertEquals(2, vm.uiState.value.outfits.size)
        val resolvedFirst = vm.uiState.value.outfits.first { it.id == firstOutfit.id }
        val resolvedSecond = vm.uiState.value.outfits.first { it.id == secondOutfit.id }
        assertTrue(resolvedFirst.compatibility.score in 70..100)
        assertTrue(resolvedFirst.compatibility.score > resolvedSecond.compatibility.score)

        // Pinning one favorite outfit replaces any previous favorite
        vm.toggleFavoriteOutfit(firstOutfit.id)
        assertEquals(firstOutfit.id, vm.uiState.value.favoriteOutfit?.id)

        vm.toggleFavoriteOutfit(secondOutfit.id)
        assertEquals(secondOutfit.id, vm.uiState.value.favoriteOutfit?.id)

        // Deleting the favorited outfit clears profile favorite
        vm.deleteOutfit(secondOutfit.id)
        assertNull(vm.uiState.value.favoriteOutfit)
        assertEquals(1, vm.uiState.value.outfits.size)

        // Updating with incomplete or conflicting items fails and preserves original outfit
        assertFalse(
            vm.updateOutfit(
                outfitId = firstOutfit.id,
                name = "Сломанный",
                itemIds = listOf("armani_jeans_dark_blue"),
            ),
        )
        assertFalse(
            vm.updateOutfit(
                outfitId = firstOutfit.id,
                name = "Конфликт низа",
                itemIds = listOf(
                    "raf_simons_archive_graphic_tee",
                    "armani_jeans_dark_blue",
                    "gallery_dept_lanvin_paint_shorts",
                    "raf_simons_rt2_white_black",
                ),
            ),
        )

        // Editing remaining outfit updates name & score
        assertTrue(
            vm.updateOutfit(
                outfitId = firstOutfit.id,
                name = "Обновлённый архив",
                itemIds = listOf(
                    "raf_simons_archive_graphic_tee",
                    "armani_jeans_dark_blue",
                    "raf_simons_rt2_white_black",
                    "mcm_stark_backpack_black",
                ),
            ),
        )
        val updated = vm.uiState.value.outfits.first { it.id == firstOutfit.id }
        assertEquals("Обновлённый архив", updated.name)
        assertEquals(4, updated.items.size)

        // Layering (T-shirt + Hoodie + Jacket + Jeans + Sneakers + Accessories) works cleanly
        val layeredOutfit = vm.createOutfit(
            name = "Слоёный Winter Лук",
            itemIds = listOf(
                "raf_simons_archive_graphic_tee",
                "offwhite_hoodie_winter_21ss",
                "dsquared2_black_puffer_jacket",
                "armani_jeans_dark_blue",
                "offwhite_odsy_1000_trail",
                "ferragamo_gancini_belt_espresso_gunmetal",
                "mcm_stark_backpack_black",
                "gutta_snake_gothic_trucker_cap",
            ),
        )
        assertTrue(layeredOutfit != null)
        assertEquals(8, layeredOutfit?.itemIds?.size)

        // Savings Goals: create item & outfit goals, gate non-existent outfits, deposits, edit, delete
        vm.clearAllGoals()
        assertTrue(vm.uiState.value.goals.isEmpty())
        assertEquals(0, vm.uiState.value.activeGoalsCount)

        // Invalid targets and unknown / uncreated outfits are rejected
        assertNull(vm.createItemGoal("unknown_item_id", 25_000L))
        assertNull(vm.createItemGoal("offwhite_hoodie_winter_21ss", 0L))
        assertNull(vm.createOutfitGoal("non_existent_outfit_id", 90_000L))
        assertNull(vm.createOutfitGoal(firstOutfit.id, -500L))
        assertTrue(vm.uiState.value.goals.isEmpty())

        // Create item goal
        val hoodieGoal = vm.createItemGoal("offwhite_hoodie_winter_21ss", 35_000L)!!
        assertEquals(1, vm.uiState.value.goals.size)
        assertEquals(1, vm.uiState.value.activeGoalsCount)
        val resolvedItemGoal = vm.uiState.value.goals.first { it.id == hoodieGoal.id }
        assertEquals(GoalTargetType.ITEM, resolvedItemGoal.targetType)
        assertEquals(35_000L, resolvedItemGoal.targetAmountRub)
        assertEquals(0L, resolvedItemGoal.savedAmountRub)
        assertEquals(35_000L, resolvedItemGoal.remainingRub)
        assertEquals(0, resolvedItemGoal.progressPercent)
        assertFalse(resolvedItemGoal.isCompleted)

        // Create outfit goal for an actually created outfit
        val outfitGoal = vm.createOutfitGoal(firstOutfit.id, 120_000L)!!
        assertEquals(2, vm.uiState.value.goals.size)
        assertEquals(2, vm.uiState.value.activeGoalsCount)
        val resolvedOutfitGoal = vm.uiState.value.goals.first { it.id == outfitGoal.id }
        assertEquals(GoalTargetType.OUTFIT, resolvedOutfitGoal.targetType)
        assertEquals("Обновлённый архив", resolvedOutfitGoal.displayTitle)
        assertEquals(4, resolvedOutfitGoal.displayItems.size)

        // Deposit money toward item goal until completed
        assertFalse(vm.addGoalDeposit(hoodieGoal.id, 0L))
        assertTrue(vm.addGoalDeposit(hoodieGoal.id, 14_000L))
        val afterPartial = vm.uiState.value.goals.first { it.id == hoodieGoal.id }
        assertEquals(14_000L, afterPartial.savedAmountRub)
        assertEquals(21_000L, afterPartial.remainingRub)
        assertEquals(40, afterPartial.progressPercent)
        assertFalse(afterPartial.isCompleted)

        assertTrue(vm.addGoalDeposit(hoodieGoal.id, 25_000L))
        val afterComplete = vm.uiState.value.goals.first { it.id == hoodieGoal.id }
        assertEquals(39_000L, afterComplete.savedAmountRub)
        assertEquals(0L, afterComplete.remainingRub)
        assertEquals(100, afterComplete.progressPercent)
        assertTrue(afterComplete.isCompleted)
        assertEquals(1, vm.uiState.value.activeGoalsCount)

        // Edit amounts on outfit goal
        assertFalse(vm.updateGoalAmounts(outfitGoal.id, targetAmountRub = 0L, savedAmountRub = 1_000L))
        assertTrue(vm.updateGoalAmounts(outfitGoal.id, targetAmountRub = 100_000L, savedAmountRub = 50_000L))
        val editedOutfitGoal = vm.uiState.value.goals.first { it.id == outfitGoal.id }
        assertEquals(100_000L, editedOutfitGoal.targetAmountRub)
        assertEquals(50_000L, editedOutfitGoal.savedAmountRub)
        assertEquals(50, editedOutfitGoal.progressPercent)

        // Persistence via SavingsGoalRepository
        val reloadedGoalRepo = SavingsGoalRepository(app)
        assertEquals(2, reloadedGoalRepo.goals.value.size)

        // Delete & clear
        vm.deleteGoal(hoodieGoal.id)
        assertEquals(1, vm.uiState.value.goals.size)
        vm.clearAllGoals()
        assertTrue(vm.uiState.value.goals.isEmpty())
        assertEquals(0, vm.uiState.value.activeGoalsCount)

        collectJob.cancel()
    }
}
