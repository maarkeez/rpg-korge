package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.commands.TriggerTerrainEffects
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnRequestEffectApplicationToOccupantsTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val terrainRepository = InMemoryTerrainRepository()
    private val onRequestEffectApplicationToOccupants =
        OnRequestEffectApplicationToOccupants(
            TriggerTerrainEffects(battlefieldRepository, SearchTerrainById(terrainRepository), eventBus),
            eventBus,
        )
    private val requestedTargets =
        mutableListOf<String>().also { targets ->
            eventBus.subscribe<BattlefieldEvent.TerrainEffectTriggered> { targets += it.application.target.toString() }
        }

    @Test
    fun `should trigger the terrain effect for the requested occupants only when it is requested`() {
        // Given
        battlefieldRepository.create(battlefield("K L", occupants = mapOf('K' to "knight-1", 'L' to "knight-2")))
        terrainRepository.create(Terrain.create(Terrain.Dto(id = "sand", canBeOccupied = true, effectId = "effect-1")))
        eventBus.publish(BattlefieldEvent.RequestEffectApplicationToOccupants(battleUnitIds = setOf("knight-2")))
        // When
        eventBus.dispatch()
        // Then
        requestedTargets.shouldContainExactly("Unit(id=knight-2)")
    }
}
