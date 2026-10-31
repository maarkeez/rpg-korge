package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.BattleUnitInfoMother.battleUnitInfo
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class ReceiverPreviewViewTest : ViewsForTesting() {
    @Test
    fun `Display should display KO when the receiver would be left with no health`() =
        sharedViewsTest {
            // Given
            val receiverPreviewView = ReceiverPreviewView(loadedResourcesLoader(), TextsMother.texts())
            // When
            receiverPreviewView.display(battleUnit = battleUnitInfo(unitId = "rat", isAlly = false), remainingHealthPoints = -5, newEffectIds = emptyList())
            // Then
            receiverPreviewView.isKnockedOut().shouldBeTrue()
            receiverPreviewView.portraitRingColor() shouldBe HudStyle.DEFEATED
        }

    @Test
    fun `Display should display the change of health when it is displayed`() =
        sharedViewsTest {
            // Given
            val receiverPreviewView = ReceiverPreviewView(loadedResourcesLoader(), TextsMother.texts())
            // When
            receiverPreviewView.display(battleUnit = battleUnitInfo(unitId = "rat", remainingHealthPoints = 20, isAlly = false), remainingHealthPoints = 8, newEffectIds = emptyList())
            // Then
            receiverPreviewView.unitName() shouldBe "Rat"
            receiverPreviewView.healthChange() shouldBe "-12"
            receiverPreviewView.healthChangeColor() shouldBe HudStyle.ENEMY
            receiverPreviewView.isKnockedOut().shouldBeFalse()
            receiverPreviewView.portraitRingColor() shouldBe HudStyle.ENEMY
        }
}
