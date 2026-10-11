package com.mkz.rpg.screen.adapters.presentation

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
    private val timeline: BattleSceneTimeline,
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

    // Each function reads what it needs straight away and draws it once the previous timeline steps have been played.

    fun updateBattleInfo() {
        val battle = battleApi.searchBattle()!!
        val player = playerApi.searchPlayerById(battle.currentPlayerTurn)!!
        timeline.play { battleInfoView.displayBattleInfo(player.name, battle.currentRound) }
    }

    fun displayPlayerWin(playerId: String) {
        val player = playerApi.searchPlayerById(playerId)!!
        timeline.play { battleInfoView.displayPlayerWin(player.name) }
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }
}
