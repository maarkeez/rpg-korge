package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.usecases.services.BattlefieldDecoder

class ParseBattlefield(
    private val battlefieldDecoder: BattlefieldDecoder,
) {
    operator fun invoke(text: String): List<List<String>> = battlefieldDecoder.decode(text)
}
