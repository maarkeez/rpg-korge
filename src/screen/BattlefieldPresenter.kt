package screen

import battlefield.adapters.presentation.BattlefieldApi
import battlefield.domain.BattlefieldEvent.BattlefieldCreated
import shared.domain.EventBus

class BattlefieldPresenter(
    private val battlefieldView: BattlefieldView,
    private val battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
) {

    private val subscription = eventBus.subscribe<BattlefieldCreated> {
        displayBattlefield()
    }

    fun displayBattlefield() {
        val battlefield = battlefieldApi.searchBattlefield()!!
        battlefieldView.displayBattlefield(battlefield)
    }

    fun dispose() {
        subscription.dispose()
    }
}
