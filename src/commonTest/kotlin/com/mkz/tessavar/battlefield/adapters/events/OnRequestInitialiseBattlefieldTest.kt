package com.mkz.tessavar.battlefield.adapters.events

import com.mkz.tessavar.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.usecases.commands.InitialiseBattlefield
import com.mkz.tessavar.battlefield.usecases.services.BattlefieldCreation
import com.mkz.tessavar.battlefield.usecases.services.FakeBattlefieldLoader
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.tessavar.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import com.mkz.tessavar.terrain.usecases.queries.SearchTerrainById
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestInitialiseBattlefieldTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val terrainRepository = InMemoryTerrainRepository().also { it.create(terrainThatCanBeOccupied(id = "sand")) }
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
