package com.mkz.rpg.mapBuilder.usecases.queries

import com.mkz.rpg.battlefield.usecases.queries.ExportBattlefield
import com.mkz.rpg.mapBuilder.domain.MapBuilderError.MapNotCreated
import com.mkz.rpg.mapBuilder.domain.MapBuilderRepository

/** The map being designed, written in the format the battlefield is loaded from. */
class ExportMap(
    private val mapBuilderRepository: MapBuilderRepository,
    private val exportBattlefield: ExportBattlefield,
) {
    operator fun invoke(): String {
        val mapBuilder = mapBuilderRepository.search() ?: throw MapNotCreated()
        return exportBattlefield(mapBuilder.toDto().tiles)
    }
}
