package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.exploration.domain.ExplorationRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Called every second while exploring: the battle units that stood still during that second receive the effect of
 * their terrain (e.g.: lava hurts, water heals), the CPU battle units included. Walking over a tile does not apply it.
 * Then, the next second starts (`ExplorationSecondStarted`).
 */
class ApplyStandingTerrainEffects(
    private val explorationRepository: ExplorationRepository,
    private val searchBattlefield: SearchBattlefield,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val exploration = explorationRepository.search() ?: return
        if (!exploration.toDto().isExploring) return
        val occupantIds =
            searchBattlefield()
                ?.tiles
                ?.values
                ?.mapNotNull { tile -> tile.battleUnitId }
                ?.toSet()
                .orEmpty()
        val standingBattleUnitIds = exploration.standingBattleUnitIds(occupantIds)
        val (events, nextSecondExploration) = exploration.startNextSecond().pullEvents()
        explorationRepository.save(nextSecondExploration)
        if (standingBattleUnitIds.isNotEmpty()) {
            eventBus.publish(BattlefieldEvent.RequestEffectApplicationToOccupants(battleUnitIds = standingBattleUnitIds))
        }
        // After the effects, so the battle units moving during the new second (e.g.: wandering) count as moved in it.
        eventBus.publish(events)
    }
}
