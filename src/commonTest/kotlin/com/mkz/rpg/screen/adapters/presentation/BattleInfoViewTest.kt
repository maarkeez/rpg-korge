package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.awaitCompletion
import com.mkz.rpg.shared.adapters.presentation.sampleUntilCompletion
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.shared.adapters.presentation.startAnimation
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class BattleInfoViewTest : ViewsForTesting() {
    @Test
    fun `DisplayBattleInfo should display player name and round when battle info is displayed`() =
        sharedViewsTest {
            // Given
            val battleInfoView = BattleInfoView(TextsMother.texts())
            val playerName = "Player 1"
            val round = 3
            // When
            battleInfoView.displayBattleInfo(playerName = playerName, round = round, turnColor = BattleInfoView.HUMAN_TURN_COLOR)
            // Then
            battleInfoView.roundText() shouldBe "Round $round"
            battleInfoView.playerTurnText() shouldBe "$playerName turn"
        }

    @Test
    fun `DisplayBattleInfo should display the player turn with the turn colour when battle info is displayed`() =
        sharedViewsTest {
            // Given
            val battleInfoView = BattleInfoView(TextsMother.texts())
            // When
            battleInfoView.displayBattleInfo(playerName = "CPU", round = 1, turnColor = BattleInfoView.CPU_TURN_COLOR)
            // Then
            battleInfoView.playerTurnColor() shouldBe BattleInfoView.CPU_TURN_COLOR
        }

    @Test
    fun `AnnounceTurn should display the battle info when the turn is announced`() =
        sharedViewsTest {
            // Given
            val battleInfoView = BattleInfoView(TextsMother.texts())
            addChild(battleInfoView)
            // When
            awaitCompletion(startAnimation { battleInfoView.announceTurn(playerName = "CPU", round = 2, turnColor = BattleInfoView.CPU_TURN_COLOR) })
            // Then
            battleInfoView.roundText() shouldBe "Round 2"
            battleInfoView.playerTurnText() shouldBe "CPU turn"
            battleInfoView.playerTurnColor() shouldBe BattleInfoView.CPU_TURN_COLOR
        }

    @Test
    fun `AnnounceTurn should pop the player turn and the round when the turn of a new round is announced`() =
        sharedViewsTest {
            // Given
            val battleInfoView = BattleInfoView(TextsMother.texts())
            addChild(battleInfoView)
            // When
            val announcing = startAnimation { battleInfoView.announceTurn(playerName = "Human", round = 1, turnColor = BattleInfoView.HUMAN_TURN_COLOR) }
            val scales = sampleUntilCompletion(announcing) { battleInfoView.roundScale() to battleInfoView.playerTurnScale() }
            // Then
            scales.maxOf { (roundScale, _) -> roundScale } shouldBe (BattleInfoView.POP_SCALE plusOrMinus 0.05)
            scales.maxOf { (_, playerTurnScale) -> playerTurnScale } shouldBe (BattleInfoView.POP_SCALE plusOrMinus 0.05)
            battleInfoView.roundScale() shouldBe 1.0
            battleInfoView.playerTurnScale() shouldBe 1.0
        }

    @Test
    fun `AnnounceTurn should only pop the player turn when the turn of the same round is announced`() =
        sharedViewsTest {
            // Given
            val battleInfoView = BattleInfoView(TextsMother.texts())
            addChild(battleInfoView)
            awaitCompletion(startAnimation { battleInfoView.announceTurn(playerName = "Human", round = 1, turnColor = BattleInfoView.HUMAN_TURN_COLOR) })
            // When
            val announcing = startAnimation { battleInfoView.announceTurn(playerName = "CPU", round = 1, turnColor = BattleInfoView.CPU_TURN_COLOR) }
            val scales = sampleUntilCompletion(announcing) { battleInfoView.roundScale() to battleInfoView.playerTurnScale() }
            // Then
            scales.maxOf { (roundScale, _) -> roundScale } shouldBe 1.0
            scales.maxOf { (_, playerTurnScale) -> playerTurnScale } shouldBe (BattleInfoView.POP_SCALE plusOrMinus 0.05)
        }

    @Test
    fun `DisplayExploring should display that the player explores without a player turn when exploring`() =
        sharedViewsTest {
            // Given
            val battleInfoView = BattleInfoView(TextsMother.texts())
            battleInfoView.displayBattleInfo(playerName = "Human", round = 1, turnColor = BattleInfoView.HUMAN_TURN_COLOR)
            // When
            battleInfoView.displayExploring()
            // Then
            battleInfoView.roundText() shouldBe "Exploring"
            battleInfoView.playerTurnText() shouldBe ""
        }

    @Test
    fun `AnnounceBattle should display and pop that a battle starts when combat is engaged`() =
        sharedViewsTest {
            // Given
            val battleInfoView = BattleInfoView(TextsMother.texts())
            addChild(battleInfoView)
            battleInfoView.displayExploring()
            // When
            val announcing = startAnimation { battleInfoView.announceBattle() }
            val scales = sampleUntilCompletion(announcing) { battleInfoView.roundScale() }
            // Then
            battleInfoView.roundText() shouldBe "Battle!"
            battleInfoView.playerTurnText() shouldBe ""
            scales.max() shouldBe (BattleInfoView.POP_SCALE plusOrMinus 0.05)
            battleInfoView.roundScale() shouldBe 1.0
        }

    @Test
    fun `AnnounceTurn should pop the round when the first turn is announced after the battle`() =
        sharedViewsTest {
            // Given
            val battleInfoView = BattleInfoView(TextsMother.texts())
            addChild(battleInfoView)
            awaitCompletion(startAnimation { battleInfoView.announceBattle() })
            // When
            val announcing = startAnimation { battleInfoView.announceTurn(playerName = "Human", round = 1, turnColor = BattleInfoView.HUMAN_TURN_COLOR) }
            val scales = sampleUntilCompletion(announcing) { battleInfoView.roundScale() }
            // Then
            battleInfoView.roundText() shouldBe "Round 1"
            scales.max() shouldBe (BattleInfoView.POP_SCALE plusOrMinus 0.05)
        }

    @Test
    fun `DisplayPlayerWin should display player name when player wins`() =
        sharedViewsTest {
            // Given
            val battleInfoView = BattleInfoView(TextsMother.texts())
            val playerName = "Player 1"
            // When
            battleInfoView.displayPlayerWin(playerName)
            // Then
            battleInfoView.winnerText() shouldBe "$playerName wins!"
            battleInfoView.roundText().shouldBeNull()
            battleInfoView.playerTurnText().shouldBeNull()
        }
}
