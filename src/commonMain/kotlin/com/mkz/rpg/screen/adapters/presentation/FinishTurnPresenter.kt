package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class FinishTurnPresenter(
    private val playerCallToActionView: PlayerCallToActionView,
    private val battleApi: BattleApi,
    private val playerApi: PlayerApi,
    private val timeline: BattleSceneTimeline,
    eventBus: EventBus,
) {
    // Whether the scene displays a human player turn. It changes in timeline order, not when the turn starts.
    private var isHumanTurnDisplayed = false

    private val subscriptions =
        listOf(
            eventBus.subscribe<PlayerTurnStarted> { event ->
                val isHumanTurn = playerApi.searchPlayerById(event.playerId)?.type == PlayerTypeDto.HUMAN
                timeline.play { isHumanTurnDisplayed = isHumanTurn }
            },
            eventBus.subscribe<PlayerVictory> {
                timeline.play { playerCallToActionView.hide() }
            },
        )

    init {
        playerCallToActionView.displayFinishTurn(onTurnFinished = { finishTurn() })
        timeline.onPlayingChanged { updateFinishTurn() }
        updateFinishTurn()
    }

    private fun finishTurn() {
        if (!canFinishTurn()) return
        isHumanTurnDisplayed = false
        updateFinishTurn()
        battleApi.finishPlayerTurn()
    }

    // The player can only finish the turn once the scene shows the current state of their turn.
    private fun canFinishTurn() = isHumanTurnDisplayed && !timeline.isPlaying

    private fun updateFinishTurn() = playerCallToActionView.setFinishTurnEnabled(canFinishTurn())
}
