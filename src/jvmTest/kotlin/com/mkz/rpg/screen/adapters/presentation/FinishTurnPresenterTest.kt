package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.battle.usecases.commands.FinishPlayerTurn
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FinishTurnPresenterTest {
    private val playerCallToActionView = mock<PlayerCallToActionView>()
    private val finishPlayerTurn = mock<FinishPlayerTurn>()
    private val battleApi = mock<BattleApi>().also { whenever(it.finishPlayerTurn).thenReturn(finishPlayerTurn) }
    private val eventBus = InMemoryEventBus()

    @Test
    fun `should display finish turn when is created`() {
        // Given
        // When
        FinishTurnPresenter(playerCallToActionView, battleApi, eventBus)
        // Then
        verify(playerCallToActionView).displayFinishTurn(any())
    }

    @Test
    fun `should finish the player turn when the player finishes the turn`() {
        // Given
        FinishTurnPresenter(playerCallToActionView, battleApi, eventBus)
        val onTurnFinished = argumentCaptor<() -> Unit>()
        verify(playerCallToActionView).displayFinishTurn(onTurnFinished.capture())
        // When
        onTurnFinished.firstValue()
        // Then
        verify(finishPlayerTurn).invoke()
    }

    @Test
    fun `should hide the call to action when a player wins`() {
        // Given
        FinishTurnPresenter(playerCallToActionView, battleApi, eventBus)
        eventBus.publish(PlayerVictory(playerId = "player-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(playerCallToActionView).hide()
    }
}
