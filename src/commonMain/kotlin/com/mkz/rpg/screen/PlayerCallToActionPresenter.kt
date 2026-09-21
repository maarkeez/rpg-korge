package com.mkz.rpg.screen

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class PlayerCallToActionPresenter(
    private val playerCallToActionView: PlayerCallToActionView,
    private val battleApi: BattleApi,
    eventBus: EventBus,
) {
    private val subscriptions =
        listOf(
            eventBus.subscribe<PlayerVictory> { event ->
                playerCallToActionView.hide()
            },
        )

    init {
        playerCallToActionView.displayFinishTurn(onTurnFinished = { battleApi.finishPlayerTurn() })
    }
}
