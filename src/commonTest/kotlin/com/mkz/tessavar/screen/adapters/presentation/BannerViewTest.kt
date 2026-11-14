package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.awaitCompletion
import com.mkz.tessavar.shared.adapters.presentation.sampleUntilCompletion
import com.mkz.tessavar.shared.adapters.presentation.sharedViewsTest
import com.mkz.tessavar.shared.adapters.presentation.startAnimation
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class BannerViewTest : ViewsForTesting() {
    @Test
    fun `DisplayBanner should display player name and round when the banner is displayed`() =
        sharedViewsTest {
            // Given
            val bannerView = BannerView(MessagesMother.messages())
            val playerId = "player-one"
            val round = 3
            // When
            bannerView.displayBanner(playerId = playerId, round = round, turnColor = BannerView.HUMAN_TURN_COLOR)
            // Then
            bannerView.roundText() shouldBe "Round $round"
            bannerView.playerTurnText() shouldBe "Human turn"
        }

    @Test
    fun `DisplayBanner should display the player turn with the turn colour when the banner is displayed`() =
        sharedViewsTest {
            // Given
            val bannerView = BannerView(MessagesMother.messages())
            // When
            bannerView.displayBanner(playerId = "player-two", round = 1, turnColor = BannerView.CPU_TURN_COLOR)
            // Then
            bannerView.playerTurnColor() shouldBe BannerView.CPU_TURN_COLOR
        }

    @Test
    fun `AnnounceTurn should display the banner when the turn is announced`() =
        sharedViewsTest {
            // Given
            val bannerView = BannerView(MessagesMother.messages())
            addChild(bannerView)
            // When
            awaitCompletion(startAnimation { bannerView.announcePlayerTurn(playerId = "player-two", round = 2, turnColor = BannerView.CPU_TURN_COLOR) })
            // Then
            bannerView.roundText() shouldBe "Round 2"
            bannerView.playerTurnText() shouldBe "CPU turn"
            bannerView.playerTurnColor() shouldBe BannerView.CPU_TURN_COLOR
        }

    @Test
    fun `AnnounceTurn should pop the player turn and the round when the turn of a new round is announced`() =
        sharedViewsTest {
            // Given
            val bannerView = BannerView(MessagesMother.messages())
            addChild(bannerView)
            // When
            val announcing = startAnimation { bannerView.announcePlayerTurn(playerId = "player-one", round = 1, turnColor = BannerView.HUMAN_TURN_COLOR) }
            val scales = sampleUntilCompletion(announcing) { bannerView.roundScale() to bannerView.playerTurnScale() }
            // Then
            scales.maxOf { (roundScale, _) -> roundScale } shouldBe (BannerView.POP_SCALE plusOrMinus 0.05)
            scales.maxOf { (_, playerTurnScale) -> playerTurnScale } shouldBe (BannerView.POP_SCALE plusOrMinus 0.05)
            bannerView.roundScale() shouldBe 1.0
            bannerView.playerTurnScale() shouldBe 1.0
        }

    @Test
    fun `AnnounceTurn should only pop the player turn when the turn of the same round is announced`() =
        sharedViewsTest {
            // Given
            val bannerView = BannerView(MessagesMother.messages())
            addChild(bannerView)
            awaitCompletion(startAnimation { bannerView.announcePlayerTurn(playerId = "player-one", round = 1, turnColor = BannerView.HUMAN_TURN_COLOR) })
            // When
            val announcing = startAnimation { bannerView.announcePlayerTurn(playerId = "player-two", round = 1, turnColor = BannerView.CPU_TURN_COLOR) }
            val scales = sampleUntilCompletion(announcing) { bannerView.roundScale() to bannerView.playerTurnScale() }
            // Then
            scales.maxOf { (roundScale, _) -> roundScale } shouldBe 1.0
            scales.maxOf { (_, playerTurnScale) -> playerTurnScale } shouldBe (BannerView.POP_SCALE plusOrMinus 0.05)
        }

    @Test
    fun `DisplayExploring should display that the player explores without a player turn when exploring`() =
        sharedViewsTest {
            // Given
            val bannerView = BannerView(MessagesMother.messages())
            bannerView.displayBanner(playerId = "player-one", round = 1, turnColor = BannerView.HUMAN_TURN_COLOR)
            // When
            bannerView.displayExploring()
            // Then
            bannerView.roundText() shouldBe "Exploring"
            bannerView.playerTurnText() shouldBe ""
        }

    @Test
    fun `AnnounceBattle should display and pop that a battle starts when combat is engaged`() =
        sharedViewsTest {
            // Given
            val bannerView = BannerView(MessagesMother.messages())
            addChild(bannerView)
            bannerView.displayExploring()
            // When
            val announcing = startAnimation { bannerView.announceBattle() }
            val scales = sampleUntilCompletion(announcing) { bannerView.roundScale() }
            // Then
            bannerView.roundText() shouldBe "Battle!"
            bannerView.playerTurnText() shouldBe ""
            scales.max() shouldBe (BannerView.POP_SCALE plusOrMinus 0.05)
            bannerView.roundScale() shouldBe 1.0
        }

    @Test
    fun `AnnounceTurn should pop the round when the first turn is announced after the battle`() =
        sharedViewsTest {
            // Given
            val bannerView = BannerView(MessagesMother.messages())
            addChild(bannerView)
            awaitCompletion(startAnimation { bannerView.announceBattle() })
            // When
            val announcing = startAnimation { bannerView.announcePlayerTurn(playerId = "player-one", round = 1, turnColor = BannerView.HUMAN_TURN_COLOR) }
            val scales = sampleUntilCompletion(announcing) { bannerView.roundScale() }
            // Then
            bannerView.roundText() shouldBe "Round 1"
            scales.max() shouldBe (BannerView.POP_SCALE plusOrMinus 0.05)
        }

    @Test
    fun `DisplayGameOver should display the game over without the round and the player turn`() =
        sharedViewsTest {
            // Given
            val bannerView = BannerView(MessagesMother.messages())
            // When
            bannerView.displayGameOver()
            // Then
            bannerView.gameOverText() shouldBe "Game over!"
            bannerView.roundText().shouldBeNull()
            bannerView.playerTurnText().shouldBeNull()
        }
}
