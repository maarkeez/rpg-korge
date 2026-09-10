package com.mkz.rpg.screen

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe

class FinishTurnPresenter(
    private val finishTurnView: FinishTurnView,
    private val battleApi: BattleApi,
    eventBus: EventBus,
) : FinishTurnView.Delegate {
    private val subscriptions =
        listOf(
            eventBus.subscribe<PlayerVictory> { event ->
                hideFinishTurn()
            },
        )

    init {
        finishTurnView.setDelegate(this)
    }

    override fun finishTurn() {
        battleApi.finishPlayerTurn()
    }

    fun hideFinishTurn() {
        finishTurnView.hide()
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }
}
