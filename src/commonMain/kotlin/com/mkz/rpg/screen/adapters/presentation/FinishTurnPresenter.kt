package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.BattleFinished
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
            // Together with the battle HUD, which takes the place of the virtual pad as soon as the battle starts.
            eventBus.subscribe<BattleStarted> {
                timeline.playNext { playerCallToActionView.displayFinishTurn(onTurnFinished = { finishTurn() }) }
            },
            // E.g.: during the human player turn, once the last CPU battle unit is defeated.
            eventBus.subscribe<BattleFinished> {
                timeline.play {
                    isHumanTurnDisplayed = false
                    playerCallToActionView.hideFinishTurn()
                }
            },
            eventBus.subscribe<PlayerTurnStarted> { event ->
                val isHumanTurn = playerApi.searchPlayerById(event.playerId)?.type == PlayerTypeDto.HUMAN
                timeline.play { isHumanTurnDisplayed = isHumanTurn }
            },
            eventBus.subscribe<PlayerVictory> {
                timeline.play { playerCallToActionView.hide() }
            },
        )

    init {
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
