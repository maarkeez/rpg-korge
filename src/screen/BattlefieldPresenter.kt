package screen

import battlefield.adapters.presentation.BattlefieldApi
import battlefield.domain.BattlefieldEvent
import battlefield.domain.BattlefieldEvent.BattlefieldCreated
import battleunit.adapters.presentation.BattleUnitApi
import player.adapters.presentation.PlayerApi
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
) : BattlefieldView.Delegate {

    private var selectionState: SelectionState = NothingSelected

    private val subscriptions = listOf(
        eventBus.subscribe<BattlefieldCreated> { displayBattlefield() },
        eventBus.subscribe<BattlefieldEvent.BattlefieldTileOccupied> { event ->
            displayUnit(event.row, event.column, event.battlefieldUnitId)
        },
        eventBus.subscribe<BattlefieldEvent.OccupantRemoved> {

        },
    )

    init {
        battlefieldView.setDelegate(this)
    }

    fun displayBattlefield() {
        val battlefield = battlefieldApi.searchBattlefield()!!
        battlefieldView.displayBattlefield(battlefield)
    }

    fun displayUnit(row: Int, column: Int, battlefieldUnitId: String) {
        val battleUnit = battleUnitApi.searchBattleUnitById(battlefieldUnitId) ?: return
        val player = playerApi.searchPlayerById(battleUnit.playerId)!!
        if(player.type == "HUMAN"){
            battlefieldView.displayHumanBattlefieldUnit(row, column)
        }else{
            battlefieldView.displayCPUBattlefieldUnit(row, column)
        }
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    override fun tileSelected(row: Int, column: Int) {
        when (selectionState) {
            is NothingSelected -> onNothingSelectedATileWasSelected(row, column)
            is BattleUnitSelected -> onBattleUnitSelectedATileWasSelected(row, column)
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
        if(occupantId!= null && occupantId != currentState.battleUnitId) {
            clearSelection()
            selectBattleUnit(row, column, occupantId)
        }else{
            clearSelection()
            // TODO: Implement movement
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
        tilesThatCanBeOccupied.forEach { position ->
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

    private sealed interface SelectionState{
        object NothingSelected : SelectionState
        data class BattleUnitSelected(val row: Int, val column: Int, val battleUnitId: String) : SelectionState
    }
}
