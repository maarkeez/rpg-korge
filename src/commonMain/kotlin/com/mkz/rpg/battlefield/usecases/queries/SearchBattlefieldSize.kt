package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldRepository

/** The rows and the columns of the battlefield, without copying its tiles. */
class SearchBattlefieldSize(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(): Battlefield.Dto.SizeDto? = battlefieldRepository.search()?.size()
}
