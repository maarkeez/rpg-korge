package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.commands.ApplyTerrainEffects
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnRequestTerrainEffectsTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val terrainRepository = InMemoryTerrainRepository()
    private val onRequestTerrainEffects =
        OnRequestTerrainEffects(
            ApplyTerrainEffects(battlefieldRepository, SearchTerrainById(terrainRepository), eventBus),
            eventBus,
        )
    private val requestedTargets =
        mutableListOf<String?>().also { targets ->
            eventBus.subscribe<BattlefieldEvent.TerrainEffectApplied> { targets += it.application.recipient.battleUnitId }
        }

    @Test
    fun `should apply the terrain effect for the requested occupants only when it is requested`() {
        // Given
        battlefieldRepository.create(battlefield("K L", occupants = mapOf('K' to "knight-1", 'L' to "knight-2")))
        terrainRepository.create(terrainThatCanBeOccupied(id = "sand", effectId = "effect-1"))
        eventBus.publish(BattlefieldEvent.RequestTerrainEffects(occupantIds = setOf("knight-2")))
        // When
        eventBus.dispatch()
        // Then
        requestedTargets.shouldContainExactly("knight-2")
    }
}
