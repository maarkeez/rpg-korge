package com.mkz.rpg.wandering.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.wandering.adapters.storage.InMemoryWanderingRepository
import com.mkz.rpg.wandering.domain.WanderingMother.wandering
import com.mkz.rpg.wandering.usecases.commands.ResolveRespawn
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

class OnBattleUnitRespawnedTest {
    private val eventBus = InMemoryEventBus()
    private val wanderingRepository = InMemoryWanderingRepository()
    private val onBattleUnitRespawned = OnBattleUnitRespawned(ResolveRespawn(wanderingRepository), eventBus)

    @Test
    fun `should stop counting the party steps when a cpu battle unit respawns`() {
        // Given
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (0 to 0))).defeat("rat-1"))
        eventBus.publish(BattleUnitEvent.BattleUnitRespawned(playerId = "cpu", battleUnitId = "rat-1", row = 0, column = 0))
        // When
        eventBus.dispatch()
        // Then
        wanderingRepository
            .search()!!
            .toDto()
            .respawns
            .shouldBeEmpty()
    }
}
