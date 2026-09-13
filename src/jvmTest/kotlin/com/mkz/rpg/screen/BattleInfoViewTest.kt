package com.mkz.rpg.screen

import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIText
import korlibs.korge.view.Container
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
                val container = Container()
                addChild(container)
                val battleInfoView = BattleInfoView(container)
                val playerName = "Player 1"
                val round = 3
                // When
                battleInfoView.displayBattleInfo(playerName = playerName, round = round)
                // Then
                val label = container.children[0] as UIText
                assertThat(label.text).isEqualTo("$playerName turn - Round: $round")
            }
    }

    @Nested
    inner class DisplayPlayerWin {
        @Test
        fun `should display player name when player wins`() =
            viewsTest {
                // Given
                val container = Container()
                addChild(container)
                val battleInfoView = BattleInfoView(container)
                val playerName = "Player 1"
                // When
                battleInfoView.displayPlayerWin(playerName)
                // Then
                val label = container.children[0] as UIText
                assertThat(label.text).isEqualTo("$playerName wins!")
            }
    }
}
