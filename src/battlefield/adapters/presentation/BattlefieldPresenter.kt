package battlefield.adapters.presentation

import battle.adapters.presentation.BattleApi
import battle.adapters.presentation.BattleInfoView
import battle.domain.BattleEvent.PlayerTurnStarted
import battlefield.domain.BattlefieldEvent
import battlefield.domain.BattlefieldEvent.BattlefieldCreated
import player.adapters.presentation.PlayerApi
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
