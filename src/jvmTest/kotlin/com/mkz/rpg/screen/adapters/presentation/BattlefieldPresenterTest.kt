package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.usecases.commands.FinishPlayerTurn
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.AbilityCastResolved
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDeployed
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.EffectReceived
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.OnTurnStartedEffectApplied
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestMoveBattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.commands.CastAbility
import com.mkz.rpg.battleUnit.usecases.commands.MoveBattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.CanMoveTo
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldCreated
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldTileOccupied
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.OccupantRemoved
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchTilesThatCanBeOccupied
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.CastGroupDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

class BattlefieldPresenterTest {
    private val battlefieldView = mock<BattlefieldView>()
    private val battleUnitInfoView = mock<BattleUnitInfoView>()
    private val attackPreviewView = mock<AttackPreviewView>()
    private val battleHudView = mock<BattleHudView>()

    // The presenter keeps these queries when it is created, so they are stubbed before creating it.
    private val searchOccupant = mock<SearchOccupant>()
    private val searchPosition = mock<SearchPosition>()
    private val searchTilesThatCanBeOccupied = mock<SearchTilesThatCanBeOccupied>()
    private val searchBattleUnitById = mock<SearchBattleUnitById>()
    private val canMoveTo = mock<CanMoveTo>()
    private val searchPlayerById = mock<SearchPlayerById>()
    private val searchUnitById = mock<SearchUnitById>()
    private val battlefieldApi =
        mock<BattlefieldApi>().also {
            whenever(it.searchOccupant).thenReturn(searchOccupant)
            whenever(it.searchPosition).thenReturn(searchPosition)
            whenever(it.searchTilesThatCanBeOccupied).thenReturn(searchTilesThatCanBeOccupied)
        }
    private val battleUnitApi =
        mock<BattleUnitApi>().also {
            whenever(it.searchBattleUnitById).thenReturn(searchBattleUnitById)
            whenever(it.canMoveTo).thenReturn(canMoveTo)
        }
    private val playerApi = mock<PlayerApi>().also { whenever(it.searchPlayerById).thenReturn(searchPlayerById) }
    private val unitApi = mock<UnitApi>().also { whenever(it.searchUnitById).thenReturn(searchUnitById) }
    private val abilityApi = mock<AbilityApi>()
    private val battleApi = mock<BattleApi>()
    private val eventBus = InMemoryEventBus()
    private val timeline = battleSceneTimeline()
    private val battlefieldPresenter =
        BattlefieldPresenter(
            battlefieldView = battlefieldView,
            battleUnitInfoView = battleUnitInfoView,
            attackPreviewView = attackPreviewView,
            playerCallToActionView = mock(),
            battleHudView = battleHudView,
            battlefieldApi = battlefieldApi,
            battleUnitApi = battleUnitApi,
            playerApi = playerApi,
            unitApi = unitApi,
            abilityApi = abilityApi,
            battleApi = battleApi,
            timeline = timeline,
            eventBus = eventBus,
        ).also {
            whenever(battlefieldApi.searchBattlefield).thenReturn(mock<SearchBattlefield>())
            whenever(battleUnitApi.moveBattleUnit).thenReturn(mock<MoveBattleUnit>())
            whenever(battleUnitApi.canCastAbility).thenReturn(mock<CanCastAbility>())
            whenever(battleUnitApi.whereCanCast).thenReturn(mock<WhereCanCast>())
            whenever(battleUnitApi.castAbility).thenReturn(mock<CastAbility>())
            whenever(battleApi.searchBattle).thenReturn(mock<SearchBattle>())
            whenever(battleApi.finishPlayerTurn).thenReturn(mock<FinishPlayerTurn>())
        }

    @Test
    fun `DisplayBattlefield should display battlefield on view when battlefield is displayed`() {
        // Given
        val searchBattlefield = mock<SearchBattlefield>()
        val battlefield = battlefield(rows = 3, columns = 3).toDto()
        whenever(battlefieldApi.searchBattlefield).thenReturn(searchBattlefield)
        whenever(searchBattlefield()).thenReturn(battlefield)
        // When
        battlefieldPresenter.displayBattlefield()
        // Then
        verify(battlefieldView).displayBattlefield(battlefield)
    }

