package com.mkz.tessavar.shared.adapters.e2e

import com.mkz.tessavar.GAME_WINDOW_SIZE
import com.mkz.tessavar.screen.adapters.presentation.VirtualPadView.Direction
import com.mkz.tessavar.screen.adapters.presentation.bannerView
import com.mkz.tessavar.screen.adapters.presentation.battleUnitInfoView
import com.mkz.tessavar.screen.adapters.presentation.battlefieldView
import com.mkz.tessavar.screen.adapters.presentation.callToActionView
import com.mkz.tessavar.screen.adapters.presentation.clickArrow
import com.mkz.tessavar.screen.adapters.presentation.clickInventoryButton
import com.mkz.tessavar.screen.adapters.presentation.clickTile
import com.mkz.tessavar.screen.adapters.presentation.hasBattleUnitAt
import com.mkz.tessavar.screen.adapters.presentation.hudView
import com.mkz.tessavar.screen.adapters.presentation.inventoryView
import com.mkz.tessavar.screen.adapters.presentation.isDisplayingBattleUnitInfo
import com.mkz.tessavar.screen.adapters.presentation.isDisplayingFinishPlayerTurn
import com.mkz.tessavar.screen.adapters.presentation.isDisplayingNothing
import com.mkz.tessavar.screen.adapters.presentation.isHudShown
import com.mkz.tessavar.screen.adapters.presentation.isInventoryButtonDisplayed
import com.mkz.tessavar.screen.adapters.presentation.isPadShown
import com.mkz.tessavar.screen.adapters.presentation.playerTurnText
import com.mkz.tessavar.screen.adapters.presentation.roundText
import com.mkz.tessavar.screen.adapters.presentation.virtualPadView
import com.mkz.tessavar.shared.adapters.presentation.sharedViewsTest
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ExploreEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    private val knightRow = KNIGHT_START_ROW
    private val knightColumn = KNIGHT_START_COLUMN
    private val nextTileColumn = KNIGHT_START_COLUMN + 1

    @Test
    fun `should explore with the virtual pad in place of the HUD when the human player plays a new game`() =
        sharedViewsTest {
            // When
            startNewGame()
            // Then
            waitUntil { virtualPadView()?.isPadShown() == true }
            withClue("Expected the virtual pad to be displayed while exploring") { virtualPadView()?.isPadShown()?.shouldBeTrue() }
            withClue("Expected the HUD to be hidden while exploring") { isHudShown().shouldBeFalse() }
            bannerView()?.roundText() shouldBe "Exploring"
        }

    @Test
    fun `should walk the knight one tile when the human player taps an arrow`() =
        sharedViewsTest {
            // Given
            startNewGame()
            waitUntil { virtualPadView()?.isPadShown() == true && battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            // When
            clickArrow(requireNotNull(virtualPadView()), Direction.EAST)
            // Then
            waitUntil { battlefieldView.hasBattleUnitAt(knightRow, nextTileColumn) && !battlefieldView.hasBattleUnitAt(knightRow, knightColumn) }
            withClue("Expected the knight to walk to ($knightRow,$nextTileColumn)") { battlefieldView.hasBattleUnitAt(knightRow, nextTileColumn).shouldBeTrue() }
            withClue("Expected the knight to have left ($knightRow,$knightColumn)") { battlefieldView.hasBattleUnitAt(knightRow, knightColumn).shouldBeFalse() }
        }

    @Test
    fun `should display the knight info and its inventory button in place of the virtual pad when the human player taps it while exploring`() =
        sharedViewsTest {
            // Given
            startNewGame()
            waitUntil { virtualPadView()?.isPadShown() == true && battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            // When
            clickTile(battlefieldView, knightRow, knightColumn)
            // Then
            waitUntil { isHudShown() && battleUnitInfoView()?.isInventoryButtonDisplayed() == true }
            withClue("Expected the HUD to display the knight info") { hudView()?.isDisplayingBattleUnitInfo()?.shouldBeTrue() }
            withClue("Expected the inventory button of the knight to be displayed") { battleUnitInfoView()?.isInventoryButtonDisplayed()?.shouldBeTrue() }
            withClue("Expected the virtual pad to be hidden while the knight is selected") { virtualPadView()?.isPadShown()?.shouldBeFalse() }
            withClue("Expected no finish turn while exploring, as there are no turns") { callToActionView()?.isDisplayingNothing()?.shouldBeTrue() }
        }

    @Test
    fun `should keep the inventory of the knight open while the cpu battle units wander when the human player opens its inventory while exploring`() =
        sharedViewsTest {
            // Given
            startNewGame(random = FixedDoublesRandom(0.99))
            waitUntil { virtualPadView()?.isPadShown() == true && battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            clickTile(battlefieldView, knightRow, knightColumn)
            waitUntil { battleUnitInfoView()?.isInventoryButtonDisplayed() == true }
            // When
            clickInventoryButton(requireNotNull(battleUnitInfoView()))
            waitUntil { inventoryView() != null }
            // Then
            inventoryView().shouldNotBeNull()
            withClue("Expected no finish turn while exploring, as there are no turns") { callToActionView()?.isDisplayingNothing()?.shouldBeTrue() }
            waitFor(3.seconds)
            withClue("Expected the inventory of the knight to stay open until the human player closes it") { inventoryView().shouldNotBeNull() }
        }

    @Test
    fun `should display the virtual pad again without moving the knight when the human player taps an empty tile while exploring`() =
        sharedViewsTest {
            // Given
            startNewGame()
            waitUntil { virtualPadView()?.isPadShown() == true && battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            clickTile(battlefieldView, knightRow, knightColumn)
            waitUntil { isHudShown() }
            // When
            clickTile(battlefieldView, knightRow, nextTileColumn)
            // Then
            waitUntil { virtualPadView()?.isPadShown() == true }
            withClue("Expected the virtual pad to be displayed again") { virtualPadView()?.isPadShown()?.shouldBeTrue() }
            withClue("Expected the HUD to be hidden again") { isHudShown().shouldBeFalse() }
            withClue("Expected the knight to stay at ($knightRow,$knightColumn)") { battlefieldView.hasBattleUnitAt(knightRow, knightColumn).shouldBeTrue() }
            withClue("Expected the knight not to walk to the tapped tile") { battlefieldView.hasBattleUnitAt(knightRow, nextTileColumn).shouldBeFalse() }
        }

    @Test
    fun `should switch to the battle when the party walks within reach of a cpu battle unit`() =
        sharedViewsTest {
            // When
            startNewBattle()
            // Then
            withClue("Expected the virtual pad to be hidden while battling") { virtualPadView()?.isPadShown()?.shouldBeFalse() }
            withClue("Expected the HUD to be displayed while battling") { isHudShown().shouldBeTrue() }
            withClue("Expected the finish turn to be displayed while battling") { callToActionView()?.isDisplayingFinishPlayerTurn()?.shouldBeTrue() }
            bannerView()?.roundText() shouldBe "Round 1"
            bannerView()?.playerTurnText() shouldBe "Human turn"
        }
}
