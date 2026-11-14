package com.mkz.tessavar.worldBuilder.usecases.queries

import com.mkz.tessavar.battlefield.usecases.queries.ExportBattlefield
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderError.WorldNotCreated
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderRepository

class ExportWorld(
    private val worldBuilderRepository: WorldBuilderRepository,
    private val exportBattlefield: ExportBattlefield,
) {
    operator fun invoke(): String {
        val worldBuilder = worldBuilderRepository.search() ?: throw WorldNotCreated()
        return exportBattlefield(worldBuilder.toDto().tiles)
    }
}
