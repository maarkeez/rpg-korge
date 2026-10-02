package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother.terrainTransitionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CanBattlefieldTileBeOccupiedTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val canBattlefieldTileBeOccupied = CanBattlefieldTileBeOccupied(battlefieldRepository)

    @Test
    fun `should return true when the tile is vacant and within boundaries`() {
        // Given
        battlefieldRepository.create(
            battlefield(),
        )
        // When
        val result = canBattlefieldTileBeOccupied(1, 1)
        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when the tile is occupied`() {
        // Given
        val battlefield =
            battlefield()
                .occupy(1, 1, "battle-unit-1")
                .pullEvents()
                .second
        battlefieldRepository.create(battlefield)
        // When
        val result = canBattlefieldTileBeOccupied(1, 1)
        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when the tile is out of boundaries`() {
        // Given
        battlefieldRepository.create(
            battlefield(),
        )
        // When
        val result = canBattlefieldTileBeOccupied(5, 5)
        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `should return true when the tile terrain can be occupied and has a single non occupiable adjacent terrain`() {
        // Given
        battlefieldRepository.create(
            battlefield(
                rows = 2,
                columns = 2,
                tiles =
                    listOf(
                        listOf("void", "sand"),
                        listOf("sand", "sand"),
                    ),
                terrainTransitionRules = setOf(terrainTransitionRule(fromTerrainId = "sand", toTerrainId = "void")),
            ),
        )
        // When
        val result = canBattlefieldTileBeOccupied(1, 0)
        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when the tile terrain can not be occupied`() {
        // Given
        battlefieldRepository.create(
            battlefield(
                rows = 1,
                columns = 1,
                tiles = listOf(listOf("void")),
            ),
        )
        // When
        val result = canBattlefieldTileBeOccupied(0, 0)
        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when the tile terrain transition can not be occupied`() {
        // Given
        battlefieldRepository.create(
            battlefield(
                rows = 2,
                columns = 2,
                tiles =
                    listOf(
                        listOf("void", "void"),
                        listOf("sand", "void"),
                    ),
                terrainTransitionRules = setOf(terrainTransitionRule(fromTerrainId = "sand", toTerrainId = "void")),
            ),
        )
        // When
        val result = canBattlefieldTileBeOccupied(1, 0)
        // Then
        assertThat(result).isFalse()
    }
}