    @Test
    fun `DisplayBattlefield should display battlefield on view when battlefield is created`() {
        // Given
        val searchBattlefield = mock<SearchBattlefield>()
        val battlefield = battlefield(rows = 3, columns = 3).toDto()
        whenever(battlefieldApi.searchBattlefield).thenReturn(searchBattlefield)
        whenever(searchBattlefield()).thenReturn(battlefield)
        eventBus.publish(BattlefieldCreated)
        // When
        eventBus.dispatch()
        // Then
        verify(battlefieldView).displayBattlefield(battlefield)
    }

    @Test
    fun `DisplayUnit should display knight battle unit on view when knight battle unit is displayed`() {
        // Given
        val searchBattleUnitById = mock<SearchBattleUnitById>()
        val battleUnitId = "battle-unit-1"
        whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
        whenever(searchBattleUnitById(battleUnitId)).thenReturn(battleUnit(unit = unit(id = "knight").toDto()).toDto())
        // When
        battlefieldPresenter.displayUnit(row = 1, column = 2, battleUnitId = battleUnitId)
        // Then
        verify(battlefieldView).displayBattleUnit(row = 1, column = 2, unitId = "knight")
    }

    @Test
    fun `DisplayUnit should display rat battle unit on view when rat battle unit is displayed`() {
        // Given
        val searchBattleUnitById = mock<SearchBattleUnitById>()
        val battleUnitId = "battle-unit-1"
        whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
        whenever(searchBattleUnitById(battleUnitId)).thenReturn(battleUnit(unit = unit(id = "rat").toDto()).toDto())
        // When
        battlefieldPresenter.displayUnit(row = 1, column = 2, battleUnitId = battleUnitId)
        // Then
        verify(battlefieldView).displayBattleUnit(row = 1, column = 2, unitId = "rat")
    }

    @Test
    fun `DisplayUnit should not display battle unit on view when battle unit is not found`() {
        // Given
        val searchBattleUnitById = mock<SearchBattleUnitById>()
        val battleUnitId = "battle-unit-1"
        whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
        whenever(searchBattleUnitById(battleUnitId)).thenReturn(null)
        // When
        battlefieldPresenter.displayUnit(row = 1, column = 2, battleUnitId = battleUnitId)
        // Then
        verify(battlefieldView, never()).displayBattleUnit(any(), any(), any())
    }

    @Test
    fun `RemoveUnit should remove battle unit from view when battle unit is removed`() {
        // Given
        // When
        battlefieldPresenter.removeUnit(row = 1, column = 2)
        // Then
        verify(battlefieldView).removeBattleUnit(row = 1, column = 2)
    }

