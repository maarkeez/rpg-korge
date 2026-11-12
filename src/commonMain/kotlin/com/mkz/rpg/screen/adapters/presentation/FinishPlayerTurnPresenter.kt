package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.BattleFinished
import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class FinishPlayerTurnPresenter(
    private val callToActionView: CallToActionView,
    private val battleApi: BattleApi,
    private val playerApi: PlayerApi,
    private val timeline: GameSceneTimeline,
    eventBus: EventBus,
) {
    private var isHumanPlayerTurnDisplayed = false

    private val subscriptions =
        listOf(
            eventBus.subscribe<BattleStarted> {
                timeline.playNext { callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = { finishPlayerTurn() }) }
            },
            eventBus.subscribe<BattleFinished> {
                timeline.play {
                    isHumanPlayerTurnDisplayed = false
                    callToActionView.hideFinishPlayerTurn()
                }
            },
            eventBus.subscribe<PlayerTurnStarted> { event ->
                val isHumanTurn = playerApi.searchPlayerById(event.playerId)?.type == PlayerTypeDto.HUMAN
                timeline.play { isHumanPlayerTurnDisplayed = isHumanTurn }
            },
        )

    init {
        timeline.onPlayingChanged { updateFinishPlayerTurn() }
        updateFinishPlayerTurn()
    }

    private fun finishPlayerTurn() {
        if (!canFinishPlayerTurn()) return
        isHumanPlayerTurnDisplayed = false
        updateFinishPlayerTurn()
        battleApi.finishPlayerTurn()
    }

    private fun canFinishPlayerTurn() = isHumanPlayerTurnDisplayed && !timeline.isPlaying

    private fun updateFinishPlayerTurn() = callToActionView.setFinishPlayerTurnEnabled(canFinishPlayerTurn())
}
