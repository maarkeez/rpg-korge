package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.shared.domain.EventBus

class InitializeBattlefield(
    private val battlefieldRepository: BattlefieldRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        rows: Int,
        columns: Int,
        tiles: List<List<String>>,
    ) {
        if (battlefieldRepository.search() != null) return
        val (events, battlefield) = Battlefield.create(rows, columns, tiles).pullEvents()
        battlefieldRepository.create(battlefield)
        eventBus.publish(events)
    }
}
