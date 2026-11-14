package com.mkz.tessavar.battlefield.usecases.queries

import com.mkz.tessavar.battlefield.usecases.services.BattlefieldEncoder

class ExportBattlefield(
    private val battlefieldEncoder: BattlefieldEncoder,
) {
    operator fun invoke(tiles: List<List<String>>): String = battlefieldEncoder.encode(tiles)
}
