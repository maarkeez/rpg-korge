package com.mkz.tessavar.battlefield.usecases.queries

import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.battlefield.usecases.services.BattlefieldCreation

class PreviewBattlefield(
    private val battlefieldCreation: BattlefieldCreation,
) {
    operator fun invoke(tiles: List<List<String>>): Battlefield.Dto = battlefieldCreation.preview(tiles)
}
