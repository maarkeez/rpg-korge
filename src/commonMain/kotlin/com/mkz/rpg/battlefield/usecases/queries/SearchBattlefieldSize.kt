package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldRepository

class SearchBattlefieldSize(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(): Battlefield.Dto.SizeDto? = battlefieldRepository.search()?.size()
}
