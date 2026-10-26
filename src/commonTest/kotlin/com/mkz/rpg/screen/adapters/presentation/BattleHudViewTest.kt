package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class BattleHudViewTest : ViewsForTesting() {
    @Test
    fun `DisplayBattleUnitInfoView should display battle unit info view when is displayed`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            val battleHudView = BattleHudView(Size(width = 400.00, height = 200.00), battleUnitInfoView, attackPreviewView, InventoryView(loadedResourcesLoader(), TextsMother.texts()))
            battleHudView.displayAttackPreviewView()
            // When
            battleHudView.displayBattleUnitInfoView()
            // Then
            battleHudView.displayedViews().shouldContainExactly(battleUnitInfoView)
        }

    @Test
    fun `DisplayAttackPreviewView should display attack preview view when is displayed`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            val battleHudView = BattleHudView(Size(width = 400.00, height = 200.00), battleUnitInfoView, attackPreviewView, InventoryView(loadedResourcesLoader(), TextsMother.texts()))
            battleHudView.displayBattleUnitInfoView()
            // When
            battleHudView.displayAttackPreviewView()
            // Then
            battleHudView.displayedViews().shouldContainExactly(attackPreviewView)
        }

    @Test
    fun `DisplayInventoryView should display inventory view when is displayed`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            val battleHudView = BattleHudView(Size(width = 400.00, height = 200.00), battleUnitInfoView, AttackPreviewView(loadedResourcesLoader()), inventoryView)
            battleHudView.displayBattleUnitInfoView()
            // When
            battleHudView.displayInventoryView()
            // Then
            battleHudView.displayedViews().shouldContainExactly(inventoryView)
        }

    @Test
    fun `Hide should not display any view when is hidden`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            val battleHudView = BattleHudView(Size(width = 400.00, height = 200.00), battleUnitInfoView, attackPreviewView, InventoryView(loadedResourcesLoader(), TextsMother.texts()))
            battleHudView.displayBattleUnitInfoView()
            // When
            battleHudView.hide()
            // Then
            battleHudView.displayedViews().shouldBeEmpty()
        }
}
