package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class FinishTurnPresenter(
    private val playerCallToActionView: PlayerCallToActionView,
    private val battleApi: BattleApi,
    private val timeline: BattleSceneTimeline,
    eventBus: EventBus,
) {
    private val subscriptions =
        listOf(
            eventBus.subscribe<PlayerVictory> {
                timeline.play { playerCallToActionView.hide() }
            },
        )

    init {
        // The player can only finish the turn once the scene shows the current state of the battle.
        playerCallToActionView.displayFinishTurn(onTurnFinished = { if (!timeline.isPlaying) battleApi.finishPlayerTurn() })
    }
}
