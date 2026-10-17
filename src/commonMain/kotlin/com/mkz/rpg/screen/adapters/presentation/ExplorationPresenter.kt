package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.exploration.adapters.presentation.ExplorationApi
import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import com.mkz.rpg.exploration.domain.ExplorationEvent.ExplorationPaused
import com.mkz.rpg.exploration.domain.ExplorationEvent.ExplorationResumed
import com.mkz.rpg.exploration.domain.ExplorationEvent.ExplorationStarted
import com.mkz.rpg.inventory.domain.InventoryEvent.InventoryFull
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Walks the party with the virtual pad while exploring. The pad takes the place of the battle HUD and the player call
 * to action, which are displayed while battling, and while exploring when a battle unit of the party is selected (e.g.:
 * to use its inventory) or a found item does not fit in the inventory. It also counts the seconds while exploring, for the terrain
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
                timeline.play { displayVirtualPad(isDisplayed = true) }
            },
            eventBus.subscribe<ExplorationPaused> {
                timeline.play { displayVirtualPad(isDisplayed = false) }
            },
            eventBus.subscribe<ExplorationResumed> {
                timeline.play { displayVirtualPad(isDisplayed = true) }
            },
            // While exploring, selecting a battle unit of the party displays its info and inventory in place of the pad,
            // until the HUD goes idle (e.g.: an empty tile is selected).
            eventBus.subscribe<BattlefieldHudEvent.SelectedBattleUnit> {
                if (isExploring()) timeline.play { displayVirtualPad(isDisplayed = false) }
            },
            eventBus.subscribe<BattlefieldHudEvent.Idle> {
                if (isExploring()) timeline.play { displayVirtualPad(isDisplayed = true) }
            },
            // Straight away, as the timeline waits until the player chooses what to do with the found item.
            eventBus.subscribe<InventoryFull> {
                if (isExploring()) displayVirtualPad(isDisplayed = false)
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

    // The party only walks once the scene shows where it is, so a held arrow can not walk ahead of the animation, nor
    // while the pad is hidden (e.g.: an arrow still held when the inventory is displayed).
    private fun walkParty(direction: DirectionDto) {
        if (timeline.isPlaying || !virtualPadView.visible) return
        explorationApi.walkParty(direction)
    }

    private fun isExploring() = !isGameOver && explorationApi.searchExploration()?.isExploring == true

    // The pad takes the place of the battle HUD and the call to action, or the other way around.
    private fun displayVirtualPad(isDisplayed: Boolean) {
        virtualPadView.visible = isDisplayed
        battleHudView.visible = !isDisplayed
        playerCallToActionView.visible = !isDisplayed
    }
}
