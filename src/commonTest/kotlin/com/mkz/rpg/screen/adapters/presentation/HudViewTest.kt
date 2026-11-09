package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.GameSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class HudViewTest : ViewsForTesting() {
    @Test
    fun `DisplayBattleUnitInfoView should display battle unit info view when is displayed`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), MessagesMother.messages())
            val castPreviewView = CastPreviewView(loadedResourcesLoader(), MessagesMother.messages())
            val hudView =
                HudView(
                    Size(width = 400.00, height = 200.00),
                    battleUnitInfoView,
                    castPreviewView,
                    InventoryView(loadedResourcesLoader(), MessagesMother.messages()),
                    GiveGoldView(loadedResourcesLoader(), MessagesMother.messages()),
                )
            hudView.displayCastPreviewView()
            // When
            hudView.displayBattleUnitInfoView()
            // Then
            hudView.displayedViews().shouldContainExactly(battleUnitInfoView)
        }

    @Test
    fun `DisplayCastPreviewView should display cast preview view when is displayed`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), MessagesMother.messages())
            val castPreviewView = CastPreviewView(loadedResourcesLoader(), MessagesMother.messages())
            val hudView =
                HudView(
                    Size(width = 400.00, height = 200.00),
                    battleUnitInfoView,
                    castPreviewView,
                    InventoryView(loadedResourcesLoader(), MessagesMother.messages()),
                    GiveGoldView(loadedResourcesLoader(), MessagesMother.messages()),
                )
            hudView.displayBattleUnitInfoView()
            // When
            hudView.displayCastPreviewView()
            // Then
            hudView.displayedViews().shouldContainExactly(castPreviewView)
        }

    @Test
    fun `DisplayInventoryView should display inventory view when is displayed`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), MessagesMother.messages())
            val inventoryView = InventoryView(loadedResourcesLoader(), MessagesMother.messages())
            val hudView =
                HudView(
                    Size(width = 400.00, height = 200.00),
                    battleUnitInfoView,
                    CastPreviewView(loadedResourcesLoader(), MessagesMother.messages()),
                    inventoryView,
                    GiveGoldView(loadedResourcesLoader(), MessagesMother.messages()),
                )
            hudView.displayBattleUnitInfoView()
            // When
            hudView.displayInventoryView()
            // Then
            hudView.displayedViews().shouldContainExactly(inventoryView)
        }

    @Test
    fun `DisplayGiveGoldView should display only the panel`() =
        sharedViewsTest {
            // Given
            val inventoryView = InventoryView(loadedResourcesLoader(), MessagesMother.messages())
            val giveGoldView = GiveGoldView(loadedResourcesLoader(), MessagesMother.messages())
            val hudView =
                HudView(
                    Size(width = 400.00, height = 200.00),
                    BattleUnitInfoView(loadedResourcesLoader(), MessagesMother.messages()),
                    CastPreviewView(loadedResourcesLoader(), MessagesMother.messages()),
                    inventoryView,
                    giveGoldView,
                )
            hudView.displayInventoryView()
            // When
            hudView.displayGiveGoldView()
            // Then
            hudView.displayedViews().shouldContainExactly(giveGoldView)
        }

    @Test
    fun `Hide should not display any view when is hidden`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), MessagesMother.messages())
            val castPreviewView = CastPreviewView(loadedResourcesLoader(), MessagesMother.messages())
            val hudView =
                HudView(
                    Size(width = 400.00, height = 200.00),
                    battleUnitInfoView,
                    castPreviewView,
                    InventoryView(loadedResourcesLoader(), MessagesMother.messages()),
                    GiveGoldView(loadedResourcesLoader(), MessagesMother.messages()),
                )
            hudView.displayBattleUnitInfoView()
            // When
            hudView.hide()
            // Then
            hudView.displayedViews().shouldBeEmpty()
        }
}
