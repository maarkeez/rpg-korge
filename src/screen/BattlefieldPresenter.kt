package screen

import battlefield.adapters.presentation.BattlefieldApi
import battlefield.domain.BattlefieldEvent
import battlefield.domain.BattlefieldEvent.BattlefieldCreated
import shared.domain.EventBus
import shared.domain.Subscription

class BattlefieldPresenter(
    private val battlefieldView: BattlefieldView,
    private val battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
) {

    private val subscriptions = listOf(
        eventBus.subscribe<BattlefieldCreated> { displayBattlefield() },
        eventBus.subscribe<BattlefieldEvent.BattlefieldTileOccupied> { displayUnits() },
        eventBus.subscribe<BattlefieldEvent.OccupantRemoved> { displayUnits() },
    )

    fun displayBattlefield() {
        val battlefield = battlefieldApi.searchBattlefield()!!
        battlefieldView.displayBattlefield(battlefield)
    }

    fun displayUnits() {
        val battleField = battlefieldApi.searchBattlefield()!!
        battleField.tiles.forEach { entry ->
            val row = entry.key.row
            val column = entry.key.column
            val battleUnitId = entry.value.battleUnitId
            // TODO: Update the view with the units
        }
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }
}
