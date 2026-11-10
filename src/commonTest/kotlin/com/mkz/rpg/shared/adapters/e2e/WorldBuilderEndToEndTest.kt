package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.screen.adapters.presentation.actionLabels
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickMenuButton
import com.mkz.rpg.screen.adapters.presentation.clickMenuOption
import com.mkz.rpg.screen.adapters.presentation.clickPaletteTerrain
import com.mkz.rpg.screen.adapters.presentation.clickPrimaryAction
import com.mkz.rpg.screen.adapters.presentation.clickRowsStep
import com.mkz.rpg.screen.adapters.presentation.clickSecondaryAction
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.menuView
import com.mkz.rpg.screen.adapters.presentation.subtitleText
import com.mkz.rpg.screen.adapters.presentation.terrainBitmapAt
import com.mkz.rpg.screen.adapters.presentation.tileCount
import com.mkz.rpg.screen.adapters.presentation.worldBuilderCallToActionView
import com.mkz.rpg.screen.adapters.presentation.worldBuilderHudView
import com.mkz.rpg.screen.adapters.presentation.worldBuilderInfoView
import com.mkz.rpg.screen.adapters.presentation.worldBuilderMenuView
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class WorldBuilderEndToEndTest : ViewsForTesting() {
    @Test
    fun `should display a 16 x 16 world when the world builder is opened from the menu`() =
        sharedViewsTest {
            // When
            startWorldBuilder()
            // Then
            battlefieldView()!!.tileCount() shouldBe 256
            worldBuilderInfoView()!!.subtitleText() shouldBe "16 x 16"
        }

    @Test
    fun `should paint the tile with the selected terrain when a tile is tapped`() =
        sharedViewsTest {
            // Given
            startWorldBuilder()
            val battlefieldView = battlefieldView()!!
            val sandBitmap = battlefieldView.terrainBitmapAt(row = 2, column = 2)
            clickPaletteTerrain(worldBuilderHudView()!!, "lava")
            // When
            clickTile(battlefieldView, row = 2, column = 2)
            // Then
            waitUntil { battlefieldView.terrainBitmapAt(row = 2, column = 2) != sandBitmap }
            battlefieldView.terrainBitmapAt(row = 2, column = 2) shouldBe battlefieldView.terrainTileBitmap("lava", terrainTransition = null)
        }

    @Test
    fun `should reject the tile and display why when the terrain can not be next to an adjacent tile`() =
        sharedViewsTest {
            // Given
            startWorldBuilder()
            val battlefieldView = battlefieldView()!!
            val hudView = worldBuilderHudView()!!
            clickPaletteTerrain(hudView, "lava")
            clickTile(battlefieldView, row = 2, column = 2)
            clickPaletteTerrain(hudView, "water")
            val sandBitmap = battlefieldView.terrainBitmapAt(row = 2, column = 3)
            // When
            clickTile(battlefieldView, row = 2, column = 3)
            // Then
            waitUntil { worldBuilderInfoView()!!.subtitleText() == "Water can not be next to Lava" }
            worldBuilderInfoView()!!.subtitleText() shouldBe "Water can not be next to Lava"
            battlefieldView.terrainBitmapAt(row = 2, column = 3) shouldBe sandBitmap
        }

    @Test
    fun `should display the resized world when the size is stepped`() =
        sharedViewsTest {
            // Given
            startWorldBuilder()
            // When
            clickRowsStep(worldBuilderHudView()!!, step = -8)
            // Then
            waitUntil { battlefieldView()?.tileCount() == 128 }
            battlefieldView()!!.tileCount() shouldBe 128
        }

    @Test
    fun `should tell the world was copied when the world is exported`() =
        sharedViewsTest {
            // Given
            startWorldBuilder()
            // When
            clickPrimaryAction(worldBuilderCallToActionView()!!)
            // Then
            waitUntil { worldBuilderInfoView()!!.subtitleText() == "World copied to the clipboard" }
            worldBuilderInfoView()!!.subtitleText() shouldBe "World copied to the clipboard"
        }

    @Test
    fun `should go back to the menu when leaving is confirmed`() =
        sharedViewsTest {
            // Given
            startWorldBuilder()
            val callToActionView = worldBuilderCallToActionView()!!
            clickSecondaryAction(callToActionView)
            withClue("Expected leaving to be confirmed first") { callToActionView.actionLabels() shouldBe ("Stay" to "Leave") }
            // When
            clickPrimaryAction(callToActionView)
            // Then
            waitUntil { menuView() != null && worldBuilderHudView() == null }
            menuView().shouldNotBeNull()
            worldBuilderHudView() shouldBe null
        }

    @Test
    fun `should stay in the world builder when staying is selected`() =
        sharedViewsTest {
            // Given
            startWorldBuilder()
            val callToActionView = worldBuilderCallToActionView()!!
            clickSecondaryAction(callToActionView)
            // When
            clickSecondaryAction(callToActionView)
            // Then
            callToActionView.actionLabels() shouldBe ("Back" to "Export")
            menuView() shouldBe null
            battlefieldView() shouldNotBe null
        }

    @Test
    fun `should tell the file can not be read when importing without a file picker`() =
        sharedViewsTest {
            // Given
            startWorldBuilder()
            val menuView = worldBuilderMenuView()!!
            clickMenuButton(menuView)
            // When
            clickMenuOption(menuView, "Import")
            // Then
            waitUntil { worldBuilderInfoView()!!.subtitleText() == "The file can not be read" }
            worldBuilderInfoView()!!.subtitleText() shouldBe "The file can not be read"
            battlefieldView()!!.tileCount() shouldBe 256
        }
}
