package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.BattleUnitInfoMother.battleUnitInfo
import com.mkz.tessavar.screen.adapters.presentation.GameSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.tessavar.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class RecipientPreviewViewTest : ViewsForTesting() {
    @Test
    fun `Display should display KO when the recipient would be left with no health`() =
        sharedViewsTest {
            // Given
            val recipientPreviewView = RecipientPreviewView(loadedResourcesLoader(), MessagesMother.messages())
            // When
            recipientPreviewView.display(battleUnit = battleUnitInfo(unitId = "rat", isHumanPlayers = false), remainingHealth = -5, newEffectIds = emptyList())
            // Then
            recipientPreviewView.isKnockedOut().shouldBeTrue()
            recipientPreviewView.portraitRingColor() shouldBe HudStyle.DEFEATED
        }

    @Test
    fun `Display should display the change of health when it is displayed`() =
        sharedViewsTest {
            // Given
            val recipientPreviewView = RecipientPreviewView(loadedResourcesLoader(), MessagesMother.messages())
            // When
            recipientPreviewView.display(battleUnit = battleUnitInfo(unitId = "rat", remainingHealth = 20, isHumanPlayers = false), remainingHealth = 8, newEffectIds = emptyList())
            // Then
            recipientPreviewView.unitName() shouldBe "Rat"
            recipientPreviewView.healthChange() shouldBe "-12"
            recipientPreviewView.healthChangeColor() shouldBe HudStyle.ENEMY
            recipientPreviewView.isKnockedOut().shouldBeFalse()
            recipientPreviewView.portraitRingColor() shouldBe HudStyle.ENEMY
        }
}
