package com.mkz.tessavar.battlefield.usecases.queries

import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.battlefield.domain.BattlefieldRepository

class SearchBattlefieldSize(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(): Battlefield.Dto.SizeDto? = battlefieldRepository.search()?.size()
}
