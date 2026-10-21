package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.shared.domain.EventBus

/** Announces the dropped items on each tile an occupant walked or teleported onto, so its inventory can store them. */
class FindLoot(
    private val battlefieldRepository: BattlefieldRepository,
    private val eventBus: EventBus,
) {
    /** Path: the tiles walked from the origin (excluded) to the destination (included). */
    operator fun invoke(
        occupantId: String,
        path: List<PositionDto>,
    ) {
        val storedBattlefield = battlefieldRepository.search() ?: return
        val (events, battlefield) = storedBattlefield.findLoot(occupantId, path).pullEvents()
        battlefieldRepository.update(battlefield)
        eventBus.publish(events)
    }
}
