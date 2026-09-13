package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.ReceiveAbilityEffects
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class OnAbilityCastedTest {
    private val eventBus = InMemoryEventBus()
    private val receiveAbilityEffects: ReceiveAbilityEffects = mock()
    private val onAbilityCasted =
        OnAbilityCasted(
            receiveAbilityEffects = receiveAbilityEffects,
            eventBus = eventBus,
        )

    @Test
    fun `should receive ability effects for all the positions in the cast group when an ability is casted`() {
        // Given
        val battleUnitId = "battle-unit-1"
        val abilityId = "ability-1"
        val castGroup =
            listOf(
                Battlefield.Dto.PositionDto(row = 1, column = 2),
                Battlefield.Dto.PositionDto(row = 2, column = 1),
            )
        eventBus.publish(
            BattleUnitEvent.AbilityCasted(
                battleUnitId = battleUnitId,
                abilityId = abilityId,
                castGroup = castGroup,
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        verify(receiveAbilityEffects).invoke(
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            row = 1,
            column = 2,
        )
        verify(receiveAbilityEffects).invoke(
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            row = 2,
            column = 1,
        )
    }

    @Test
    fun `should not receive ability effects when a different event type is dispatched`() {
        // Given
        eventBus.publish(BattleUnitEvent.BattleUnitDeployed(battleUnitId = "battle-unit-1", row = 0, column = 0))
        // When
        eventBus.dispatch()
        // Then
        verify(receiveAbilityEffects, never()).invoke(any(), any(), any(), any())
    }
}
