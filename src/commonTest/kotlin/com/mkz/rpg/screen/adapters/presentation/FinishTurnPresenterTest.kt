package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FinishTurnPresenterTest {
    // The battle has started with the human player turn.
    private val apis =
        SubdomainApis(
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            playerOrder = listOf("human", "cpu"),
        ).also(SubdomainApis::initialise)
    private val eventBus = apis.eventBus
    private val playerCallToActionView = FakePlayerCallToActionView()
    private val timeline = battleSceneTimeline()

    private fun createPresenter() = FinishTurnPresenter(playerCallToActionView, apis.battleApi, apis.playerApi, timeline, eventBus)

    private fun startTurnOf(playerId: String) {
        eventBus.publish(PlayerTurnStarted(playerId = playerId))
        eventBus.dispatch()
    }

    // The player clicks on the finish turn the presenter displayed.
    private fun clickFinishTurn() {
        playerCallToActionView.onTurnFinished.shouldNotBeNull().invoke()
        eventBus.dispatch()
    }

    private fun currentPlayerTurn() = apis.battleApi.searchBattle()?.currentPlayerTurn

    @Test
    fun `Init should display finish turn when is created`() {
        // Given
        // When
        createPresenter()
        // Then
        playerCallToActionView.onTurnFinished.shouldNotBeNull()
    }

    @Test
    fun `Init should disable finish turn when is created and no turn has started yet`() {
        // Given
        // When
        createPresenter()
        // Then
        playerCallToActionView.isFinishTurnEnabled.shouldBeFalse()
    }

    @Test
    fun `EnableFinishTurn should enable finish turn when a human player turn starts`() {
        // Given
        createPresenter()
        // When
        startTurnOf("human")
        // Then
        playerCallToActionView.isFinishTurnEnabled.shouldBeTrue()
    }

    @Test
    fun `EnableFinishTurn should disable finish turn when a cpu player turn starts`() {
        // Given
        createPresenter()
        startTurnOf("human")
        // When
        startTurnOf("cpu")
        // Then
        playerCallToActionView.isFinishTurnEnabled.shouldBeFalse()
    }

    @Test
    fun `EnableFinishTurn should disable finish turn while the timeline is playing during a human player turn`() {
        // Given
        createPresenter()
        startTurnOf("human")
        // When
        timeline.playLongStep()
        // Then
        playerCallToActionView.isFinishTurnEnabled.shouldBeFalse()
    }

    @Test
    fun `EnableFinishTurn should enable finish turn once the previous steps have been played when a human player turn starts`() {
        // Given
        createPresenter()
        val cpuMove = timeline.playLongStep()
        startTurnOf("human")
        playerCallToActionView.isFinishTurnEnabled.shouldBeFalse()
        // When
        cpuMove.complete(Unit)
        // Then
        playerCallToActionView.isFinishTurnEnabled.shouldBeTrue()
    }

    @Test
    fun `FinishTurn should finish the player turn when the player finishes the turn`() {
        // Given
        createPresenter()
        startTurnOf("human")
        // When
        clickFinishTurn()
        // Then
        currentPlayerTurn() shouldBe "cpu"
    }

    @Test
    fun `FinishTurn should disable finish turn when the player finishes the turn`() {
        // Given
        createPresenter()
        startTurnOf("human")
        // When
        clickFinishTurn()
        // Then
        playerCallToActionView.isFinishTurnEnabled.shouldBeFalse()
    }

    @Test
    fun `FinishTurn should not finish the player turn when the timeline is playing`() {
        // Given
        createPresenter()
        startTurnOf("human")
        timeline.playLongStep()
        // When
        clickFinishTurn()
        // Then
        currentPlayerTurn() shouldBe "human"
    }

    @Test
    fun `FinishTurn should not finish the player turn when the cpu player turn is displayed`() {
        // Given
        createPresenter()
        startTurnOf("cpu")
        // When
        clickFinishTurn()
        // Then
        currentPlayerTurn() shouldBe "human"
    }

    @Test
    fun `PlayerWins should hide the call to action when a player wins`() {
        // Given
        createPresenter()
        eventBus.publish(PlayerVictory(playerId = "human"))
        // When
        eventBus.dispatch()
        // Then
        playerCallToActionView.hideCount shouldBe 1
    }
}
