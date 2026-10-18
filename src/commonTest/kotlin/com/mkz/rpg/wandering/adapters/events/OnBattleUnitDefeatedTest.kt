package com.mkz.rpg.wandering.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.wandering.adapters.storage.InMemoryWanderingRepository
import com.mkz.rpg.wandering.domain.Wandering
import com.mkz.rpg.wandering.domain.WanderingMother.wandering
import com.mkz.rpg.wandering.usecases.commands.AwaitRespawn
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleUnitDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val wanderingRepository = InMemoryWanderingRepository()
    private val onBattleUnitDefeated = OnBattleUnitDefeated(AwaitRespawn(wanderingRepository), eventBus)

    @Test
    fun `should start counting the party steps when a cpu battle unit is defeated`() {
        // Given
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (0 to 0))))
        eventBus.publish(BattleUnitEvent.BattleUnitDefeated(playerId = "cpu", battleUnitId = "rat-1", defeatedAtRow = 1, defeatedAtColumn = 1))
        // When
        eventBus.dispatch()
        // Then
        wanderingRepository
            .search()!!
            .toDto()
            .respawns
            .shouldContainExactly(Wandering.Dto.RespawnDto(battleUnitId = "rat-1", partySteps = 0))
    }
}
