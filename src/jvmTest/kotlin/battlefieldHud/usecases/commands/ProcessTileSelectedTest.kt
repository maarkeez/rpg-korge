package battlefieldHud.usecases.commands

import battle.adapters.presentation.BattleApi
import battle.domain.BattleMother
import battle.usecases.queries.SearchBattle
import battlefield.adapters.presentation.BattlefieldApi
import battlefield.usecases.queries.SearchOccupant
import battlefieldHud.domain.BattlefieldHudMother
import battleunit.adapters.presentation.BattleUnitApi
import battleunit.domain.BattleUnit
import battleunit.domain.BattleUnitMother
import battleunit.usecases.commands.MoveBattleUnit
import battleunit.usecases.queries.SearchBattleUnitById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import player.adapters.presentation.PlayerApi
import player.domain.Player
import player.domain.PlayerMother
import player.usecases.queries.SearchPlayerById
import screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import screen.battlefieldHud.domain.BattlefieldHud.Idle
import screen.battlefieldHud.domain.BattlefieldHudEvent
import screen.battlefieldHud.usecases.services.MovementService
import shared.domain.FakeEventBus
import shared.domain.assertThat

class ProcessTileSelectedTest {
    private val battlefieldApi: BattlefieldApi = mock()
    private val battleUnitApi: BattleUnitApi = mock()
    private val playerApi: PlayerApi = mock()
    private val battleApi: BattleApi = mock()
    private val searchOccupant: SearchOccupant = mock()
    private val searchBattleUnitById: SearchBattleUnitById = mock()
    private val moveBattleUnit: MoveBattleUnit = mock()
    private val searchPlayerById: SearchPlayerById = mock()
    private val searchBattle: SearchBattle = mock()
    private val movementService: MovementService = mock()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
    private val processTileSelected =
        _root_ide_package_.screen.battlefieldHud.usecases.commands.ProcessTileSelected(
            battlefieldApi = battlefieldApi,
            battleUnitApi = battleUnitApi,
            playerApi = playerApi,
            battleApi = battleApi,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )

    @BeforeEach
    fun setUp() {
        whenever(battlefieldApi.searchOccupant).thenReturn(searchOccupant)
        whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
        whenever(battleUnitApi.moveBattleUnit).thenReturn(moveBattleUnit)
        whenever(playerApi.searchPlayerById).thenReturn(searchPlayerById)
        whenever(battleApi.searchBattle).thenReturn(searchBattle)
    }

    private fun battleUnitDto(
        id: String,
        playerId: String,
    ): BattleUnit.Dto =
        _root_ide_package_.battleunit.domain.BattleUnitMother
            .battleUnit(
                player =
                    _root_ide_package_.player.domain.PlayerMother
                        .player(id = playerId)
                        .toDto(),
            ).toDto()
            .copy(id = id, playerId = playerId)

