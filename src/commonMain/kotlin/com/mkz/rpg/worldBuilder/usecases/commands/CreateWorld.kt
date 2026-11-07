package com.mkz.rpg.worldBuilder.usecases.commands

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.worldBuilder.domain.WorldBuilder
import com.mkz.rpg.worldBuilder.domain.WorldBuilderRepository

/** Starts building a new world, replacing the previous one. */
class CreateWorld(
    private val worldBuilderRepository: WorldBuilderRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val (events, worldBuilder) = WorldBuilder.create().pullEvents()
        worldBuilderRepository.save(worldBuilder)
        eventBus.publish(events)
    }
}
