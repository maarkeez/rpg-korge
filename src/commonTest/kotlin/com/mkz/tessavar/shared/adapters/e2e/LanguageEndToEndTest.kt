package com.mkz.tessavar.shared.adapters.e2e

import com.mkz.tessavar.GAME_WINDOW_SIZE
import com.mkz.tessavar.screen.adapters.presentation.bannerView
import com.mkz.tessavar.screen.adapters.presentation.clickLanguage
import com.mkz.tessavar.screen.adapters.presentation.clickPlayNewGame
import com.mkz.tessavar.screen.adapters.presentation.menuView
import com.mkz.tessavar.screen.adapters.presentation.pickLanguage
import com.mkz.tessavar.screen.adapters.presentation.playNewGameLabel
import com.mkz.tessavar.screen.adapters.presentation.roundText
import com.mkz.tessavar.shared.adapters.presentation.sharedViewsTest
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.startApp
import io.kotest.matchers.shouldBe
import korlibs.korge.service.storage.InmemoryStorage
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class LanguageEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    @Test
    fun `should play in the language picked from the menu when the human player picks one before a new game`() =
        sharedViewsTest {
            // Given
            stage.startApp(random = FixedDoublesRandom.cpuBattleUnitsAlwaysResting(), storage = InmemoryStorage())
            val menuView = openMenu()
            clickLanguage(menuView)
            pickLanguage(menuView, "Español")
            menuView.playNewGameLabel() shouldBe "Jugar partida nueva"
            // When
            clickPlayNewGame(menuView)
            // Then
            waitUntil { bannerView()?.roundText() == "Explorando" }
            bannerView()?.roundText() shouldBe "Explorando"
        }
}
