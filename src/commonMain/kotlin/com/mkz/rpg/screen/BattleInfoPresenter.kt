package com.mkz.rpg.screen

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe

class BattleInfoPresenter(
    private val battleInfoView: BattleInfoView,
    private val battleApi: BattleApi,
    private val playerApi: PlayerApi,
    eventBus: EventBus,
) {
    private val subscriptions =
        listOf(
            eventBus.subscribe<PlayerTurnStarted> {
                updateBattleInfo()
            },
            eventBus.subscribe<PlayerVictory> { event ->
                displayPlayerWin(event.playerId)
            },
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
