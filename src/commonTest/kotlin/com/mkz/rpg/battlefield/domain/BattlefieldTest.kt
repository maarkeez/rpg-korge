package com.mkz.rpg.battlefield.domain

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class BattlefieldTest {
    @Test
    fun `Create should create a battlefield when the tile matrix matches the rows and columns`() {
        // Given
        val rows = 2
        val columns = 3
        val tiles = List(rows) { List(columns) { BattlefieldMother.terrainId() } }
        // When
        val createdBattlefield =
            Battlefield
                .create(
                    rows = rows,
                    columns = columns,
                    tiles = tiles,
                    terrainCanBeOccupied = BattlefieldMother.terrainOccupancy(tiles),
                    terrainTransitionRules = BattlefieldMother.terrainTransitionRules(tiles),
                )
        // Then
        val battlefieldDto = createdBattlefield.toDto()
        battlefieldDto.rows shouldBe rows
        battlefieldDto.columns shouldBe columns
        battlefieldDto.tiles.shouldHaveSize(rows * columns)
    }

    @Test
    fun `Create should publish the battlefield created event when the battlefield is created`() {
        // Given
        val tiles = List(3) { List(3) { BattlefieldMother.terrainId() } }
        // When
        val createdBattlefield =
            Battlefield
                .create(
                    rows = 3,
                    columns = 3,
                    tiles = tiles,
                    terrainCanBeOccupied = BattlefieldMother.terrainOccupancy(tiles),
                    terrainTransitionRules = BattlefieldMother.terrainTransitionRules(tiles),
                )
        // Then
        val (events, _) = createdBattlefield.pullEvents()
        events.shouldContainExactly(BattlefieldEvent.BattlefieldCreated)
    }

    @Test
    fun `Create should store the terrain transition on the tile when the tile terrain has a transition rule and is adjacent to the target terrain`() {
        // Given
        val tiles = listOf(listOf("void", "sand"))
        // When
        val createdBattlefield =
            Battlefield
                .create(
                    rows = 1,
                    columns = 2,
                    tiles = tiles,
                    terrainCanBeOccupied = BattlefieldMother.terrainOccupancy(tiles),
                    terrainTransitionRules = setOf(BattlefieldMother.terrainTransitionRule(fromTerrainId = "sand", toTerrainId = "void")),
                )
        // Then
        val sandTile = createdBattlefield.toDto().tiles.getValue(Battlefield.Dto.PositionDto(row = 0, column = 1))
        sandTile.terrainTransition shouldBe Battlefield.Dto.TerrainTransitionDto(fromTerrainId = "sand", toTerrainId = "void", wangIndex = 8)
    }

    @Test
    fun `Create should store the terrain transition wang index with the target terrain sides when the tile is adjacent to the target terrain on multiple sides`() {
        // Given
        val tiles =
            listOf(
                listOf("void", "void", "sand"),
                listOf("void", "sand", "sand"),
                listOf("sand", "sand", "sand"),
            )
        // When
        val createdBattlefield =
            Battlefield
                .create(
                    rows = 3,
                    columns = 3,
                    tiles = tiles,
                    terrainCanBeOccupied = BattlefieldMother.terrainOccupancy(tiles),
                    terrainTransitionRules = setOf(BattlefieldMother.terrainTransitionRule(fromTerrainId = "sand", toTerrainId = "void")),
                )
        // Then
        val sandTile = createdBattlefield.toDto().tiles.getValue(Battlefield.Dto.PositionDto(row = 1, column = 1))
        sandTile.terrainTransition?.wangIndex shouldBe 9
    }

    @Test
    fun `Create should store the terrain transition to the adjacent target terrain when the tile terrain has multiple transition rules`() {
        // Given
        val tiles = listOf(listOf("grass", "sand"))
        // When
        val createdBattlefield =
            Battlefield
                .create(
                    rows = 1,
                    columns = 2,
                    tiles = tiles,
                    terrainCanBeOccupied = BattlefieldMother.terrainOccupancy(tiles),
                    terrainTransitionRules =
                        setOf(
                            BattlefieldMother.terrainTransitionRule(fromTerrainId = "sand", toTerrainId = "void"),
                            BattlefieldMother.terrainTransitionRule(fromTerrainId = "sand", toTerrainId = "grass"),
                        ),
                )
        // Then
        val sandTile = createdBattlefield.toDto().tiles.getValue(Battlefield.Dto.PositionDto(row = 0, column = 1))
        sandTile.terrainTransition shouldBe Battlefield.Dto.TerrainTransitionDto(fromTerrainId = "sand", toTerrainId = "grass", wangIndex = 8)
    }

    @Test
    fun `Create should store no terrain transition on the tile when the tile is not adjacent to the target terrain`() {
        // Given
        val tiles = listOf(listOf("sand", "sand"))
        // When
        val createdBattlefield =
            Battlefield
                .create(
                    rows = 1,
                    columns = 2,
                    tiles = tiles,
                    terrainCanBeOccupied = BattlefieldMother.terrainOccupancy(tiles),
                    terrainTransitionRules = setOf(BattlefieldMother.terrainTransitionRule(fromTerrainId = "sand", toTerrainId = "void")),
                )
        // Then
        val sandTile = createdBattlefield.toDto().tiles.getValue(Battlefield.Dto.PositionDto(row = 0, column = 0))
        sandTile.terrainTransition.shouldBeNull()
    }

    @Test
    fun `Create should store no terrain transition on the tile when the tile terrain has no transition rule`() {
        // Given
        val tiles = listOf(listOf("void", "sand"))
        // When
        val createdBattlefield =
            Battlefield
                .create(
                    rows = 1,
                    columns = 2,
                    tiles = tiles,
                    terrainCanBeOccupied = BattlefieldMother.terrainOccupancy(tiles),
                    terrainTransitionRules = setOf(BattlefieldMother.terrainTransitionRule(fromTerrainId = "sand", toTerrainId = "void")),
                )
        // Then
        val voidTile = createdBattlefield.toDto().tiles.getValue(Battlefield.Dto.PositionDto(row = 0, column = 0))
        voidTile.terrainTransition.shouldBeNull()
    }

    @Test
    fun `Create should fail when the tile is adjacent to a different terrain and no transition rule exists`() {
        // Given
        val tiles = listOf(listOf("sand", "void"))
        // When
        val result =
            runCatching {
                Battlefield
                    .create(
                        rows = 1,
                        columns = 2,
                        tiles = tiles,
                        terrainCanBeOccupied = BattlefieldMother.terrainOccupancy(tiles),
                        terrainTransitionRules = emptySet(),
                    )
            }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattlefieldError.TileTerrainTransitionNotAllowed>()
    }

    @Test
    fun `Create should create the battlefield when the tile is adjacent to a different terrain and a transition rule exists in the reverse direction`() {
        // Given
        val tiles = listOf(listOf("sand", "void"))
        // When
        val createdBattlefield =
            Battlefield
                .create(
                    rows = 1,
                    columns = 2,
                    tiles = tiles,
                    terrainCanBeOccupied = BattlefieldMother.terrainOccupancy(tiles),
                    terrainTransitionRules = setOf(BattlefieldMother.terrainTransitionRule(fromTerrainId = "void", toTerrainId = "sand")),
                )
        // Then
        createdBattlefield.toDto().tiles.shouldHaveSize(2)
    }

    @Test
    fun `ToDto should expose the rows, columns and tiles when converted to dto`() {
        // Given
        val battlefield = BattlefieldMother.battlefield(rows = 2, columns = 3)
        // When
        val result = battlefield.toDto()
        // Then
        result.rows shouldBe 2
        result.columns shouldBe 3
        result.tiles.shouldHaveSize(6)
    }

    @Test
    fun `Occupy should occupy the tile and publish the event when the tile is vacant`() {
        // Given
        val battlefield = BattlefieldMother.battlefield()
        val occupantId = "battle-unit-1"
        // When
        val occupiedBattlefield = battlefield.occupy(row = 0, column = 0, occupantId = occupantId)
        // Then
        occupiedBattlefield.occupant(row = 0, column = 0) shouldBe occupantId
        val (events, _) = occupiedBattlefield.pullEvents()
        events.shouldContainExactly(BattlefieldEvent.BattlefieldTileOccupied(row = 0, column = 0, occupantId = occupantId))
    }

    @Test
    fun `Occupy should free the previous tile when the battle unit occupies a new tile`() {
        // Given
        val occupantId = "battle-unit-1"
        val battlefield = BattlefieldMother.battlefield().occupy(row = 0, column = 0, occupantId = occupantId)
        // When
        val occupiedBattlefield = battlefield.occupy(row = 0, column = 1, occupantId = occupantId)
        // Then
        occupiedBattlefield.occupant(row = 0, column = 0).shouldBeNull()
        occupiedBattlefield.occupant(row = 0, column = 1) shouldBe occupantId
    }

    @Test
    fun `Occupy should fail when the tile is not vacant`() {
        // Given
        val battlefield = BattlefieldMother.battlefield().occupy(row = 0, column = 0, occupantId = "battle-unit-1")
        // When
        val result = runCatching { battlefield.occupy(row = 0, column = 0, occupantId = "battle-unit-2") }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattlefieldError.TileIsNotVacant>()
    }

    @Test
    fun `Occupy should fail when the tile is not found`() {
        // Given
        val battlefield = BattlefieldMother.battlefield()
        // When
        val result = runCatching { battlefield.occupy(row = 5, column = 5, occupantId = "battle-unit-1") }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattlefieldError.TileNotFound>()
    }

    @Test
    fun `RemoveOccupant should remove the occupant and publish the event when the battle unit is deployed`() {
        // Given
        val occupantId = "battle-unit-1"
        val battlefield =
            BattlefieldMother
                .battlefield()
                .occupy(row = 1, column = 1, occupantId = occupantId)
                .pullEvents()
                .second
        // When
        val updatedBattlefield = battlefield.removeOccupant(occupantId = occupantId)
        // Then
        updatedBattlefield.occupant(row = 1, column = 1).shouldBeNull()
        val (events, _) = updatedBattlefield.pullEvents()
        events.shouldContainExactly(BattlefieldEvent.OccupantRemoved(occupantId = occupantId, row = 1, column = 1))
    }

    @Test
    fun `RemoveOccupant should not update the battlefield when the battle unit is not deployed`() {
        // Given
        val battlefield = BattlefieldMother.battlefield()
        // When
        val updatedBattlefield = battlefield.removeOccupant(occupantId = "battle-unit-1")
        // Then
        updatedBattlefield shouldBe battlefield
    }

    @Test
    fun `SwapOccupants should swap the tiles of both occupants at once and announce both tiles when both are deployed`() {
        // Given
        val battlefield =
            BattlefieldMother
                .battlefield()
                .occupy(row = 1, column = 1, occupantId = "knight-1")
                .occupy(row = 2, column = 1, occupantId = "knight-2")
                .pullEvents()
                .second
        // When
        val updatedBattlefield = battlefield.swapOccupants(occupantId = "knight-1", otherOccupantId = "knight-2")
        // Then
        updatedBattlefield.occupant(row = 1, column = 1) shouldBe "knight-2"
        updatedBattlefield.occupant(row = 2, column = 1) shouldBe "knight-1"
        updatedBattlefield.pullEvents().first.shouldContainExactly(
            BattlefieldEvent.BattlefieldTileOccupied(row = 2, column = 1, occupantId = "knight-1"),
            BattlefieldEvent.BattlefieldTileOccupied(row = 1, column = 1, occupantId = "knight-2"),
        )
    }

    @Test
    fun `SwapOccupants should not update the battlefield when an occupant is not deployed`() {
        // Given
        val battlefield =
            BattlefieldMother
                .battlefield()
                .occupy(row = 1, column = 1, occupantId = "knight-1")
                .pullEvents()
                .second
        // When
        val updatedBattlefield = battlefield.swapOccupants(occupantId = "knight-1", otherOccupantId = "knight-2")
        // Then
        updatedBattlefield shouldBe battlefield
    }

    @Test
    fun `CanBeOccupied should be true when the tile is vacant`() {
        // Given
        val battlefield = BattlefieldMother.battlefield()
        // When
        val result = battlefield.canBeOccupied(row = 0, column = 0)
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `CanBeOccupied should be false when the tile is occupied`() {
        // Given
        val battlefield = BattlefieldMother.battlefield().occupy(row = 0, column = 0, occupantId = "battle-unit-1")
        // When
        val result = battlefield.canBeOccupied(row = 0, column = 0)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `CanBeOccupied should be true when the tile terrain can be occupied and has a single non occupiable adjacent terrain`() {
        // Given
        val tiles =
            listOf(
                listOf("void", "sand"),
                listOf("sand", "sand"),
            )
        val battlefield =
            BattlefieldMother
                .battlefield(rows = 2, columns = 2, tiles = tiles, terrainTransitionRules = BattlefieldMother.terrainTransitionRules(tiles))
        // When
        val result = battlefield.canBeOccupied(row = 1, column = 0)
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `CanBeOccupied should be false when the tile terrain can not be occupied`() {
        // Given
        val battlefield =
            BattlefieldMother.battlefield(
                rows = 1,
                columns = 1,
                tiles = listOf(listOf("void")),
            )
        // When
        val result = battlefield.canBeOccupied(row = 0, column = 0)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `CanBeOccupied should be false when the tile terrain transition can not be occupied`() {
        // Given
        val tiles =
            listOf(
                listOf("void", "void"),
                listOf("sand", "void"),
            )
        val battlefield =
            BattlefieldMother
                .battlefield(rows = 2, columns = 2, tiles = tiles, terrainTransitionRules = BattlefieldMother.terrainTransitionRules(tiles))
        // When
        val result = battlefield.canBeOccupied(row = 1, column = 0)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `Occupant should return the battle unit id when the tile is occupied`() {
        // Given
        val occupantId = "battle-unit-1"
        val battlefield = BattlefieldMother.battlefield().occupy(row = 0, column = 0, occupantId = occupantId)
        // When
        val result = battlefield.occupant(row = 0, column = 0)
        // Then
        result shouldBe occupantId
    }

    @Test
    fun `Occupant should return null when the tile is vacant`() {
        // Given
        val battlefield = BattlefieldMother.battlefield()
        // When
        val result = battlefield.occupant(row = 0, column = 0)
        // Then
        result.shouldBeNull()
    }

    @Test
    fun `Position should return the position when the battle unit is deployed`() {
        // Given
        val occupantId = "battle-unit-1"
        val battlefield = BattlefieldMother.battlefield().occupy(row = 1, column = 2, occupantId = occupantId)
        // When
        val result = battlefield.position(occupantId = occupantId)
        // Then
        result shouldBe Battlefield.Dto.PositionDto(row = 1, column = 2)
    }

    @Test
    fun `Position should return null when the battle unit is not deployed`() {
        // Given
        val battlefield = BattlefieldMother.battlefield()
        // When
        val result = battlefield.position(occupantId = "battle-unit-1")
        // Then
        result.shouldBeNull()
    }

    @Test
    fun `EffectApplicationsToOccupants should return an application per occupied tile when the tile terrain has an effect assigned`() {
        // Given
        val battlefield =
            BattlefieldMother
                .battlefield()
                .occupy(row = 0, column = 0, occupantId = "battle-unit-1")
                .occupy(row = 2, column = 1, occupantId = "battle-unit-2")
        // When
        val result = battlefield.effectApplicationsToOccupants(terrainEffectIds = mapOf("sand" to "effect-1"))
        // Then
        result.shouldContainExactly(
            Battlefield.Dto.EffectApplicationToOccupantDto(occupantId = "battle-unit-1", terrainId = "sand", effectId = "effect-1"),
            Battlefield.Dto.EffectApplicationToOccupantDto(occupantId = "battle-unit-2", terrainId = "sand", effectId = "effect-1"),
        )
    }

    @Test
    fun `EffectApplicationsToOccupants should not return an application when the tile is vacant`() {
        // Given
        val battlefield = BattlefieldMother.battlefield()
        // When
        val result = battlefield.effectApplicationsToOccupants(terrainEffectIds = mapOf("sand" to "effect-1"))
        // Then
        result.shouldBeEmpty()
    }

    @Test
    fun `EffectApplicationsToOccupants should not return an application when the tile terrain has no effect assigned`() {
        // Given
        val battlefield = BattlefieldMother.battlefield().occupy(row = 0, column = 0, occupantId = "battle-unit-1")
        // When
        val result = battlefield.effectApplicationsToOccupants(terrainEffectIds = emptyMap())
        // Then
        result.shouldBeEmpty()
    }

    @Test
    fun `EffectApplicationsToOccupants should return only the applications of the tiles on the terrains with an effect assigned when the battlefield has multiple terrains`() {
        // Given
        val tiles = listOf(listOf("sand", "forest"), listOf("sand", "forest"))
        val battlefield =
            BattlefieldMother
                .battlefield(rows = 2, columns = 2, tiles = tiles, terrainTransitionRules = BattlefieldMother.terrainTransitionRules(tiles))
                .occupy(row = 0, column = 0, occupantId = "battle-unit-1")
                .occupy(row = 0, column = 1, occupantId = "battle-unit-2")
        // When
        val result = battlefield.effectApplicationsToOccupants(terrainEffectIds = mapOf("sand" to "effect-1"))
        // Then
        result.shouldContainExactly(
            Battlefield.Dto.EffectApplicationToOccupantDto(occupantId = "battle-unit-1", terrainId = "sand", effectId = "effect-1"),
        )
    }

    @Test
    fun `IsInBoundaries should be true when the position is within the battlefield`() {
        // Given
        val battlefield = BattlefieldMother.battlefield(rows = 2, columns = 4)
        // When
        val result = battlefield.isInBoundaries(row = 1, column = 3)
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `IsInBoundaries should be false when the row is outside the battlefield`() {
        // Given
        val battlefield = BattlefieldMother.battlefield(rows = 2, columns = 4)
        // When
        val result = battlefield.isInBoundaries(row = 2, column = 0)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `IsInBoundaries should be false when the column is outside the battlefield`() {
        // Given
        val battlefield = BattlefieldMother.battlefield(rows = 4, columns = 2)
        // When
        val result = battlefield.isInBoundaries(row = 0, column = 2)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `PullEvents should pull the pending events and clear them when the battlefield has events`() {
        // Given
        val battlefield = BattlefieldMother.battlefield().occupy(row = 0, column = 0, occupantId = "battle-unit-1")
        // When
        val (events, clearedBattlefield) = battlefield.pullEvents()
        // Then
        events.shouldContainExactly(BattlefieldEvent.BattlefieldTileOccupied(row = 0, column = 0, occupantId = "battle-unit-1"))
        clearedBattlefield.pullEvents().first.shouldBeEmpty()
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // Given
        val battlefield = BattlefieldMother.battlefield()
        // When
        val (events, _) = battlefield.pullEvents()
        // Then
        events.shouldBeEmpty()
    }
}
