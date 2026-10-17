package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.screen.adapters.presentation.MapBuilderPresenter
import com.mkz.rpg.screen.adapters.presentation.actionLabels
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickMenuButton
import com.mkz.rpg.screen.adapters.presentation.clickMenuOption
import com.mkz.rpg.screen.adapters.presentation.clickPaletteTerrain
import com.mkz.rpg.screen.adapters.presentation.clickPrimaryAction
import com.mkz.rpg.screen.adapters.presentation.clickRowsStep
import com.mkz.rpg.screen.adapters.presentation.clickSecondaryAction
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.mapBuilderCallToActionView
import com.mkz.rpg.screen.adapters.presentation.mapBuilderHudView
import com.mkz.rpg.screen.adapters.presentation.mapBuilderInfoView
import com.mkz.rpg.screen.adapters.presentation.mapBuilderMenuView
import com.mkz.rpg.screen.adapters.presentation.menuView
import com.mkz.rpg.screen.adapters.presentation.subtitleText
import com.mkz.rpg.screen.adapters.presentation.terrainBitmapAt
import com.mkz.rpg.screen.adapters.presentation.tileCount
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class MapBuilderEndToEndTest : ViewsForTesting() {
    @Test
    fun `should display a 16 x 16 map when the map builder is opened from the menu`() =
        sharedViewsTest {
            // When
            startMapBuilder()
            // Then
            battlefieldView()!!.tileCount() shouldBe 256
            mapBuilderInfoView()!!.subtitleText() shouldBe "16 x 16"
        }

    @Test
    fun `should paint the tile with the selected terrain when a tile is tapped`() =
        sharedViewsTest {
            // Given
            startMapBuilder()
            val battlefieldView = battlefieldView()!!
            val sandBitmap = battlefieldView.terrainBitmapAt(row = 2, column = 2)
            clickPaletteTerrain(mapBuilderHudView()!!, "lava")
            // When
            clickTile(battlefieldView, row = 2, column = 2)
            // Then
            waitUntil { battlefieldView.terrainBitmapAt(row = 2, column = 2) != sandBitmap }
            battlefieldView.terrainBitmapAt(row = 2, column = 2) shouldBe battlefieldView.terrainTileBitmap("lava", terrainTransition = null)
        }

    @Test
    fun `should reject the tile and display why when the terrain can not be next to its neighbour`() =
        sharedViewsTest {
            // Given
            startMapBuilder()
            val battlefieldView = battlefieldView()!!
            val hudView = mapBuilderHudView()!!
            clickPaletteTerrain(hudView, "lava")
            clickTile(battlefieldView, row = 2, column = 2)
            clickPaletteTerrain(hudView, "water")
            val sandBitmap = battlefieldView.terrainBitmapAt(row = 2, column = 3)
            // When
            clickTile(battlefieldView, row = 2, column = 3)
            // Then
            waitUntil { mapBuilderInfoView()!!.subtitleText() == "Water can not be next to lava" }
            mapBuilderInfoView()!!.subtitleText() shouldBe "Water can not be next to lava"
            battlefieldView.terrainBitmapAt(row = 2, column = 3) shouldBe sandBitmap
        }

    @Test
    fun `should display the resized map when the size is stepped`() =
        sharedViewsTest {
            // Given
            startMapBuilder()
            // When
            clickRowsStep(mapBuilderHudView()!!, step = -8)
            // Then
            waitUntil { battlefieldView()?.tileCount() == 128 }
            battlefieldView()!!.tileCount() shouldBe 128
        }

    @Test
    fun `should tell the map was copied when the map is exported`() =
        sharedViewsTest {
            // Given
            startMapBuilder()
            // When
            clickPrimaryAction(mapBuilderCallToActionView()!!)
            // Then
            waitUntil { mapBuilderInfoView()!!.subtitleText() == MapBuilderPresenter.MAP_COPIED }
            mapBuilderInfoView()!!.subtitleText() shouldBe MapBuilderPresenter.MAP_COPIED
        }

    @Test
    fun `should go back to the menu when leaving is confirmed`() =
        sharedViewsTest {
            // Given
            startMapBuilder()
            val callToActionView = mapBuilderCallToActionView()!!
            clickSecondaryAction(callToActionView)
            withClue("Expected leaving to be confirmed first") { callToActionView.actionLabels() shouldBe ("Stay" to "Leave") }
            // When
            clickPrimaryAction(callToActionView)
            // Then
            waitUntil { menuView() != null && mapBuilderHudView() == null }
            menuView().shouldNotBeNull()
            mapBuilderHudView() shouldBe null
        }

    @Test
    fun `should stay in the map builder when staying is selected`() =
        sharedViewsTest {
            // Given
            startMapBuilder()
            val callToActionView = mapBuilderCallToActionView()!!
            clickSecondaryAction(callToActionView)
            // When
            clickSecondaryAction(callToActionView)
            // Then
            callToActionView.actionLabels() shouldBe ("Back" to "Export")
            menuView() shouldBe null
            battlefieldView() shouldNotBe null
        }

    // KorGE opens no file picker without a window, so the file can never be read in the tests.
    @Test
    fun `should tell the file can not be read when importing without a file picker`() =
        sharedViewsTest {
            // Given
            startMapBuilder()
            val menuView = mapBuilderMenuView()!!
            clickMenuButton(menuView)
            // When
            clickMenuOption(menuView, "Import")
            // Then
            waitUntil { mapBuilderInfoView()!!.subtitleText() == MapBuilderPresenter.FILE_NOT_READ }
            mapBuilderInfoView()!!.subtitleText() shouldBe MapBuilderPresenter.FILE_NOT_READ
            battlefieldView()!!.tileCount() shouldBe 256
        }
}
