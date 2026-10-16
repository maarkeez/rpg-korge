package com.mkz.rpg.mapBuilder.usecases.commands

import com.mkz.rpg.mapBuilder.domain.MapBuilderError.MapNotCreated
import com.mkz.rpg.mapBuilder.domain.MapBuilderRepository
import com.mkz.rpg.shared.domain.EventBus

class ResizeMap(
    private val mapBuilderRepository: MapBuilderRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        rows: Int,
        columns: Int,
    ) {
        val mapBuilder = mapBuilderRepository.search() ?: throw MapNotCreated()
        val (events, resizedMapBuilder) = mapBuilder.resize(rows = rows, columns = columns).pullEvents()
        mapBuilderRepository.save(resizedMapBuilder)
        eventBus.publish(events)
    }
}
