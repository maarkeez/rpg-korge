package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.battleInfoView
import com.mkz.rpg.screen.adapters.presentation.clickLanguage
import com.mkz.rpg.screen.adapters.presentation.clickPlayNewGame
import com.mkz.rpg.screen.adapters.presentation.menuView
import com.mkz.rpg.screen.adapters.presentation.pickLanguage
import com.mkz.rpg.screen.adapters.presentation.playNewGameLabel
import com.mkz.rpg.screen.adapters.presentation.roundText
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.startGame
import io.kotest.matchers.shouldBe
import korlibs.korge.service.storage.InmemoryStorage
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class LanguageEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    @Test
    fun `should play in the language picked from the menu when the human player picks one before a new game`() =
        sharedViewsTest {
            // Given
            stage.startGame(random = FixedDoublesRandom(0.0), storage = InmemoryStorage())
            waitUntil { stage.menuView() != null }
            val menuView = requireNotNull(stage.menuView()) { "The menu is not displayed" }
            clickLanguage(menuView)
            pickLanguage(menuView, "Español")
            menuView.playNewGameLabel() shouldBe "Jugar partida nueva"
            // When
            clickPlayNewGame(menuView)
            // Then
            waitUntil { battleInfoView()?.roundText() == "Explorando" }
            battleInfoView()?.roundText() shouldBe "Explorando"
        }
}
