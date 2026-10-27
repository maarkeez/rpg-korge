package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.usecases.commands.InitialiseBattlefield
import com.mkz.rpg.battlefield.usecases.services.BattlefieldCreation
import com.mkz.rpg.battlefield.usecases.services.FakeBattlefieldLoader
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestInitialiseBattlefieldTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val terrainRepository = InMemoryTerrainRepository().also { it.create(occupiableTerrain(id = "sand")) }
    private val onRequestInitialiseBattlefield =
        OnRequestInitialiseBattlefield(
            InitialiseBattlefield(FakeBattlefieldLoader(tiles = listOf(listOf("sand", "sand"))), BattlefieldCreation(battlefieldRepository, SearchTerrainById(terrainRepository)), eventBus),
            eventBus,
        )

    @Test
    fun `should initialise the battlefield when the request is dispatched`() {
        // Given
        eventBus.publish(BattlefieldEvent.RequestInitialiseBattlefield)
        // When
        eventBus.dispatch()
        // Then
        val battlefield = battlefieldRepository.search()?.toDto()
        battlefield?.rows shouldBe 1
        battlefield?.columns shouldBe 2
    }
}
