package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent.BattleFinishedWithoutWinner
import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.GameSceneTimelineMother.gameSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.GameSceneTimelineMother.playLongStep
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.shared.domain.DomainEvent
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FinishPlayerTurnPresenterTest {
    // The battle has started with the human player turn.
    private val apis =
        SubdomainApis(
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            playerOrder = listOf("human", "cpu"),
        ).also(SubdomainApis::initialise)
    private val eventBus = apis.eventBus
    private val callToActionView = FakeCallToActionView()
    private val timeline = gameSceneTimeline()

    private fun createPresenter() = FinishPlayerTurnPresenter(callToActionView, apis.battleApi, apis.playerApi, timeline, eventBus)

    private fun publish(event: DomainEvent) {
        eventBus.publish(event)
        eventBus.dispatch()
    }

    private fun startTurnOf(playerId: String) = publish(PlayerTurnStarted(playerId = playerId, battleUnitIds = emptySet()))

    // The player clicks on the finish turn the presenter displayed.
    private fun clickFinishPlayerTurn() {
        callToActionView.onPlayerTurnFinished.shouldNotBeNull().invoke()
        eventBus.dispatch()
    }

    private fun currentPlayerId() = apis.battleApi.searchBattle()?.currentPlayerId

    @Test
    fun `Init should not display finish turn when is created, as a new game starts exploring`() {
        // Given
        // When
        createPresenter()
        // Then
        callToActionView.onPlayerTurnFinished.shouldBeNull()
    }

    @Test
    fun `DisplayFinishPlayerTurn should display finish turn when the battle starts`() {
        // Given
        createPresenter()
        // When
        publish(BattleStarted)
        // Then
        callToActionView.onPlayerTurnFinished.shouldNotBeNull()
    }

    @Test
    fun `HideFinishPlayerTurn should hide finish turn when the battle finishes`() {
        // Given
        createPresenter()
        publish(BattleStarted)
        // When
        publish(BattleFinishedWithoutWinner)
        // Then
        callToActionView.onPlayerTurnFinished.shouldBeNull()
    }

    @Test
    fun `HideFinishPlayerTurn should disable finish turn when the battle finishes during the human player turn`() {
        // Given
        createPresenter()
        publish(BattleStarted)
        startTurnOf("human")
        // When
        publish(BattleFinishedWithoutWinner)
        // Then
        callToActionView.isFinishPlayerTurnEnabled.shouldBeFalse()
    }

    @Test
    fun `Init should disable finish turn when is created and no turn has started yet`() {
        // Given
        // When
        createPresenter()
        // Then
        callToActionView.isFinishPlayerTurnEnabled.shouldBeFalse()
    }

    @Test
    fun `EnableFinishPlayerTurn should enable finish turn when a human player turn starts`() {
        // Given
        createPresenter()
        // When
        startTurnOf("human")
        // Then
        callToActionView.isFinishPlayerTurnEnabled.shouldBeTrue()
    }

    @Test
    fun `EnableFinishPlayerTurn should disable finish turn when a cpu player turn starts`() {
        // Given
        createPresenter()
        startTurnOf("human")
        // When
        startTurnOf("cpu")
        // Then
        callToActionView.isFinishPlayerTurnEnabled.shouldBeFalse()
    }

    @Test
    fun `EnableFinishPlayerTurn should disable finish turn while the timeline is playing during a human player turn`() {
        // Given
        createPresenter()
        startTurnOf("human")
        // When
        timeline.playLongStep()
        // Then
        callToActionView.isFinishPlayerTurnEnabled.shouldBeFalse()
    }

    @Test
    fun `EnableFinishPlayerTurn should enable finish turn once the previous steps have been played when a human player turn starts`() {
        // Given
        createPresenter()
        val cpuMove = timeline.playLongStep()
        startTurnOf("human")
        callToActionView.isFinishPlayerTurnEnabled.shouldBeFalse()
        // When
        cpuMove.complete(Unit)
        // Then
        callToActionView.isFinishPlayerTurnEnabled.shouldBeTrue()
    }

    @Test
    fun `FinishPlayerTurn should finish the player turn when the player finishes the turn`() {
        // Given
        createPresenter()
        publish(BattleStarted)
        startTurnOf("human")
        // When
        clickFinishPlayerTurn()
        // Then
        currentPlayerId() shouldBe "cpu"
    }

    @Test
    fun `FinishPlayerTurn should disable finish turn when the player finishes the turn`() {
        // Given
        createPresenter()
        publish(BattleStarted)
        startTurnOf("human")
        // When
        clickFinishPlayerTurn()
        // Then
        callToActionView.isFinishPlayerTurnEnabled.shouldBeFalse()
    }

    @Test
    fun `FinishPlayerTurn should not finish the player turn when the timeline is playing`() {
        // Given
        createPresenter()
        publish(BattleStarted)
        startTurnOf("human")
        timeline.playLongStep()
        // When
        clickFinishPlayerTurn()
        // Then
        currentPlayerId() shouldBe "human"
    }

    @Test
    fun `FinishPlayerTurn should not finish the player turn when the cpu player turn is displayed`() {
        // Given
        createPresenter()
        publish(BattleStarted)
        startTurnOf("cpu")
        // When
        clickFinishPlayerTurn()
        // Then
        currentPlayerId() shouldBe "human"
    }

    @Test
    fun `PlayerWins should hide the call to action when a player wins`() {
        // Given
        createPresenter()
        eventBus.publish(PlayerVictory(playerId = "human"))
        // When
        eventBus.dispatch()
        // Then
        callToActionView.hideCount shouldBe 1
    }
}
