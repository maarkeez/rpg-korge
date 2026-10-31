package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.BattleUnitInfoMother.battleUnitInfo
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class AttackPreviewViewTest : ViewsForTesting() {
    // The height of the battle HUD.
    private val battleHudHeight = 300.0

    private val casterBattleUnit = battleUnitInfo(unitId = "knight")

    private fun receivers(count: Int): List<AttackPreviewView.Receiver> =
        List(count) {
            AttackPreviewView.Receiver(
                battleUnit = battleUnitInfo(unitId = "rat"),
                remainingHealthPoints = 10,
            )
        }

    private fun AttackPreviewView.display(receivers: List<AttackPreviewView.Receiver>) = display(abilityId = "mushroom", casterBattleUnit = casterBattleUnit, manaCost = 10, receivers = receivers)

    @Test
    fun `Display should be visible when is displayed`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader(), TextsMother.texts())
            // When
            attackPreviewView.display(receivers = emptyList())
            // Then
            attackPreviewView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should display the ability and its caster when it is displayed`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader(), TextsMother.texts())
            // When
            attackPreviewView.display(receivers = receivers(1))
            // Then
            attackPreviewView.isAbilityIconDisplayed().shouldBeTrue()
            attackPreviewView.abilityName() shouldBe "Mushroom"
            attackPreviewView.caster() shouldBe "by Knight"
            attackPreviewView.manaSpent() shouldBe "-10 mana"
        }

    @Test
    fun `Display should leave out the mana spent when the ability is free`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader(), TextsMother.texts())
            // When
            attackPreviewView.display(abilityId = "sword", casterBattleUnit = casterBattleUnit, manaCost = 0, receivers = receivers(1))
            // Then
            attackPreviewView.manaSpent().shouldBeNull()
        }

    @Test
    fun `Display should display a row for the caster when the cast changes its health`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader(), TextsMother.texts())
            // When
            attackPreviewView.display(
                abilityId = "heal",
                casterBattleUnit = battleUnitInfo(unitId = "knight", remainingHealthPoints = 10),
                manaCost = 10,
                receivers = emptyList(),
                casterRemainingHealthPoints = 20,
            )
            // Then
            attackPreviewView.isCasterRowDisplayed().shouldBeTrue()
            attackPreviewView.rows().single().healthChange() shouldBe "+10"
        }

    @Test
    fun `Display should display no row for the caster when the cast does not change it`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader(), TextsMother.texts())
            // When
            attackPreviewView.display(receivers = receivers(1))
            // Then
            attackPreviewView.isCasterRowDisplayed().shouldBeFalse()
            attackPreviewView.receiversCount() shouldBe 1
            attackPreviewView.isReceiverPortraitVisible(0).shouldBeTrue()
        }

    @Test
    fun `Display should display every receiver in sight when four receivers are provided`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader(), TextsMother.texts())
            // When
            attackPreviewView.display(
                abilityId = "cleave",
                casterBattleUnit = casterBattleUnit,
                manaCost = 10,
                receivers = receivers(4),
                casterNewEffectIds = listOf("venom-damage"),
            )
            // Then
            attackPreviewView.rows().size shouldBe 5
            attackPreviewView.areRowsInSight(height = battleHudHeight).shouldBeTrue()
        }

    @Test
    fun `Display should hide the previous receivers when fewer receivers are provided`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader(), TextsMother.texts())
            attackPreviewView.display(receivers = receivers(3))
            // When
            attackPreviewView.display(receivers = receivers(1))
            // Then
            attackPreviewView.receiversCount() shouldBe 1
            attackPreviewView.isReceiverPortraitVisible(1).shouldBeFalse()
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader(), TextsMother.texts())
            attackPreviewView.display(receivers = emptyList())
            // When
            attackPreviewView.hide()
            // Then
            attackPreviewView.isVisibleToUser().shouldBeFalse()
        }
}
