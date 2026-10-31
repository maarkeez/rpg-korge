package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldRepository

/** What is saved of the battlefield: the occupant of each tile. The map itself is read from the resources again. */
class SearchBattlefieldSnapshot(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(): List<Battlefield.Dto.OccupantDto> = battlefieldRepository.search()?.occupants().orEmpty()
}
