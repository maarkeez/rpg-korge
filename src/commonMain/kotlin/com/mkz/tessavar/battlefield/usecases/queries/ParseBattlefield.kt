package com.mkz.tessavar.battlefield.usecases.queries

import com.mkz.tessavar.battlefield.usecases.services.BattlefieldDecoder

class ParseBattlefield(
    private val battlefieldDecoder: BattlefieldDecoder,
) {
    operator fun invoke(text: String): List<List<String>> = battlefieldDecoder.decode(text)
}
