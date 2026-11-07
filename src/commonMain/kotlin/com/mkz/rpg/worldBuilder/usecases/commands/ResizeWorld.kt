package com.mkz.rpg.worldBuilder.usecases.commands

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.worldBuilder.domain.WorldBuilderError.WorldNotCreated
import com.mkz.rpg.worldBuilder.domain.WorldBuilderRepository

class ResizeWorld(
    private val worldBuilderRepository: WorldBuilderRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        rows: Int,
        columns: Int,
    ) {
        val worldBuilder = worldBuilderRepository.search() ?: throw WorldNotCreated()
        val (events, resizedWorldBuilder) = worldBuilder.resize(rows = rows, columns = columns).pullEvents()
        worldBuilderRepository.save(resizedWorldBuilder)
        eventBus.publish(events)
    }
}
