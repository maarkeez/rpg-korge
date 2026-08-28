package screen

import battle.adapters.presentation.BattleApi
import battle.domain.BattleEvent
import battle.domain.BattleEvent.PlayerDefeated
import battle.domain.BattleEvent.PlayerTurnStarted
import battle.domain.BattleEvent.PlayerVictory
import player.adapters.presentation.PlayerApi
import shared.domain.EventBus
import shared.domain.Subscription

class BattleInfoPresenter(
    private val battleInfoView: BattleInfoView,
    private val battleApi: BattleApi,
    private val playerApi: PlayerApi,
    eventBus: EventBus,
) {

    private val subscriptions = listOf(
        eventBus.subscribe<PlayerTurnStarted> {
            updateBattleInfo()
        },
        eventBus.subscribe<PlayerVictory> { event ->
            displayPlayerWin(event.playerId)
        }
    )

    fun updateBattleInfo() {
        val battle = battleApi.searchBattle()!!
        val player = playerApi.searchPlayerById(battle.currentPlayerTurn)!!
        battleInfoView.displayBattleInfo(player.name, battle.currentRound)
    }

    fun displayPlayerWin(playerId: String) {
        val player = playerApi.searchPlayerById(playerId)!!
        battleInfoView.displayPlayerWin(player.name)
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }
}
