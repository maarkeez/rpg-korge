package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
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
                battleInfoView.displayBattleInfo(playerName = playerName, round = round)
                // Then
                assertThat(battleInfoView.roundText()).isEqualTo("Round $round")
                assertThat(battleInfoView.playerTurnText()).isEqualTo("$playerName turn")
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
