package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Called every second while exploring: the battle units that stood still during that second receive the effect of
 * their terrain (e.g.: lava hurts, water heals), the CPU battle units included. Walking over a tile does not apply it,
 * and the other occupants (e.g.: the npcs) receive none.
 * Then, the next second starts (`ExplorationSecondStarted`).
 */
class ApplyStandingTerrainEffects(
    private val partyRepository: PartyRepository,
    private val searchBattlefield: SearchBattlefield,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val party = partyRepository.search() ?: return
        if (!party.toDto().isExploring) return
        val occupantIds =
            searchBattlefield()
                ?.tiles
                ?.values
                ?.mapNotNull { tile -> tile.battleUnitId }
                ?.filter { occupantId -> searchBattleUnitById(occupantId) != null }
                ?.toSet()
                .orEmpty()
        val standingBattleUnitIds = party.standingBattleUnitIds(occupantIds)
        val (events, nextSecondParty) = party.startNextSecond().pullEvents()
        partyRepository.save(nextSecondParty)
        if (standingBattleUnitIds.isNotEmpty()) {
            eventBus.publish(BattlefieldEvent.RequestEffectApplicationToOccupants(battleUnitIds = standingBattleUnitIds))
        }
        // After the effects, so the battle units moving during the new second (e.g.: wandering) count as moved in it.
        eventBus.publish(events)
    }
}
