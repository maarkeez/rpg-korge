package screen

import battlefield.adapters.presentation.BattlefieldApi
import battlefield.domain.BattlefieldEvent
import battlefield.domain.BattlefieldEvent.BattlefieldCreated
import battleunit.adapters.presentation.BattleUnitApi
import player.adapters.presentation.PlayerApi
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
        // TODO: Reset battlefield style
        val occupantId = battlefieldApi.searchOccupant(row, column)
        if(occupantId != null) {
            // Display battle unit info
            val battleUnit = battleUnitApi.searchBattleUnitById(occupantId)!!
            val unit = unitApi.searchUnitById(battleUnit.unitId)!!
            battleUnitInfoView.display(battleUnit, unit)
            val tilesThatCanBeOccupied = battlefieldApi.searchTilesThatCanBeOccupied(
                battleUnitId = battleUnit.id,
                distance = battleUnit.remainingTurnActions.remainingSteps
            )
            tilesThatCanBeOccupied.forEach{ position ->
                battlefieldView.displayPotentialMovement(
                    row = position.row,
                    column = position.column
                )
            }

        }else{
            battleUnitInfoView.hide()
        }
    }
}
