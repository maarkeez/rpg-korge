package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.exploration.adapters.presentation.ExplorationApi
import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import com.mkz.rpg.exploration.domain.ExplorationEvent.ExplorationPaused
import com.mkz.rpg.exploration.domain.ExplorationEvent.ExplorationStarted
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe

/**
 * Walks the party with the virtual pad while exploring. The pad takes the place of the battle HUD and the player call
 * to action, which are only displayed while battling.
 */
class ExplorationPresenter(
    private val virtualPadView: VirtualPadView,
    private val battleHudView: BattleHudView,
    private val playerCallToActionView: PlayerCallToActionView,
    private val explorationApi: ExplorationApi,
    private val timeline: BattleSceneTimeline,
    eventBus: EventBus,
) {
    private val subscriptions =
        listOf(
            eventBus.subscribe<ExplorationStarted> {
                timeline.play { displayExploring(isExploring = true) }
            },
            eventBus.subscribe<ExplorationPaused> {
                timeline.play { displayExploring(isExploring = false) }
            },
        )

    init {
        virtualPadView.visible = false
        virtualPadView.setOnDirectionHeld { direction -> walkParty(direction) }
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    // The party only walks once the scene shows where it is, so a held arrow can not walk ahead of the animation.
    private fun walkParty(direction: DirectionDto) {
        if (timeline.isPlaying) return
        explorationApi.walkParty(direction)
    }

    private fun displayExploring(isExploring: Boolean) {
        virtualPadView.visible = isExploring
        battleHudView.visible = !isExploring
        playerCallToActionView.visible = !isExploring
    }
}
