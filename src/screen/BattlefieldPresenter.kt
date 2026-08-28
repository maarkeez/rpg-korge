package screen

import battle.domain.BattleEvent
import battlefield.adapters.presentation.BattlefieldApi
import battlefield.domain.Battlefield
import battlefield.domain.BattlefieldEvent
import battlefield.domain.BattlefieldEvent.BattlefieldCreated
import battleunit.adapters.presentation.BattleUnitApi
import battleunit.domain.BattleUnitEvent
import battleunit.usecases.queries.WhereCanCast
import battleunit.usecases.queries.WhereCanCast.PositionDto
import player.adapters.presentation.PlayerApi
import screen.BattlefieldPresenter.SelectionState.AbilitySelected
import screen.BattlefieldPresenter.SelectionState.BattleUnitSelected
import screen.BattlefieldPresenter.SelectionState.NothingSelected
import shared.domain.EventBus
import shared.domain.Subscription
import unit.adapters.presentation.UnitApi

class BattlefieldPresenter(
    private val battlefieldView: BattlefieldView,
    private val battleUnitInfoView: BattleUnitInfoView,
    private val battlefieldApi: BattlefieldApi,
    private val battleUnitApi: BattleUnitApi,
    private val playerApi: PlayerApi,
    private val unitApi: UnitApi,
    eventBus: EventBus,
) : BattlefieldView.Delegate, BattleUnitInfoView.Delegate {

    private var selectionState: SelectionState = NothingSelected

    private val subscriptions = listOf(
        eventBus.subscribe<BattlefieldCreated> { displayBattlefield() },
        eventBus.subscribe<BattleUnitEvent.BattleUnitDeployed> { event ->
            displayUnit(event.row, event.column, event.battleUnitId)
        },
        eventBus.subscribe<BattleUnitEvent.BattleUnitMoved> { event ->
            removeUnit(event.fromRow, event.fromColumn)
            displayUnit(event.toRow, event.toColumn, event.battleUnitId)
        },
        eventBus.subscribe<BattlefieldEvent.OccupantRemoved> { event ->
            removeUnit(event.row, event.column)
        },
        eventBus.subscribe<BattleEvent.PlayerTurnStarted> {
            clearSelection()
        }
    )

    init {
        battlefieldView.setDelegate(this)
        battleUnitInfoView.setDelegate(this)
    }

    fun displayBattlefield() {
        val battlefield = battlefieldApi.searchBattlefield()!!
        battlefieldView.displayBattlefield(battlefield)
    }

    fun displayUnit(row: Int, column: Int, battleUnitId: String) {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return
        val player = playerApi.searchPlayerById(battleUnit.playerId)!!
        if(player.type == "HUMAN"){
            battlefieldView.displayHumanBattleUnit(row, column)
        }else{
            battlefieldView.displayCPUBattleUnit(row, column)
        }
    }

    fun removeUnit(row: Int, column: Int) {
        battlefieldView.removeBattleUnit(row, column)
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    override fun tileSelected(row: Int, column: Int) {
        when (selectionState) {
            is NothingSelected -> onNothingSelectedATileWasSelected(row, column)
            is BattleUnitSelected -> onBattleUnitSelectedATileWasSelected(row, column)
            is AbilitySelected -> onAbilitySelectedATileWasSelected(row, column)
        }
    }

    private fun onNothingSelectedATileWasSelected(row: Int, column: Int) {
        val occupantId = battlefieldApi.searchOccupant(row, column)
        if(occupantId != null) {
            selectBattleUnit(row, column, occupantId)
        }else{
            clearSelection()
        }
    }

    private fun onBattleUnitSelectedATileWasSelected(row: Int, column: Int) {
        val occupantId = battlefieldApi.searchOccupant(row, column)
        val currentState = selectionState as BattleUnitSelected
        if(occupantId == null) {
            clearSelection()
            val battleUnit = battleUnitApi.searchBattleUnitById(currentState.battleUnitId)!!
            // TODO: Should we have a battle unit use case "where can be moved" instead?
            val tilesThatCanBeOccupied = battlefieldApi.searchTilesThatCanBeOccupied(
                battleUnitId = currentState.battleUnitId,
                distance = battleUnit.remainingTurnActions.remainingSteps
            ).filter { position ->
                battleUnitApi.canMoveTo(
                    battleUnitId = currentState.battleUnitId,
                    moveToRow = position.row,
                    moveToColumn = position.column,
                )
            }
            val selectedTileInMovementRange = tilesThatCanBeOccupied.contains(Battlefield.Dto.PositionDto(row, column))
            val player = playerApi.searchPlayerById(battleUnit.playerId)!!
            val isHumanPlayer = player.type == "HUMAN"
            if(selectedTileInMovementRange && isHumanPlayer) {
                battleUnitApi.moveBattleUnit(battleUnitId = battleUnit.id, moveToRow = row, moveToColumn = column)
            }
        } else if(occupantId == currentState.battleUnitId) {
            clearSelection()
        } else if(occupantId != currentState.battleUnitId) {
            clearSelection()
            selectBattleUnit(row, column, occupantId)
        }
    }

    private fun selectBattleUnit(row: Int, column: Int, occupantId: String) {
        selectionState = BattleUnitSelected(row, column, occupantId)
        val battleUnit = battleUnitApi.searchBattleUnitById(occupantId)!!
        val unit = unitApi.searchUnitById(battleUnit.unitId)!!
        battleUnitInfoView.display(battleUnit, unit)
        val tilesThatCanBeOccupied = battlefieldApi.searchTilesThatCanBeOccupied(
            battleUnitId = battleUnit.id,
            distance = battleUnit.remainingTurnActions.remainingSteps
        )
        val tilesWhereCanBeMoved = tilesThatCanBeOccupied.filter { tilePosition ->
            battleUnitApi.canMoveTo(
                battleUnitId = battleUnit.id,
                moveToRow = tilePosition.row,
                moveToColumn = tilePosition.column
            )
        }
        tilesWhereCanBeMoved.forEach { position ->
            battlefieldView.displayPotentialMovement(
                row = position.row,
                column = position.column
            )
        }
    }

    private fun clearSelection() {
        selectionState = NothingSelected
        battleUnitInfoView.hide()
        battlefieldView.resetTiles()
    }

    override fun abilitySelected(abilityId: String) {
        if(selectionState !is BattleUnitSelected) return
        val battleUnitSelected = selectionState as BattleUnitSelected
        val canCastAbility = battleUnitApi.canCastAbility(battleUnitSelected.battleUnitId, abilityId)
        if(!canCastAbility) return
        val castPositions = battleUnitApi.whereCanCast(battleUnitSelected.battleUnitId, abilityId)
        if(castPositions.isEmpty()) return
        battlefieldView.resetTiles()
        castPositions.forEach { position ->
            battlefieldView.displayPotentialCast(
                row = position.row,
                column = position.column
            )
        }
        selectionState = AbilitySelected(
            row = battleUnitSelected.row,
            column = battleUnitSelected.column,
            battleUnitId = battleUnitSelected.battleUnitId,
            abilityId = abilityId
        )
    }

    private fun onAbilitySelectedATileWasSelected(row: Int, column: Int) {
        val abilitySelected = selectionState as AbilitySelected
        val castPositions = battleUnitApi.whereCanCast(abilitySelected.battleUnitId, abilitySelected.abilityId)
        if(castPositions.contains(PositionDto(row, column))) {
            battleUnitApi.castAbility(abilitySelected.battleUnitId, abilitySelected.abilityId, row, column)
        }
        clearSelection()
    }

    private sealed interface SelectionState{
        object NothingSelected : SelectionState
        data class BattleUnitSelected(val row: Int, val column: Int, val battleUnitId: String) : SelectionState
        data class AbilitySelected(val row: Int, val column: Int, val battleUnitId: String, val abilityId: String) : SelectionState
    }
}
