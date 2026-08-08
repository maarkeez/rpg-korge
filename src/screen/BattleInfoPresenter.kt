package screen

import battle.adapters.presentation.BattleApi
import battle.domain.BattleEvent.PlayerTurnStarted
import player.adapters.presentation.PlayerApi
import shared.domain.EventBus

class BattleInfoPresenter(
    private val battleInfoView: BattleInfoView,
    private val battleApi: BattleApi,
    private val playerApi: PlayerApi,
    eventBus: EventBus,
) {

    private val subscription = eventBus.subscribe<PlayerTurnStarted> {
        updateBattleInfo()
    }

    fun updateBattleInfo() {
        val battle = battleApi.searchBattle()!!
        val player = playerApi.searchPlayerById(battle.currentPlayerTurn)!!
        battleInfoView.displayBattleInfo(player.name, battle.currentRound)
    }

    fun dispose() {
        subscription.dispose()
    }
}
