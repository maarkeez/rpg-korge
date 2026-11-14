package com.mkz.tessavar.battlefield.usecases.commands

import com.mkz.tessavar.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.usecases.services.BattlefieldCreation
import com.mkz.tessavar.battlefield.usecases.services.FakeBattlefieldLoader
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.tessavar.terrain.domain.TerrainMother
import com.mkz.tessavar.terrain.usecases.queries.SearchTerrainById
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialiseBattlefieldTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val terrainRepository = InMemoryTerrainRepository()
    private val eventBus = FakeEventBus()

    @Test
    fun `should create the battlefield with the size of the loaded tiles when the battlefield is initialised`() {
        // Given
        terrainRepository.create(TerrainMother.terrainThatCanBeOccupied(id = "sand"))
        val initialiseBattlefield =
            InitialiseBattlefield(FakeBattlefieldLoader(tiles = List(2) { List(3) { "sand" } }), BattlefieldCreation(battlefieldRepository, SearchTerrainById(terrainRepository)), eventBus)
        // When
        initialiseBattlefield()
        // Then
        val battlefield = battlefieldRepository.search()?.toDto()
        battlefield?.rows shouldBe 2
        battlefield?.columns shouldBe 3
        eventBus.shouldHavePublishedEvents(BattlefieldEvent.BattlefieldCreated)
    }
}
