package com.mkz.rpg.battlefield.usecases.services

/** Writes the terrain id of each tile in the format the battlefield is loaded from, see [BattlefieldLoader]. */
interface BattlefieldEncoder {
    fun encode(tiles: List<List<String>>): String
}
