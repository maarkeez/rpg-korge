package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.usecases.commands.ApplyOnTurnStartedEffects
import com.mkz.rpg.battleUnit.usecases.commands.ReplenishMana
import com.mkz.rpg.battleUnit.usecases.commands.ResetBattleUnitActionsAndReduceCooldowns
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class OnPlayerTurnStartedTest {
    private val eventBus = InMemoryEventBus()
    private val resetBattleUnitActionsAndReduceCooldowns: ResetBattleUnitActionsAndReduceCooldowns = mock()
    private val applyOnTurnStartedEffects: ApplyOnTurnStartedEffects = mock()
    private val replenishMana: ReplenishMana = mock()
    private val onPlayerTurnStarted =
        OnPlayerTurnStarted(
            resetBattleUnitActionsAndReduceCooldowns = resetBattleUnitActionsAndReduceCooldowns,
            applyOnTurnStartedEffects = applyOnTurnStartedEffects,
            replenishMana = replenishMana,
            eventBus = eventBus,
        )

    @Test
    fun `should reset actions, apply on turn started effects and replenish mana when a player turn starts`() {
        // Given
        val playerId = "player-1"
        eventBus.publish(BattleEvent.PlayerTurnStarted(playerId = playerId))
        // When
        eventBus.dispatch()
        // Then
        verify(resetBattleUnitActionsAndReduceCooldowns).invoke(playerId = playerId)
        verify(applyOnTurnStartedEffects).invoke(playerId = playerId)
        verify(replenishMana).invoke(playerId = playerId)
    }

    @Test
    fun `should not reset actions, apply on turn started effects or replenish mana when a different event type is dispatched`() {
        // Given
        eventBus.publish(BattleEvent.PlayerDefeated(playerId = "player-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(resetBattleUnitActionsAndReduceCooldowns, never()).invoke(any())
        verify(applyOnTurnStartedEffects, never()).invoke(any())
        verify(replenishMana, never()).invoke(any())
    }
}
