package com.mkz.rpg.worldBuilder.usecases.queries

import com.mkz.rpg.battlefield.usecases.queries.ExportBattlefield
import com.mkz.rpg.worldBuilder.domain.WorldBuilderError.WorldNotCreated
import com.mkz.rpg.worldBuilder.domain.WorldBuilderRepository

class ExportWorld(
    private val worldBuilderRepository: WorldBuilderRepository,
    private val exportBattlefield: ExportBattlefield,
) {
    operator fun invoke(): String {
        val worldBuilder = worldBuilderRepository.search() ?: throw WorldNotCreated()
        return exportBattlefield(worldBuilder.toDto().tiles)
    }
}
