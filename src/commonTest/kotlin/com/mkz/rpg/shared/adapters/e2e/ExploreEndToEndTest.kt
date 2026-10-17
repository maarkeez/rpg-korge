package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import com.mkz.rpg.screen.adapters.presentation.battleHudView
import com.mkz.rpg.screen.adapters.presentation.battleInfoView
import com.mkz.rpg.screen.adapters.presentation.battleUnitInfoView
import com.mkz.rpg.screen.adapters.presentation.battlefieldView
import com.mkz.rpg.screen.adapters.presentation.clickArrow
import com.mkz.rpg.screen.adapters.presentation.clickTile
import com.mkz.rpg.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.isBagDisplayed
import com.mkz.rpg.screen.adapters.presentation.isBattleHudShown
import com.mkz.rpg.screen.adapters.presentation.isDisplayingBattleUnitInfo
import com.mkz.rpg.screen.adapters.presentation.isPadShown
import com.mkz.rpg.screen.adapters.presentation.playerTurnText
import com.mkz.rpg.screen.adapters.presentation.roundText
import com.mkz.rpg.screen.adapters.presentation.virtualPadView
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class ExploreEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    // The battle setup deploys the knights at (6,6), which leads, and (7,6), far from the CPU battle units.

    @Test
    fun `should explore with the virtual pad in place of the battle HUD when the human player plays a new game`() =
        sharedViewsTest {
            // When
            startNewGame()
            // Then
            waitUntil { virtualPadView()?.isPadShown() == true }
            withClue("Expected the virtual pad to be displayed while exploring") { virtualPadView()?.isPadShown()?.shouldBeTrue() }
            withClue("Expected the battle HUD to be hidden while exploring") { isBattleHudShown().shouldBeFalse() }
            battleInfoView()?.roundText() shouldBe "Exploring"
        }

    @Test
    fun `should walk the leading knight and have the other knight follow it when the human player taps an arrow`() =
        sharedViewsTest {
            // Given
            startNewGame()
            waitUntil { virtualPadView()?.isPadShown() == true && battlefieldView()?.hasBattleUnitAt(7, 6) == true }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            // When
            clickArrow(requireNotNull(virtualPadView()), DirectionDto.NORTH)
            // Then
            waitUntil { battlefieldView.hasBattleUnitAt(5, 6) && !battlefieldView.hasBattleUnitAt(7, 6) }
            withClue("Expected the leading knight to walk to (5,6)") { battlefieldView.hasBattleUnitAt(5, 6).shouldBeTrue() }
            withClue("Expected the other knight to follow it to (6,6)") { battlefieldView.hasBattleUnitAt(6, 6).shouldBeTrue() }
            withClue("Expected the other knight to have left (7,6)") { battlefieldView.hasBattleUnitAt(7, 6).shouldBeFalse() }
        }

    @Test
    fun `should display the knight info and its bag in place of the virtual pad when the human player taps it while exploring`() =
        sharedViewsTest {
            // Given
            startNewGame()
            waitUntil { virtualPadView()?.isPadShown() == true && battlefieldView()?.hasBattleUnitAt(6, 6) == true }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            // When
            clickTile(battlefieldView, 6, 6)
            // Then
            waitUntil { isBattleHudShown() && battleUnitInfoView()?.isBagDisplayed() == true }
            withClue("Expected the battle HUD to display the knight info") { battleHudView()?.isDisplayingBattleUnitInfo()?.shouldBeTrue() }
            withClue("Expected the bag of the knight to be displayed") { battleUnitInfoView()?.isBagDisplayed()?.shouldBeTrue() }
            withClue("Expected the virtual pad to be hidden while the knight is selected") { virtualPadView()?.isPadShown()?.shouldBeFalse() }
        }

    @Test
    fun `should display the virtual pad again without moving the knight when the human player taps an empty tile while exploring`() =
        sharedViewsTest {
            // Given
            startNewGame()
            waitUntil { virtualPadView()?.isPadShown() == true && battlefieldView()?.hasBattleUnitAt(6, 6) == true }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            clickTile(battlefieldView, 6, 6)
            waitUntil { isBattleHudShown() }
            // When
            clickTile(battlefieldView, 5, 6)
            // Then
            waitUntil { virtualPadView()?.isPadShown() == true }
            withClue("Expected the virtual pad to be displayed again") { virtualPadView()?.isPadShown()?.shouldBeTrue() }
            withClue("Expected the battle HUD to be hidden again") { isBattleHudShown().shouldBeFalse() }
            withClue("Expected the knight to stay at (6,6)") { battlefieldView.hasBattleUnitAt(6, 6).shouldBeTrue() }
            withClue("Expected the knight not to walk to the tapped tile") { battlefieldView.hasBattleUnitAt(5, 6).shouldBeFalse() }
        }

    @Test
    fun `should switch to the battle when the party walks within reach of a cpu battle unit`() =
        sharedViewsTest {
            // When
            startNewBattle()
            // Then
            withClue("Expected the virtual pad to be hidden while battling") { virtualPadView()?.isPadShown()?.shouldBeFalse() }
            withClue("Expected the battle HUD to be displayed while battling") { isBattleHudShown().shouldBeTrue() }
            // The CPU played first, as it engaged.
            battleInfoView()?.roundText() shouldBe "Round 1"
            battleInfoView()?.playerTurnText() shouldBe "Human turn"
        }
}
