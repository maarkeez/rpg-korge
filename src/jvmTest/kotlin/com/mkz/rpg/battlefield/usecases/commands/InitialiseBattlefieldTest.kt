package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.usecases.services.FakeBattlefieldLoader
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainMother
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class InitialiseBattlefieldTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val terrainRepository = InMemoryTerrainRepository()
    private val eventBus = FakeEventBus()

    @Test
    fun `should create the battlefield with the size of the loaded tiles when the battlefield is initialised`() {
        // Given
        terrainRepository.create(TerrainMother.occupiableTerrain(id = "sand"))
        val initialiseBattlefield =
            InitialiseBattlefield(FakeBattlefieldLoader(tiles = List(2) { List(3) { "sand" } }), battlefieldRepository, SearchTerrainById(terrainRepository), eventBus)
        // When
        initialiseBattlefield()
        // Then
        val battlefield = battlefieldRepository.search()?.toDto()
        battlefield?.rows shouldBe 2
        battlefield?.columns shouldBe 3
        eventBus.shouldHavePublishedEvents(BattlefieldEvent.BattlefieldCreated)
    }
}
