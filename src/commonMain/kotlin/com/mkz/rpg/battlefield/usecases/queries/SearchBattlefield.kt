package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldRepository

class SearchBattlefield(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(): Battlefield.Dto? = battlefieldRepository.search()?.toDto()
}
