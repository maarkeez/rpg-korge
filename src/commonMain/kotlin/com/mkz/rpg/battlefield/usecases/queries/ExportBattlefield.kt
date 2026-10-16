package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.usecases.services.BattlefieldEncoder

/** The terrain id of each tile, written in the format the battlefield is loaded from, e.g.: to save a designed map. */
class ExportBattlefield(
    private val battlefieldEncoder: BattlefieldEncoder,
) {
    operator fun invoke(tiles: List<List<String>>): String = battlefieldEncoder.encode(tiles)
}
