package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BattlefieldMapperTest {
    @Test
    fun `Terrain should keep the size and the terrain of each tile when the battlefield has no transitions`() {
        // Given
        val battlefield = battlefield(rows = 1, columns = 2, tiles = listOf(listOf("sand", "sand"))).toDto()
        // When
        val terrain = BattlefieldMapper.terrain(battlefield)
        // Then
        terrain.rows shouldBe 1
        terrain.columns shouldBe 2
        terrain.tiles.getValue(TilePosition(0, 0)).terrainId shouldBe "sand"
        terrain.tiles.getValue(TilePosition(0, 1)).terrainId shouldBe "sand"
        terrain.tiles
            .getValue(TilePosition(0, 0))
            .transition
            .shouldBeNull()
    }

    @Test
    fun `Terrain should map the transition of a tile when it is next to another terrain`() {
        // Given
        val battlefield =
            battlefield(
                rows = 1,
                columns = 2,
                tiles = listOf(listOf("void", "sand")),
                terrainTransitionRules = setOf(BattlefieldMother.terrainTransitionRule(fromTerrainId = "sand", toTerrainId = "void")),
            ).toDto()
        val domainTransition = battlefield.tiles.getValue(Battlefield.Dto.PositionDto(0, 1)).terrainTransition!!
        // When
        val terrain = BattlefieldMapper.terrain(battlefield)
        // Then
        terrain.tiles.getValue(TilePosition(0, 1)).transition shouldBe
            TerrainTransition(
                fromTerrainId = domainTransition.fromTerrainId,
                toTerrainId = domainTransition.toTerrainId,
                wangIndex = domainTransition.wangIndex,
            )
    }

    @Test
    fun `Positions should keep the row and the column of each position when they are mapped`() {
        // When
        val positions = BattlefieldMapper.positions(listOf(Battlefield.Dto.PositionDto(2, 3), Battlefield.Dto.PositionDto(0, 1)))
        // Then
        positions shouldBe setOf(TilePosition(2, 3), TilePosition(0, 1))
    }
}