    @Test
    fun `should select the battle unit when a tile occupied by a battle unit is selected while idle`() {
        // Given
        val tilesWhereCanBeMoved =
            setOf(
                _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                    .tile(1, 3),
            )
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
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
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .tile(1, 2),
        )
        assertThat(storedHud.tilesWhereCanBeMoved).isEqualTo(tilesWhereCanBeMoved)
        _root_ide_package_.shared.domain.assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnit(
                tile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(1, 2),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            ),
        )
    }

    @Test
    fun `should go idle when an empty tile is selected while idle`() {
        // Given
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .idle(),
        )
        whenever(searchOccupant(0, 0)).thenReturn(null)
        // When
        processTileSelected(0, 0)
        // Then
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should move the battle unit when a tile within the movement range is selected`() {
        // Given
        val hud =
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange(
                tile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved =
                    setOf(
                        _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                            .tile(2, 3),
                    ),
            )
        battlefieldHudRepository.create(hud)
        whenever(searchOccupant(2, 3)).thenReturn(null)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchPlayerById("player-1")).thenReturn(
            _root_ide_package_.player.domain.PlayerMother
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
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange(
                tile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved =
                    setOf(
                        _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                            .tile(2, 3),
                    ),
            ),
        )
        whenever(searchOccupant(5, 5)).thenReturn(null)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchPlayerById("player-1")).thenReturn(
            _root_ide_package_.player.domain.PlayerMother
                .player(id = "player-1", type = Player.Dto.PlayerTypeDto.HUMAN)
                .toDto(),
        )
        // When
        processTileSelected(5, 5)
        // Then
        verify(moveBattleUnit, never()).invoke(eq("battle-unit-1"), eq(5), eq(5))
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should go idle without moving the battle unit when it belongs to a non-human player`() {
        // Given
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange(
                tile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved =
                    setOf(
                        _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                            .tile(2, 3),
                    ),
            ),
        )
        whenever(searchOccupant(2, 3)).thenReturn(null)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchPlayerById("player-1")).thenReturn(
            _root_ide_package_.player.domain.PlayerMother
                .player(id = "player-1", type = Player.Dto.PlayerTypeDto.CPU)
                .toDto(),
        )
        // When
        processTileSelected(2, 3)
        // Then
        verify(moveBattleUnit, never()).invoke(eq("battle-unit-1"), eq(2), eq(3))
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should go idle when the tile is occupied by the same battle unit`() {
        // Given
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange(
                tile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
            ),
        )
        whenever(searchOccupant(1, 1)).thenReturn("battle-unit-1")
        // When
        processTileSelected(1, 1)
        // Then
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should select the other battle unit when the tile is occupied by another battle unit`() {
        // Given
        val tilesWhereCanBeMoved =
            setOf(
                _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                    .tile(2, 3),
            )
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange(
                tile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
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
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .tile(2, 2),
        )
        assertThat(storedHud.tilesWhereCanBeMoved).isEqualTo(tilesWhereCanBeMoved)
        _root_ide_package_.shared.domain.assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.Idle,
            BattlefieldHudEvent.SelectedBattleUnit(
                tile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(2, 2),
                battleUnitId = "battle-unit-2",
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            ),
        )
    }

    @Test
    fun `should do nothing when the hud is previewing the ability cast`() {
        // Given
        val hud =
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .displayAbilityCastPreview()
        battlefieldHudRepository.create(hud)
        // When
        processTileSelected(1, 1)
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus.publishedEvents).isEmpty()
    }

    @Test
    fun `should preview the self ability cast when the selected tile is a cast position with no occupant`() {
        // Given
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange(
                casterTile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanCast =
                    setOf(
                        _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                            .tile(1, 1),
                    ),
            ),
        )
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchBattle()).thenReturn(
            _root_ide_package_.battle.domain.BattleMother
                .battle(players = listOf("player-1", "player-2"))
                .toDto(),
        )
        whenever(searchOccupant(1, 1)).thenReturn(null)
        // When
        processTileSelected(1, 1)
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastPreview
        assertThat(storedHud.castTile).isEqualTo(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .tile(1, 1),
        )
        assertThat(storedHud.enemyBattleUnitId).isNull()
        _root_ide_package_.shared.domain.assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelfAbilityCastPreviewed(
                casterBattleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castTile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(1, 1),
            ),
        )
    }

    @Test
    fun `should preview the enemy ability cast when the selected tile is occupied by an enemy battle unit`() {
        // Given
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange(
                casterTile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanCast =
                    setOf(
                        _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                            .tile(1, 1),
                    ),
            ),
        )
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchBattle()).thenReturn(
            _root_ide_package_.battle.domain.BattleMother
                .battle(players = listOf("player-1", "player-2"))
                .toDto(),
        )
        whenever(searchOccupant(1, 1)).thenReturn("enemy-battle-unit-1")
        // When
        processTileSelected(1, 1)
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastPreview
        assertThat(storedHud.castTile).isEqualTo(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .tile(1, 1),
        )
        assertThat(storedHud.enemyBattleUnitId).isEqualTo("enemy-battle-unit-1")
        _root_ide_package_.shared.domain.assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.EnemyAbilityCastPreviewed(
                casterBattleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castTile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(1, 1),
                enemyBattleUnitId = "enemy-battle-unit-1",
            ),
        )
    }

    @Test
    fun `should go idle when the selected tile is not a cast position`() {
        // Given
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange(
                casterTile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanCast =
                    setOf(
                        _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                            .tile(1, 1),
                    ),
            ),
        )
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchBattle()).thenReturn(
            _root_ide_package_.battle.domain.BattleMother
                .battle(players = listOf("player-1", "player-2"))
                .toDto(),
        )
        // When
        processTileSelected(4, 4)
        // Then
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should go idle when the caster is not the current player`() {
        // Given
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange(
                casterTile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanCast =
                    setOf(
                        _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                            .tile(1, 1),
                    ),
            ),
        )
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnitDto("battle-unit-1", "player-1"))
        whenever(searchBattle()).thenReturn(
            _root_ide_package_.battle.domain.BattleMother
                .battle(players = listOf("player-1", "player-2"))
                .toDto()
                .copy(currentPlayerTurn = "player-2"),
        )
        // When
        processTileSelected(1, 1)
        // Then
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should do nothing when no battle exists`() {
        // Given
        val hud =
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .displayAbilityCastRange()
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
