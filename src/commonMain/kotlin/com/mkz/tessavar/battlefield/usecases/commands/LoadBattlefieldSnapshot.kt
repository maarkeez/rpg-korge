package com.mkz.tessavar.battlefield.usecases.commands

import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.battlefield.domain.BattlefieldRepository

class LoadBattlefieldSnapshot(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(occupants: List<Battlefield.Dto.OccupantDto>) {
        val battlefield = requireNotNull(battlefieldRepository.search()) { "The battlefield is not created yet" }
        battlefieldRepository.update(battlefield.loadOccupants(occupants))
    }
}