    @Test
    fun `DisplayUnitInOrder should display the deployed battle unit once the previous steps have been played`() {
        // Given
        val searchBattleUnitById = mock<SearchBattleUnitById>()
        whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit(unit = unit(id = "knight").toDto()).toDto())
        val previousStep = timeline.playLongStep()
        eventBus.publish(BattleUnitDeployed(battleUnitId = "battle-unit-1", row = 1, column = 2))
        eventBus.dispatch()
        verify(battlefieldView, never()).displayBattleUnit(any(), any(), any())
        // When
        previousStep.complete(Unit)
        // Then
        verify(battlefieldView).displayBattleUnit(row = 1, column = 2, unitId = "knight")
    }

    @Test
    fun `MoveUnit should move the battle unit along its path on view when the battle unit is moved`() {
        // Given
        val searchBattleUnitById = mock<SearchBattleUnitById>()
        whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit(unit = unit(id = "rat").toDto()).toDto())
        val path = listOf(PositionDto(row = 0, column = 1), PositionDto(row = 0, column = 2))
        eventBus.publish(BattleUnitMoved(battleUnitId = "battle-unit-1", fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2, path = path))
        // When
        eventBus.dispatch()
        // Then
        verifyBlocking(battlefieldView) { moveBattleUnit(fromRow = 0, fromColumn = 0, path = path, unitId = "rat") }
    }

    @Test
    fun `MoveUnit should not move the battle unit on view when the battle unit is not found`() {
        // Given
        val searchBattleUnitById = mock<SearchBattleUnitById>()
        whenever(battleUnitApi.searchBattleUnitById).thenReturn(searchBattleUnitById)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(null)
        val path = listOf(PositionDto(row = 0, column = 1))
        eventBus.publish(BattleUnitMoved(battleUnitId = "battle-unit-1", fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 1, path = path))
        // When
        eventBus.dispatch()
        // Then
        verifyBlocking(battlefieldView, never()) { moveBattleUnit(any(), any(), any(), any()) }
    }

    private fun knightAt(position: PositionDto?) {
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit(unit = unit(id = "knight").toDto()).toDto())
        whenever(searchPosition("battle-unit-1")).thenReturn(position)
    }

    @Test
    fun `FlashUnit should flash the battle unit on view when it receives an effect`() {
        // Given
        knightAt(PositionDto(row = 3, column = 4))
        eventBus.publish(EffectReceived(battleUnitId = "battle-unit-1", effectId = "lava-damage"))
        // When
        eventBus.dispatch()
        // Then
        verifyBlocking(battlefieldView) { flashBattleUnit(row = 3, column = 4, unitId = "knight") }
    }

    @Test
    fun `FlashUnit should flash the battle unit on view when an effect is applied at the start of its turn`() {
        // Given
        knightAt(PositionDto(row = 3, column = 4))
        eventBus.publish(OnTurnStartedEffectApplied(battleUnitId = "battle-unit-1", effectId = "venom-damage"))
        // When
        eventBus.dispatch()
        // Then
        verifyBlocking(battlefieldView) { flashBattleUnit(row = 3, column = 4, unitId = "knight") }
    }

    @Test
    fun `FlashUnit should not flash the battle unit on view when it is not on the battlefield`() {
        // Given
        knightAt(null)
        eventBus.publish(EffectReceived(battleUnitId = "battle-unit-1", effectId = "lava-damage"))
        // When
        eventBus.dispatch()
        // Then
        verifyBlocking(battlefieldView, never()) { flashBattleUnit(any(), any(), any()) }
    }

    @Test
    fun `FlashUnit should flash the battle unit once the previous steps have been played`() {
        // Given
        knightAt(PositionDto(row = 3, column = 4))
        val previousStep = timeline.playLongStep()
        eventBus.publish(EffectReceived(battleUnitId = "battle-unit-1", effectId = "lava-damage"))
        eventBus.dispatch()
        verifyBlocking(battlefieldView, never()) { flashBattleUnit(any(), any(), any()) }
        // When
        previousStep.complete(Unit)
        // Then
        verifyBlocking(battlefieldView) { flashBattleUnit(row = 3, column = 4, unitId = "knight") }
    }

    private fun knight() {
        val knight = unit(id = "knight")
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit(unit = knight.toDto()).toDto())
        whenever(searchUnitById("knight")).thenReturn(knight.toDto())
    }

    @Test
    fun `DimTiles should dim every tile but the movement range and the selected battle unit when a battle unit is selected`() {
        // Given
        knight()
        eventBus.publish(
            BattlefieldHudEvent.SelectedBattleUnit(
                tile = TileDto(row = 1, column = 1),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved = setOf(TileDto(row = 1, column = 2), TileDto(row = 2, column = 1)),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        verify(battlefieldView).dimTilesExcept(setOf(PositionDto(row = 1, column = 2), PositionDto(row = 2, column = 1), PositionDto(row = 1, column = 1)))
    }

    @Test
    fun `DimTiles should dim every tile but the cast tiles and the caster when an ability is selected`() {
        // Given
        knight()
        eventBus.publish(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile = TileDto(row = 1, column = 1),
                battleUnitId = "battle-unit-1",
                abilityId = "sword",
                castGroupsWhereCanCast =
                    listOf(
                        CastGroupDto(tiles = listOf(TileDto(row = 0, column = 1))),
                        CastGroupDto(tiles = listOf(TileDto(row = 1, column = 0))),
                    ),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        verify(battlefieldView).dimTilesExcept(setOf(PositionDto(row = 0, column = 1), PositionDto(row = 1, column = 0), PositionDto(row = 1, column = 1)))
    }

    @Test
    fun `DimTiles should reset the tiles when the battlefield hud is idle`() {
        // Given
        eventBus.publish(BattlefieldHudEvent.Idle)
        // When
        eventBus.dispatch()
        // Then
        verify(battlefieldView).resetTiles()
    }

    private val selectedKnightUnit = unit(id = "knight", movementRange = 3).toDto()
    private val selectedKnight = battleUnit(id = "knight-1", unit = selectedKnightUnit, player = player(id = "human", type = HUMAN).toDto())

    private fun selectKnight() {
        whenever(searchOccupant(row = 1, column = 1)).thenReturn("knight-1")
        whenever(searchBattleUnitById("knight-1")).thenReturn(selectedKnight.toDto())
        whenever(searchUnitById("knight")).thenReturn(selectedKnightUnit)
        whenever(searchPlayerById("human")).thenReturn(player(id = "human", type = HUMAN).toDto())
        whenever(searchPosition("knight-1")).thenReturn(PositionDto(row = 1, column = 1))
        whenever(canMoveTo(any(), any(), any())).thenReturn(true)
        reachableTiles(PositionDto(row = 1, column = 2))
        battlefieldPresenter.tileSelected(row = 1, column = 1)
        eventBus.dispatch()
    }

    private fun reachableTiles(vararg tiles: PositionDto) {
        whenever(searchTilesThatCanBeOccupied(battleUnitId = "knight-1", distance = 3)).thenReturn(tiles.toList())
    }

    @Test
    fun `RefreshSelectedBattleUnit should display the new paths of the selected battle unit when a tile is vacated`() {
        // Given
        selectKnight()
        reachableTiles(PositionDto(row = 1, column = 2), PositionDto(row = 1, column = 3))
        eventBus.publish(OccupantRemoved(battleUnitId = "rat-1", row = 1, column = 3))
        // When
        eventBus.dispatch()
        // Then
        verify(battlefieldView).displayPotentialMovement(row = 1, column = 3)
    }

    @Test
    fun `RefreshSelectedBattleUnit should stop displaying the closed paths of the selected battle unit when a tile is occupied`() {
        // Given
        selectKnight()
        reachableTiles()
        eventBus.publish(BattlefieldTileOccupied(row = 1, column = 2, battlefieldUnitId = "bee-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(battlefieldView).dimTilesExcept(setOf(PositionDto(row = 1, column = 1)))
    }

    @Test
    fun `RefreshSelectedBattleUnit should display the selected battle unit again when its cast has been resolved`() {
        // Given
        selectKnight()
        eventBus.publish(AbilityCastResolved(battleUnitId = "knight-1", abilityId = "heal"))
        // When
        eventBus.dispatch()
        // Then
        verify(battleUnitInfoView, times(2)).display(selectedKnight.toDto(), selectedKnightUnit)
    }

    @Test
    fun `RefreshSelectedBattleUnit should not move the previously selected battle unit when a turn starts and an empty tile is selected`() {
        // Given
        selectKnight()
        val requestedMoves = mutableListOf<RequestMoveBattleUnit>()
        eventBus.subscribe<RequestMoveBattleUnit> { event -> requestedMoves += event }
        eventBus.publish(PlayerTurnStarted(playerId = "human"))
        eventBus.dispatch()
        // When
        battlefieldPresenter.tileSelected(row = 1, column = 2)
        eventBus.dispatch()
        // Then
        requestedMoves.shouldBeEmpty()
    }

    @Test
    fun `TileSelected should process the selected tile when the timeline is idle`() {
        // Given
        // When
        battlefieldPresenter.tileSelected(row = 1, column = 2)
        // Then
        verify(searchOccupant).invoke(row = 1, column = 2)
    }

    @Test
    fun `TileSelected should ignore the selected tile when the timeline is playing`() {
        // Given
        timeline.playLongStep()
        // When
        battlefieldPresenter.tileSelected(row = 1, column = 2)
        // Then
        verify(searchOccupant, never()).invoke(any(), any())
    }
}
