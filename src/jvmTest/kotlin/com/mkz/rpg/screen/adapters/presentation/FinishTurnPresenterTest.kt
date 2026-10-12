package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.battle.usecases.commands.FinishPlayerTurn
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FinishTurnPresenterTest {
    private val playerCallToActionView = mock<PlayerCallToActionView>()
    private val finishPlayerTurn = mock<FinishPlayerTurn>()
    private val battleApi = mock<BattleApi>().also { whenever(it.finishPlayerTurn).thenReturn(finishPlayerTurn) }
    private val searchPlayerById = mock<SearchPlayerById>()
    private val playerApi =
        mock<PlayerApi>().also {
            whenever(it.searchPlayerById).thenReturn(searchPlayerById)
            whenever(searchPlayerById("human")).thenReturn(Player.Dto(id = "human", name = "Human", type = Player.Dto.PlayerTypeDto.HUMAN))
            whenever(searchPlayerById("cpu")).thenReturn(Player.Dto(id = "cpu", name = "CPU", type = Player.Dto.PlayerTypeDto.CPU))
        }
    private val eventBus = InMemoryEventBus()
    private val timeline = battleSceneTimeline()

    private fun createPresenter() = FinishTurnPresenter(playerCallToActionView, battleApi, playerApi, timeline, eventBus)

    private fun startTurnOf(playerId: String) {
        eventBus.publish(PlayerTurnStarted(playerId = playerId))
        eventBus.dispatch()
    }

    private fun isFinishTurnEnabled(): Boolean {
        val enabled = argumentCaptor<Boolean>()
        verify(playerCallToActionView, atLeastOnce()).setFinishTurnEnabled(enabled.capture())
        return enabled.lastValue
    }

    private fun clickFinishTurn() {
        val onTurnFinished = argumentCaptor<() -> Unit>()
        verify(playerCallToActionView).displayFinishTurn(onTurnFinished.capture())
        onTurnFinished.firstValue()
    }

    @Nested
    inner class Init {
        @Test
        fun `should display finish turn when is created`() {
            // Given
            // When
            createPresenter()
            // Then
            verify(playerCallToActionView).displayFinishTurn(any())
        }

        @Test
        fun `should disable finish turn when is created and no turn has started yet`() {
            // Given
            // When
            createPresenter()
            // Then
            isFinishTurnEnabled().shouldBeFalse()
        }
    }

    @Nested
    inner class EnableFinishTurn {
        @Test
        fun `should enable finish turn when a human player turn starts`() {
            // Given
            createPresenter()
            // When
            startTurnOf("human")
            // Then
            isFinishTurnEnabled().shouldBeTrue()
        }

        @Test
        fun `should disable finish turn when a cpu player turn starts`() {
            // Given
            createPresenter()
            startTurnOf("human")
            // When
            startTurnOf("cpu")
            // Then
            isFinishTurnEnabled().shouldBeFalse()
        }

        @Test
        fun `should disable finish turn while the timeline is playing during a human player turn`() {
            // Given
            createPresenter()
            startTurnOf("human")
            // When
            timeline.playLongStep()
            // Then
            isFinishTurnEnabled().shouldBeFalse()
        }

        @Test
        fun `should enable finish turn once the previous steps have been played when a human player turn starts`() {
            // Given
            createPresenter()
            val cpuMove = timeline.playLongStep()
            startTurnOf("human")
            isFinishTurnEnabled().shouldBeFalse()
            // When
            cpuMove.complete(Unit)
            // Then
            isFinishTurnEnabled().shouldBeTrue()
        }
    }

    @Nested
    inner class FinishTurn {
        @Test
        fun `should finish the player turn when the player finishes the turn`() {
            // Given
            createPresenter()
            startTurnOf("human")
            // When
            clickFinishTurn()
            // Then
            verify(finishPlayerTurn).invoke()
        }

        @Test
        fun `should disable finish turn when the player finishes the turn`() {
            // Given
            createPresenter()
            startTurnOf("human")
            // When
            clickFinishTurn()
            // Then
            isFinishTurnEnabled().shouldBeFalse()
        }

        @Test
        fun `should not finish the player turn when the timeline is playing`() {
            // Given
            createPresenter()
            startTurnOf("human")
            timeline.playLongStep()
            // When
            clickFinishTurn()
            // Then
            verify(finishPlayerTurn, never()).invoke()
        }

        @Test
        fun `should not finish the player turn when the cpu player turn is displayed`() {
            // Given
            createPresenter()
            startTurnOf("cpu")
            // When
            clickFinishTurn()
            // Then
            verify(finishPlayerTurn, never()).invoke()
        }
    }

    @Nested
    inner class PlayerWins {
        @Test
        fun `should hide the call to action when a player wins`() {
            // Given
            createPresenter()
            eventBus.publish(PlayerVictory(playerId = "human"))
            // When
            eventBus.dispatch()
            // Then
            verify(playerCallToActionView).hide()
        }
    }
}
