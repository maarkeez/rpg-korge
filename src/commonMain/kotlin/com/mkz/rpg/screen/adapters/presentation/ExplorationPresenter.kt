package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.exploration.adapters.presentation.ExplorationApi
import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import com.mkz.rpg.exploration.domain.ExplorationEvent.ExplorationPaused
import com.mkz.rpg.exploration.domain.ExplorationEvent.ExplorationResumed
import com.mkz.rpg.exploration.domain.ExplorationEvent.ExplorationStarted
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Walks the party with the virtual pad while exploring. The pad takes the place of the battle HUD and the player call
 * to action, which are only displayed while battling. It also counts the seconds while exploring, for the terrain
 * effects of the battle units standing still. Once the game is over, the pad is hidden and the seconds are no longer
 * counted.
 */
class ExplorationPresenter(
    private val virtualPadView: VirtualPadView,
    private val battleHudView: BattleHudView,
    private val playerCallToActionView: PlayerCallToActionView,
    private val explorationApi: ExplorationApi,
    private val timeline: BattleSceneTimeline,
    eventBus: EventBus,
) {
    companion object {
        // How often the battle units standing still receive the effect of their terrain while exploring.
        val STANDING_TERRAIN_EFFECT_INTERVAL = 1.seconds
    }

    private var elapsedSinceTerrainEffects = Duration.ZERO
    private var isGameOver = false

    private val subscriptions =
        listOf(
            eventBus.subscribe<ExplorationStarted> {
                timeline.play { displayExploring(isExploring = true) }
            },
            eventBus.subscribe<ExplorationPaused> {
                timeline.play { displayExploring(isExploring = false) }
            },
            eventBus.subscribe<ExplorationResumed> {
                timeline.play { displayExploring(isExploring = true) }
            },
            eventBus.subscribe<PlayerVictory> {
                isGameOver = true
                timeline.play { virtualPadView.visible = false }
            },
        )

    init {
        virtualPadView.visible = false
        virtualPadView.setOnDirectionHeld { direction -> walkParty(direction) }
    }

    /** Counts the time elapsed since the previous frame, and applies the standing terrain effects every second. */
    fun tick(elapsed: Duration) {
        if (isGameOver) return
        elapsedSinceTerrainEffects += elapsed
        while (elapsedSinceTerrainEffects >= STANDING_TERRAIN_EFFECT_INTERVAL) {
            elapsedSinceTerrainEffects -= STANDING_TERRAIN_EFFECT_INTERVAL
            explorationApi.applyStandingTerrainEffects()
        }
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
