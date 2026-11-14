package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.BattleUnitInfoMother.battleUnitInfo
import com.mkz.tessavar.screen.adapters.presentation.GameSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.tessavar.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class CastPreviewViewTest : ViewsForTesting() {
    private val battleHudHeight = 300.0

    private val casterBattleUnit = battleUnitInfo(unitId = "knight")

    private fun recipients(count: Int): List<CastPreviewView.Recipient> =
        List(count) {
            CastPreviewView.Recipient(
                battleUnit = battleUnitInfo(unitId = "rat"),
                remainingHealth = 10,
            )
        }

    private fun CastPreviewView.display(recipients: List<CastPreviewView.Recipient>) = display(abilityId = "mushroom", casterBattleUnit = casterBattleUnit, manaCost = 10, recipients = recipients)

    @Test
    fun `Display should be visible when is displayed`() =
        sharedViewsTest {
            // Given
            val castPreviewView = CastPreviewView(loadedResourcesLoader(), MessagesMother.messages())
            // When
            castPreviewView.display(recipients = emptyList())
            // Then
            castPreviewView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should display the ability and its caster when it is displayed`() =
        sharedViewsTest {
            // Given
            val castPreviewView = CastPreviewView(loadedResourcesLoader(), MessagesMother.messages())
            // When
            castPreviewView.display(recipients = recipients(1))
            // Then
            castPreviewView.isAbilityIconDisplayed().shouldBeTrue()
            castPreviewView.abilityName() shouldBe "Mushroom"
            castPreviewView.caster() shouldBe "by Knight"
            castPreviewView.manaSpent() shouldBe "-10 mana"
        }

    @Test
    fun `Display should leave out the mana spent when the ability is free`() =
        sharedViewsTest {
            // Given
            val castPreviewView = CastPreviewView(loadedResourcesLoader(), MessagesMother.messages())
            // When
            castPreviewView.display(abilityId = "sword", casterBattleUnit = casterBattleUnit, manaCost = 0, recipients = recipients(1))
            // Then
            castPreviewView.manaSpent().shouldBeNull()
        }

    @Test
    fun `Display should display a row for the caster when the cast changes its health`() =
        sharedViewsTest {
            // Given
            val castPreviewView = CastPreviewView(loadedResourcesLoader(), MessagesMother.messages())
            // When
            castPreviewView.display(
                abilityId = "heal",
                casterBattleUnit = battleUnitInfo(unitId = "knight", remainingHealth = 10),
                manaCost = 10,
                recipients = emptyList(),
                casterRemainingHealth = 20,
            )
            // Then
            castPreviewView.isCasterRowDisplayed().shouldBeTrue()
            castPreviewView.rows().single().healthChange() shouldBe "+10"
        }

    @Test
    fun `Display should display no row for the caster when the cast does not change it`() =
        sharedViewsTest {
            // Given
            val castPreviewView = CastPreviewView(loadedResourcesLoader(), MessagesMother.messages())
            // When
            castPreviewView.display(recipients = recipients(1))
            // Then
            castPreviewView.isCasterRowDisplayed().shouldBeFalse()
            castPreviewView.recipientsCount() shouldBe 1
            castPreviewView.isRecipientPortraitVisible(0).shouldBeTrue()
        }

    @Test
    fun `Display should display every recipient in sight when four recipients are provided`() =
        sharedViewsTest {
            // Given
            val castPreviewView = CastPreviewView(loadedResourcesLoader(), MessagesMother.messages())
            // When
            castPreviewView.display(
                abilityId = "cleave",
                casterBattleUnit = casterBattleUnit,
                manaCost = 10,
                recipients = recipients(4),
                casterNewEffectIds = listOf("venom-damage"),
            )
            // Then
            castPreviewView.rows().size shouldBe 5
            castPreviewView.areRowsInSight(height = battleHudHeight).shouldBeTrue()
        }

    @Test
    fun `Display should hide the previous recipients when fewer recipients are provided`() =
        sharedViewsTest {
            // Given
            val castPreviewView = CastPreviewView(loadedResourcesLoader(), MessagesMother.messages())
            castPreviewView.display(recipients = recipients(3))
            // When
            castPreviewView.display(recipients = recipients(1))
            // Then
            castPreviewView.recipientsCount() shouldBe 1
            castPreviewView.isRecipientPortraitVisible(1).shouldBeFalse()
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        sharedViewsTest {
            // Given
            val castPreviewView = CastPreviewView(loadedResourcesLoader(), MessagesMother.messages())
            castPreviewView.display(recipients = emptyList())
            // When
            castPreviewView.hide()
            // Then
            castPreviewView.isVisibleToUser().shouldBeFalse()
        }
}
