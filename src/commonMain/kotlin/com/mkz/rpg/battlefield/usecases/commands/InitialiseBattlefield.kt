package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.usecases.services.BattlefieldCreation
import com.mkz.rpg.battlefield.usecases.services.BattlefieldLoader
import com.mkz.rpg.shared.domain.EventBus

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
