package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battleInfoView
import com.mkz.rpg.screen.adapters.presentation.clickFinishTurn
import com.mkz.rpg.screen.adapters.presentation.isFinishTurnEnabled
import com.mkz.rpg.screen.adapters.presentation.isTurnAnnounced
import com.mkz.rpg.screen.adapters.presentation.playerCallToActionView
import com.mkz.rpg.screen.adapters.presentation.playerTurnText
import com.mkz.rpg.screen.adapters.presentation.roundText
import com.mkz.rpg.startGame
import korlibs.korge.tests.ViewsForTesting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FinishTurnEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    // The battle setup has a human player, which plays first, and a CPU player.

    @Test
    fun `should disable finish turn while the cpu turn is displayed and enable it again when the human turn is announced`() =
        viewsTest {
            // Given
            startGame()
            waitUntil { isTurnAnnounced() && playerCallToActionView()?.isFinishTurnEnabled() == true }
            val playerCallToActionView = requireNotNull(playerCallToActionView()) { "The call to action is not displayed" }
            val battleInfoView = requireNotNull(battleInfoView()) { "The battle info is not displayed" }
            // When
            clickFinishTurn(playerCallToActionView)
            // Then
            waitUntil { battleInfoView.playerTurnText() == "CPU turn" }
            assertThat(battleInfoView.playerTurnText()).isEqualTo("CPU turn")
            assertThat(playerCallToActionView.isFinishTurnEnabled())
                .withFailMessage("Expected finish turn to be disabled while the CPU turn is displayed")
                .isFalse
            waitUntil { battleInfoView.playerTurnText() == "Human turn" && isTurnAnnounced() }
            assertThat(battleInfoView.roundText()).isEqualTo("Round 2")
            assertThat(playerCallToActionView.isFinishTurnEnabled())
                .withFailMessage("Expected finish turn to be enabled once the human turn has been announced")
                .isTrue
        }
}
