package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.usecases.services.BattlefieldDecoder

/**
 * The terrain id of each tile written in a text in the format the battlefield is loaded from, e.g.: an imported map.
 * Throws `InvalidBattlefieldFormat` when the text is not a battlefield.
 */
class ParseBattlefield(
    private val battlefieldDecoder: BattlefieldDecoder,
) {
    operator fun invoke(text: String): List<List<String>> = battlefieldDecoder.decode(text)
}
