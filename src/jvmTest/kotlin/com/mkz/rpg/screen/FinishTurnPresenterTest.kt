package com.mkz.rpg.screen

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.battle.usecases.commands.FinishPlayerTurn
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FinishTurnPresenterTest {
    private val finishTurnView = mock<FinishTurnView>()
    private val battleApi = mock<BattleApi>()
    private val finishPlayerTurn: FinishPlayerTurn = mock()
    private val eventBus = InMemoryEventBus()
    private val finishTurnPresenter =
        FinishTurnPresenter(
            finishTurnView = finishTurnView,
            battleApi = battleApi,
            eventBus = eventBus,
        ).also {
            whenever(battleApi.finishPlayerTurn).thenReturn(finishPlayerTurn)
        }

    @Test
    fun `should finish player turn when finish turn is requested`() {
        // Given
        // When
        finishTurnPresenter.finishTurn()
        // Then
        verify(finishPlayerTurn).invoke()
    }

    @Test
    fun `should hide finish turn view when finish turn is hidden`() {
        // Given
        // When
        finishTurnPresenter.hideFinishTurn()
        // Then
        verify(finishTurnView).hide()
    }

    @Test
    fun `should hide finish turn view when player wins`() {
        // Given
        eventBus.publish(PlayerVictory(playerId = "player-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(finishTurnView).hide()
    }
}
