package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.usecases.services.BattlefieldCreation

class PreviewBattlefield(
    private val battlefieldCreation: BattlefieldCreation,
) {
    operator fun invoke(tiles: List<List<String>>): Battlefield.Dto = battlefieldCreation.preview(tiles)
}
