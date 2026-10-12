package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.usecases.services.BattlefieldLoader
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.terrain.domain.TerrainMother
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InitialiseBattlefieldTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchTerrainById: SearchTerrainById = mock()
    private val eventBus = FakeEventBus()
    private val battlefieldLoader: BattlefieldLoader = mock()
    private val initialiseBattlefield = InitialiseBattlefield(battlefieldLoader, battlefieldRepository, searchTerrainById, eventBus)

    @Test
    fun `should create the battlefield with the size of the loaded tiles when the battlefield is initialised`() {
        // Given
        whenever(battlefieldLoader.loadTiles()).thenReturn(List(2) { List(3) { "sand" } })
        whenever(searchTerrainById(any())).thenReturn(TerrainMother.occupiableTerrain(id = "sand").toDto())
        // When
        initialiseBattlefield()
        // Then
        val battlefield = battlefieldRepository.search()?.toDto()
        battlefield?.rows shouldBe 2
        battlefield?.columns shouldBe 3
        eventBus.shouldHavePublishedEvents(BattlefieldEvent.BattlefieldCreated)
    }
}
