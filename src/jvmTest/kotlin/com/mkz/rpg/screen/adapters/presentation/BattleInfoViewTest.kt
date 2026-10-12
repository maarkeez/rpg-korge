package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.awaitCompletion
import com.mkz.rpg.shared.adapters.presentation.sampleUntilCompletion
import com.mkz.rpg.shared.adapters.presentation.startAnimation
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BattleInfoViewTest : ViewsForTesting() {
    @Nested
    inner class DisplayBattleInfo {
        @Test
        fun `should display player name and round when battle info is displayed`() =
            viewsTest {
                // Given
                val battleInfoView = BattleInfoView()
                val playerName = "Player 1"
                val round = 3
                // When
                battleInfoView.displayBattleInfo(playerName = playerName, round = round, turnColor = BattleInfoView.HUMAN_TURN_COLOR)
                // Then
                battleInfoView.roundText() shouldBe "Round $round"
                battleInfoView.playerTurnText() shouldBe "$playerName turn"
            }

        @Test
        fun `should display the player turn with the turn colour when battle info is displayed`() =
            viewsTest {
                // Given
                val battleInfoView = BattleInfoView()
                // When
                battleInfoView.displayBattleInfo(playerName = "CPU", round = 1, turnColor = BattleInfoView.CPU_TURN_COLOR)
                // Then
                battleInfoView.playerTurnColor() shouldBe BattleInfoView.CPU_TURN_COLOR
            }
    }

    @Nested
    inner class AnnounceTurn {
        @Test
        fun `should display the battle info when the turn is announced`() =
            viewsTest {
                // Given
                val battleInfoView = BattleInfoView()
                addChild(battleInfoView)
                // When
                awaitCompletion(startAnimation { battleInfoView.announceTurn(playerName = "CPU", round = 2, turnColor = BattleInfoView.CPU_TURN_COLOR) })
                // Then
                battleInfoView.roundText() shouldBe "Round 2"
                battleInfoView.playerTurnText() shouldBe "CPU turn"
                battleInfoView.playerTurnColor() shouldBe BattleInfoView.CPU_TURN_COLOR
            }

        @Test
        fun `should pop the player turn and the round when the turn of a new round is announced`() =
            viewsTest {
                // Given
                val battleInfoView = BattleInfoView()
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
        fun `should only pop the player turn when the turn of the same round is announced`() =
            viewsTest {
                // Given
                val battleInfoView = BattleInfoView()
                addChild(battleInfoView)
                awaitCompletion(startAnimation { battleInfoView.announceTurn(playerName = "Human", round = 1, turnColor = BattleInfoView.HUMAN_TURN_COLOR) })
                // When
                val announcing = startAnimation { battleInfoView.announceTurn(playerName = "CPU", round = 1, turnColor = BattleInfoView.CPU_TURN_COLOR) }
                val scales = sampleUntilCompletion(announcing) { battleInfoView.roundScale() to battleInfoView.playerTurnScale() }
                // Then
                scales.maxOf { (roundScale, _) -> roundScale } shouldBe 1.0
                scales.maxOf { (_, playerTurnScale) -> playerTurnScale } shouldBe (BattleInfoView.POP_SCALE plusOrMinus 0.05)
            }
    }

    @Nested
    inner class DisplayPlayerWin {
        @Test
        fun `should display player name when player wins`() =
            viewsTest {
                // Given
                val battleInfoView = BattleInfoView()
                val playerName = "Player 1"
                // When
                battleInfoView.displayPlayerWin(playerName)
                // Then
                battleInfoView.winnerText() shouldBe "$playerName wins!"
                battleInfoView.roundText().shouldBeNull()
                battleInfoView.playerTurnText().shouldBeNull()
            }
    }
}
