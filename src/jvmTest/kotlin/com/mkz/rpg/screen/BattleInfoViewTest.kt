package com.mkz.rpg.screen

import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIText
import korlibs.korge.ui.UIVerticalStack
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
                val layout = battleInfoView.children[0] as UIVerticalStack
                val roundText = layout.children[0] as UIText
                val playerText = layout.children[1] as UIText
                assertThat(roundText.text).isEqualTo("Round $round")
                assertThat(playerText.text).isEqualTo("$playerName turn")
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
                val layout = battleInfoView.children[0] as UIVerticalStack
                val winnerText = layout.children[0] as UIText
                assertThat(winnerText.text).isEqualTo("$playerName wins!")
            }
    }

    @Nested
    inner class DisplayPlayback {
        @Test
        fun `should show the playback indicator only while playback is active`() =
            viewsTest {
                // Given
                val battleInfoView = BattleInfoView()
                val hiddenAtStart = !battleInfoView.isPlaybackIndicatorVisible
                // When
                battleInfoView.displayPlayback(active = true)
                val shownWhileActive = battleInfoView.isPlaybackIndicatorVisible
                battleInfoView.displayPlayback(active = false)
                // Then
                assertThat(hiddenAtStart).isTrue()
                assertThat(shownWhileActive).isTrue()
                assertThat(battleInfoView.isPlaybackIndicatorVisible).isFalse()
            }
    }
}
