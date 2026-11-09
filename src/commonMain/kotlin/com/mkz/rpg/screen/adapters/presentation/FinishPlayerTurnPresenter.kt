package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.BattleFinishedWithoutWinner
import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

/**
 * Displays the finish turn during the battles only, as there are no turns while exploring. It is enabled once the scene
 * displays a human player turn.
 */
class FinishPlayerTurnPresenter(
    private val callToActionView: CallToActionView,
    private val battleApi: BattleApi,
    private val playerApi: PlayerApi,
    private val timeline: GameSceneTimeline,
    eventBus: EventBus,
) {
    // Whether the scene displays a human player turn. It changes in timeline order, not when the turn starts.
    private var isHumanPlayerTurnDisplayed = false

    private val subscriptions =
        listOf(
            // Together with the HUD, which takes the place of the virtual pad as soon as the battle starts.
            eventBus.subscribe<BattleStarted> {
                timeline.playNext { callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = { finishPlayerTurn() }) }
            },
            // E.g.: during the human player turn, once the last CPU battle unit is defeated.
            eventBus.subscribe<BattleFinishedWithoutWinner> {
                timeline.play {
                    isHumanPlayerTurnDisplayed = false
                    callToActionView.hideFinishPlayerTurn()
                }
            },
            eventBus.subscribe<PlayerTurnStarted> { event ->
                val isHumanTurn = playerApi.searchPlayerById(event.playerId)?.type == PlayerTypeDto.HUMAN
                timeline.play { isHumanPlayerTurnDisplayed = isHumanTurn }
            },
            eventBus.subscribe<PlayerVictory> {
                timeline.play { callToActionView.hide() }
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

    // The player can only finish the turn once the scene shows the current state of their turn.
    private fun canFinishPlayerTurn() = isHumanPlayerTurnDisplayed && !timeline.isPlaying

    private fun updateFinishPlayerTurn() = callToActionView.setFinishPlayerTurnEnabled(canFinishPlayerTurn())
}
