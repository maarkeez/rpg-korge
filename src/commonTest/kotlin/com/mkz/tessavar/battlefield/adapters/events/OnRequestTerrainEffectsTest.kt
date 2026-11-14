package com.mkz.tessavar.battlefield.adapters.events

import com.mkz.tessavar.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.battlefield.usecases.commands.ApplyTerrainEffects
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.tessavar.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import com.mkz.tessavar.terrain.usecases.queries.SearchTerrainById
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
