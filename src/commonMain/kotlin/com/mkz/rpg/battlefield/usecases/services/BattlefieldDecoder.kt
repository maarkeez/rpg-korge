package com.mkz.rpg.battlefield.usecases.services

/** Reads the terrain id of each tile from the format the battlefield is loaded from, see [BattlefieldLoader]. */
interface BattlefieldDecoder {
    /** Throws `InvalidBattlefieldFormat` when the text is not a battlefield, e.g.: its rows have different lengths. */
    fun decode(text: String): List<List<String>>
}
