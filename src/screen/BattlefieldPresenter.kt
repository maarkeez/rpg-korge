package screen

import battlefield.adapters.presentation.BattlefieldApi
import battlefield.domain.BattlefieldEvent
import battlefield.domain.BattlefieldEvent.BattlefieldCreated
import battleunit.adapters.presentation.BattleUnitApi
import player.adapters.presentation.PlayerApi
import shared.domain.EventBus
import shared.domain.Subscription

class BattlefieldPresenter(
    private val battlefieldView: BattlefieldView,
    private val battleUnitInfoView: BattleUnitInfoView,
    private val battlefieldApi: BattlefieldApi,
    private val battleUnitApi: BattleUnitApi,
    private val playerApi: PlayerApi,
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
        val occupantId = battlefieldApi.searchOccupant(row, column)
        if(occupantId != null) {
            val battleUnit = battleUnitApi.searchBattleUnitById(occupantId)!!
            battleUnitInfoView.display(battleUnit)
        }else{
            battleUnitInfoView.clear()
        }
    }
}
