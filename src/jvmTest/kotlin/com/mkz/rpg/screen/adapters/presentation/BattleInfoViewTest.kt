package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.awaitCompletion
import com.mkz.rpg.shared.adapters.presentation.sampleUntilCompletion
import com.mkz.rpg.shared.adapters.presentation.startAnimation
import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
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
                assertThat(battleInfoView.roundText()).isEqualTo("Round $round")
                assertThat(battleInfoView.playerTurnText()).isEqualTo("$playerName turn")
            }

        @Test
        fun `should display the player turn with the turn colour when battle info is displayed`() =
            viewsTest {
                // Given
                val battleInfoView = BattleInfoView()
                // When
                battleInfoView.displayBattleInfo(playerName = "CPU", round = 1, turnColor = BattleInfoView.CPU_TURN_COLOR)
                // Then
                assertThat(battleInfoView.playerTurnColor()).isEqualTo(BattleInfoView.CPU_TURN_COLOR)
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
                assertThat(battleInfoView.roundText()).isEqualTo("Round 2")
                assertThat(battleInfoView.playerTurnText()).isEqualTo("CPU turn")
                assertThat(battleInfoView.playerTurnColor()).isEqualTo(BattleInfoView.CPU_TURN_COLOR)
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
                assertThat(scales.maxOf { (roundScale, _) -> roundScale }).isCloseTo(BattleInfoView.POP_SCALE, within(0.05))
                assertThat(scales.maxOf { (_, playerTurnScale) -> playerTurnScale }).isCloseTo(BattleInfoView.POP_SCALE, within(0.05))
                assertThat(battleInfoView.roundScale()).isEqualTo(1.0)
                assertThat(battleInfoView.playerTurnScale()).isEqualTo(1.0)
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
                assertThat(scales.maxOf { (roundScale, _) -> roundScale }).isEqualTo(1.0)
                assertThat(scales.maxOf { (_, playerTurnScale) -> playerTurnScale }).isCloseTo(BattleInfoView.POP_SCALE, within(0.05))
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
                assertThat(battleInfoView.winnerText()).isEqualTo("$playerName wins!")
                assertThat(battleInfoView.roundText()).isNull()
                assertThat(battleInfoView.playerTurnText()).isNull()
            }
    }
}
