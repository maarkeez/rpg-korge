package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldRepository

/**
 * Puts back the occupants a save took, every other tile vacant, publishing no events. Fails when the battlefield is not
 * created yet, or when a tile is no longer on its map.
 */
class RestoreBattlefield(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(occupants: List<Battlefield.Dto.OccupantDto>) {
        val battlefield = requireNotNull(battlefieldRepository.search()) { "The battlefield is not created yet" }
        battlefieldRepository.update(battlefield.restoreOccupants(occupants))
    }
}
