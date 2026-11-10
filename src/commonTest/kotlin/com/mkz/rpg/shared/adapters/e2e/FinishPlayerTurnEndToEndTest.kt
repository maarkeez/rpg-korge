package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.screen.adapters.presentation.bannerView
import com.mkz.rpg.screen.adapters.presentation.callToActionView
import com.mkz.rpg.screen.adapters.presentation.canHumanPlayerAct
import com.mkz.rpg.screen.adapters.presentation.clickFinishPlayerTurn
import com.mkz.rpg.screen.adapters.presentation.isFinishPlayerTurnEnabled
import com.mkz.rpg.screen.adapters.presentation.playerTurnText
import com.mkz.rpg.screen.adapters.presentation.roundText
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class FinishPlayerTurnEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    @Test
    fun `should disable finish turn while the cpu turn is displayed and enable it again when the human turn is announced`() =
        sharedViewsTest {
            // Given
            startNewBattle()
            val callToActionView = requireNotNull(callToActionView()) { "The call to action is not displayed" }
            val bannerView = requireNotNull(bannerView()) { "The banner is not displayed" }
            // When
            clickFinishPlayerTurn(callToActionView)
            // Then
            waitUntil { bannerView.playerTurnText() == "CPU turn" }
            bannerView.playerTurnText() shouldBe "CPU turn"
            withClue("Expected finish turn to be disabled while the CPU turn is displayed") { callToActionView.isFinishPlayerTurnEnabled().shouldBeFalse() }
            waitUntil { bannerView.playerTurnText() == "Human turn" && canHumanPlayerAct() }
            bannerView.roundText() shouldBe "Round 2"
            withClue("Expected finish turn to be enabled once the human turn has been announced") { callToActionView.isFinishPlayerTurnEnabled().shouldBeTrue() }
        }
}
