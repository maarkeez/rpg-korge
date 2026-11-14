package com.mkz.tessavar.battlefield.usecases.queries

import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.battlefield.domain.BattlefieldRepository

class SearchBattlefieldSnapshot(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(): List<Battlefield.Dto.OccupantDto> = battlefieldRepository.search()?.occupants().orEmpty()
}
