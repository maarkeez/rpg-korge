package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.usecases.services.BattlefieldEncoder

class ExportBattlefield(
    private val battlefieldEncoder: BattlefieldEncoder,
) {
    operator fun invoke(tiles: List<List<String>>): String = battlefieldEncoder.encode(tiles)
}
