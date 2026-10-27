package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldError.TileTerrainTransitionNotAllowed
import com.mkz.rpg.battlefield.usecases.services.BattlefieldCreation
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainMother.nonOccupiableTerrain
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PreviewBattlefieldTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val terrainRepository =
        InMemoryTerrainRepository().apply {
            create(occupiableTerrain(id = "sand", allowedTransitionTo = setOf("lava", "water")))
            create(nonOccupiableTerrain(id = "lava"))
            create(occupiableTerrain(id = "water"))
        }
    private val previewBattlefield = PreviewBattlefield(BattlefieldCreation(battlefieldRepository, SearchTerrainById(terrainRepository)))

    @Test
    fun `should build the battlefield the battle would create when the tiles are previewed`() {
        // Given
        val tiles = listOf(listOf("sand", "lava", "sand"), listOf("sand", "sand", "water"))
        val battleRepository = InMemoryBattlefieldRepository()
        BattlefieldCreation(battleRepository, SearchTerrainById(terrainRepository)).create(rows = 2, columns = 3, tiles = tiles)
        // When
        val battlefield = previewBattlefield(tiles)
        // Then
        battlefield shouldBe battleRepository.search()!!.toDto()
        battlefield.tiles
            .getValue(PositionDto(0, 0))
            .terrainTransition
            ?.toTerrainId shouldBe "lava"
    }

    @Test
    fun `should not store the battlefield when the tiles are previewed`() {
        // Given
        val tiles = listOf(listOf("sand"))
        // When
        previewBattlefield(tiles)
        // Then
        battlefieldRepository.search().shouldBeNull()
    }

    @Test
    fun `should fail with the terrains when two terrains without a transition are next to each other`() {
        // Given
        val tiles = listOf(listOf("lava", "water"))
        // When
        val preview = { previewBattlefield(tiles) }
        // Then
        val error = shouldThrow<TileTerrainTransitionNotAllowed> { preview() }
        (error.terrainId to error.transitionToTerrainId) shouldBe ("lava" to "water")
    }
}
