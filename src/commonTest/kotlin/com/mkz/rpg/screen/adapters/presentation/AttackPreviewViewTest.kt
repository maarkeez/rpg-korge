package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class AttackPreviewViewTest : ViewsForTesting() {
    @Test
    fun `Display should be visible when is displayed`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            val casterBattleUnit = battleUnit(unit = unit(id = "knight").toDto()).toDto()
            val casterUnit = unit(id = "knight").toDto()
            // When
            attackPreviewView.display(
                casterBattleUnit = casterBattleUnit,
                casterUnit = casterUnit,
                manaCost = 10,
                receiverBattleUnit = null,
                receiverUnit = null,
                damage = null,
            )
            // Then
            attackPreviewView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should hide receiver views when no receiver battle unit is provided`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            val casterBattleUnit = battleUnit(unit = unit(id = "knight").toDto()).toDto()
            val casterUnit = unit(id = "knight").toDto()
            // When
            attackPreviewView.display(
                casterBattleUnit = casterBattleUnit,
                casterUnit = casterUnit,
                manaCost = 10,
                receiverBattleUnit = null,
                receiverUnit = null,
                damage = null,
            )
            // Then
            attackPreviewView.isCasterPortraitVisible().shouldBeTrue()
            attackPreviewView.isReceiverPortraitVisible().shouldBeFalse()
        }

    @Test
    fun `Display should display receiver views when receiver battle unit is provided`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            val casterBattleUnit = battleUnit(unit = unit(id = "knight").toDto()).toDto()
            val casterUnit = unit(id = "knight").toDto()
            val receiverBattleUnit = battleUnit(unit = unit(id = "rat").toDto()).toDto()
            val receiverUnit = unit(id = "rat").toDto()
            // When
            attackPreviewView.display(
                casterBattleUnit = casterBattleUnit,
                casterUnit = casterUnit,
                manaCost = 10,
                receiverBattleUnit = receiverBattleUnit,
                receiverUnit = receiverUnit,
                damage = 10,
            )
            // Then
            attackPreviewView.isCasterPortraitVisible().shouldBeTrue()
            attackPreviewView.isReceiverPortraitVisible().shouldBeTrue()
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            val casterBattleUnit = battleUnit(unit = unit(id = "knight").toDto()).toDto()
            val casterUnit = unit(id = "knight").toDto()
            attackPreviewView.display(
                casterBattleUnit = casterBattleUnit,
                casterUnit = casterUnit,
                manaCost = 10,
                receiverBattleUnit = null,
                receiverUnit = null,
                damage = null,
            )
            // When
            attackPreviewView.hide()
            // Then
            attackPreviewView.isVisibleToUser().shouldBeFalse()
        }
}
