package com.mkz.rpg.battlefieldHud.usecases.commands

import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.commands.MoveBattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.castGroup
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.tile
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.battlefieldHud.usecases.commands.ProcessTileSelected
import com.mkz.rpg.screen.battlefieldHud.usecases.services.MovementService
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ProcessTileSelectedTest {
    private val searchOccupant: SearchOccupant = mock()
    private val searchBattleUnitById: SearchBattleUnitById = mock()
    private val moveBattleUnit: MoveBattleUnit = mock()
    private val searchPlayerById: SearchPlayerById = mock()
    private val searchBattle: SearchBattle = mock()
    private val movementService: MovementService = mock()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val processTileSelected =
        ProcessTileSelected(
            searchOccupant = searchOccupant,
            searchBattleUnitById = searchBattleUnitById,
            moveBattleUnit = moveBattleUnit,
            searchPlayerById = searchPlayerById,
            searchBattle = searchBattle,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )

    private fun battleUnitDto(
        id: String,
        playerId: String,
    ): BattleUnit.Dto =
        battleUnit(
            player =
                com.mkz.rpg.player.domain.PlayerMother
                    .player(id = playerId)
                    .toDto(),
        ).toDto()
            .copy(id = id, playerId = playerId)

    @Test
    fun `should select the battle unit when a tile occupied by a battle unit is selected while idle`() {
        // Given
        val tilesWhereCanBeMoved =
            setOf(
                tile(1, 3),
            )
        battlefieldHudRepository.create(
            com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
                .idle(),
        )
        whenever(searchOccupant(1, 2)).thenReturn("battle-unit-1")
        whenever(movementService.tilesWhereCanMove("battle-unit-1")).thenReturn(tilesWhereCanBeMoved)
        // When
        processTileSelected(1, 2)
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayMovementRange
        assertThat(storedHud.battleUnitId).isEqualTo("battle-unit-1")
        assertThat(storedHud.tile).isEqualTo(
            tile(1, 2),
        )
        assertThat(storedHud.tilesWhereCanBeMoved).isEqualTo(tilesWhereCanBeMoved)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnit(
                tile =
                    tile(1, 2),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            ),
        )
    }

    @Test
    fun `should go idle when an empty tile is selected while idle`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
                .idle(),
        )
        whenever(searchOccupant(0, 0)).thenReturn(null)
        // When
        processTileSelected(0, 0)
        // Then
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should move the battle unit when a tile within the movement range is selected`() {
        // Given
        val hud =
            displayMovementRange(
                tile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved =
                    setOf(
                        tile(2, 3),
                    ),
            )
        battlefieldHudRepository.create(hud)
        whenever(searchOccupant(2, 3)).thenReturn(null)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchPlayerById("player-1")).thenReturn(
            com.mkz.rpg.player.domain.PlayerMother
                .player(id = "player-1", type = Player.Dto.PlayerTypeDto.HUMAN)
                .toDto(),
        )
        // When
        processTileSelected(2, 3)
        // Then
        verify(moveBattleUnit).invoke(battleUnitId = "battle-unit-1", moveToRow = 2, moveToColumn = 3)
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus.publishedEvents).isEmpty()
    }

    @Test
    fun `should go idle when a tile outside the movement range is selected`() {
        // Given
        battlefieldHudRepository.create(
            displayMovementRange(
                tile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved =
                    setOf(
                        tile(2, 3),
                    ),
            ),
        )
        whenever(searchOccupant(5, 5)).thenReturn(null)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchPlayerById("player-1")).thenReturn(
            com.mkz.rpg.player.domain.PlayerMother
                .player(id = "player-1", type = Player.Dto.PlayerTypeDto.HUMAN)
                .toDto(),
        )
        // When
        processTileSelected(5, 5)
        // Then
        verify(moveBattleUnit, never()).invoke(eq("battle-unit-1"), eq(5), eq(5))
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should go idle without moving the battle unit when it belongs to a non-human player`() {
        // Given
        battlefieldHudRepository.create(
            displayMovementRange(
                tile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved =
                    setOf(
                        tile(2, 3),
                    ),
            ),
        )
        whenever(searchOccupant(2, 3)).thenReturn(null)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchPlayerById("player-1")).thenReturn(
            com.mkz.rpg.player.domain.PlayerMother
                .player(id = "player-1", type = Player.Dto.PlayerTypeDto.CPU)
                .toDto(),
        )
        // When
        processTileSelected(2, 3)
        // Then
        verify(moveBattleUnit, never()).invoke(eq("battle-unit-1"), eq(2), eq(3))
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should go idle when the tile is occupied by the same battle unit`() {
        // Given
        battlefieldHudRepository.create(
            displayMovementRange(
                tile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
            ),
        )
        whenever(searchOccupant(1, 1)).thenReturn("battle-unit-1")
        // When
        processTileSelected(1, 1)
        // Then
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should select the other battle unit when the tile is occupied by another battle unit`() {
        // Given
        val tilesWhereCanBeMoved =
            setOf(
                tile(2, 3),
            )
        battlefieldHudRepository.create(
            displayMovementRange(
                tile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
            ),
        )
        whenever(searchOccupant(2, 2)).thenReturn("battle-unit-2")
        whenever(movementService.tilesWhereCanMove("battle-unit-2")).thenReturn(tilesWhereCanBeMoved)
        // When
        processTileSelected(2, 2)
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayMovementRange
        assertThat(storedHud.battleUnitId).isEqualTo("battle-unit-2")
        assertThat(storedHud.tile).isEqualTo(
            tile(2, 2),
        )
        assertThat(storedHud.tilesWhereCanBeMoved).isEqualTo(tilesWhereCanBeMoved)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.Idle,
            BattlefieldHudEvent.SelectedBattleUnit(
                tile =
                    tile(2, 2),
                battleUnitId = "battle-unit-2",
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            ),
        )
    }

    @Test
    fun `should do nothing when the hud is previewing the ability cast`() {
        // Given
        val hud =
            com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
                .displayAbilityCastPreview()
        battlefieldHudRepository.create(hud)
        // When
        processTileSelected(1, 1)
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus.publishedEvents).isEmpty()
    }

    @Test
    fun `should preview the self ability cast when the selected tile belongs to a cast group with no occupant`() {
        // Given
        val castGroup = castGroup(tile(1, 1))
        battlefieldHudRepository.create(
            displayAbilityCastRange(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroupsWhereCanCast = listOf(castGroup),
            ),
        )
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchBattle()).thenReturn(
            com.mkz.rpg.battle.domain.BattleMother
                .battle(players = listOf("player-1", "player-2"))
                .toDto(),
        )
        whenever(searchOccupant(1, 1)).thenReturn(null)
        // When
        processTileSelected(1, 1)
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastPreview
        assertThat(storedHud.castGroup).isEqualTo(castGroup)
        assertThat(storedHud.enemyBattleUnitId).isNull()
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelfAbilityCastPreviewed(
                casterBattleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroup = castGroup,
            ),
        )
    }

    @Test
    fun `should preview the enemy ability cast when the selected tile is occupied by an enemy battle unit`() {
        // Given
        val castGroup = castGroup(tile(1, 1))
        battlefieldHudRepository.create(
            displayAbilityCastRange(
                casterTile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroupsWhereCanCast =
                    listOf(
                        castGroup,
                    ),
            ),
        )
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchBattle()).thenReturn(
            com.mkz.rpg.battle.domain.BattleMother
                .battle(players = listOf("player-1", "player-2"))
                .toDto(),
        )
        whenever(searchOccupant(1, 1)).thenReturn("enemy-battle-unit-1")
        // When
        processTileSelected(1, 1)
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastPreview
        assertThat(storedHud.castGroup).isEqualTo(castGroup)
        assertThat(storedHud.enemyBattleUnitId).isEqualTo("enemy-battle-unit-1")
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.EnemyAbilityCastPreviewed(
                casterBattleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroup = castGroup,
                enemyBattleUnitId = "enemy-battle-unit-1",
            ),
        )
    }

    @Test
    fun `should go idle when the selected tile does not belong to any cast group`() {
        // Given
        battlefieldHudRepository.create(
            displayAbilityCastRange(
                casterTile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroupsWhereCanCast =
                    listOf(
                        castGroup(tile(1, 1)),
                    ),
            ),
        )
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchBattle()).thenReturn(
            com.mkz.rpg.battle.domain.BattleMother
                .battle(players = listOf("player-1", "player-2"))
                .toDto(),
        )
        // When
        processTileSelected(4, 4)
        // Then
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should go idle when the caster is not the current player`() {
        // Given
        battlefieldHudRepository.create(
            displayAbilityCastRange(
                casterTile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroupsWhereCanCast =
                    listOf(
                        castGroup(tile(1, 1)),
                    ),
            ),
        )
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchBattle()).thenReturn(
            com.mkz.rpg.battle.domain.BattleMother
                .battle(players = listOf("player-1", "player-2"))
                .toDto()
                .copy(currentPlayerTurn = "player-2"),
        )
        // When
        processTileSelected(1, 1)
        // Then
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should do nothing when no battle exists`() {
        // Given
        val hud =
            displayAbilityCastRange()
        battlefieldHudRepository.create(hud)
        whenever(searchBattleUnitById(any())).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchBattle()).thenReturn(null)
        // When
        processTileSelected(1, 1)
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
