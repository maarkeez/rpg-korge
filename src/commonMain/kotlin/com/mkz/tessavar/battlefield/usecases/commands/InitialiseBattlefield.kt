package com.mkz.tessavar.battlefield.usecases.commands

import com.mkz.tessavar.battlefield.usecases.services.BattlefieldCreation
import com.mkz.tessavar.battlefield.usecases.services.BattlefieldLoader
import com.mkz.tessavar.shared.domain.EventBus

class InitialiseBattlefield(
    private val battlefieldLoader: BattlefieldLoader,
    private val battlefieldCreation: BattlefieldCreation,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val tiles = battlefieldLoader.loadTiles()
        eventBus.publish(battlefieldCreation.create(rows = tiles.size, columns = tiles.firstOrNull()?.size ?: 0, tiles = tiles))
    }
}
