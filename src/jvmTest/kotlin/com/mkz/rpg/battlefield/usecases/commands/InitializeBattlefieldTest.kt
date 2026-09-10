package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.terrainId
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InitializeBattlefieldTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val eventBus = FakeEventBus()
    private val initializeBattlefield =
        InitializeBattlefield(
            battlefieldRepository = battlefieldRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should create battlefield when no battlefield exists`() {
        // Given
        val rows = 3
        val columns = 4
        val tiles =
            List(rows) {
                List(columns) {
                    terrainId()
                }
            }
        // When
        initializeBattlefield(rows = rows, columns = columns, tiles = tiles)
        // Then
        val storedBattlefield = battlefieldRepository.search()?.toDto()
        assertThat(storedBattlefield).isNotNull
        assertThat(storedBattlefield!!.rows).isEqualTo(rows)
        assertThat(storedBattlefield.columns).isEqualTo(columns)
        assertThat(storedBattlefield.tiles).hasSize(rows * columns)
        assertThat(storedBattlefield.tiles.values)
            .allSatisfy { tile -> assertThat(tile.battleUnitId).isNull() }
        assertThat(eventBus)
            .hasPublishedEvents(BattlefieldEvent.BattlefieldCreated)
    }

    @Test
    fun `should not create battlefield when a battlefield already exists`() {
        // Given
        val existingBattlefield =
            com.mkz.rpg.battlefield.domain.BattlefieldMother
                .battlefield()
        battlefieldRepository.create(existingBattlefield)
        // When
        initializeBattlefield(rows = 3, columns = 3, tiles = emptyList())
        // Then
        assertThat(battlefieldRepository.search()?.toDto())
            .isEqualTo(existingBattlefield.toDto())
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
