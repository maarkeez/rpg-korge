package com.mkz.tessavar.worldBuilder.adapters.presentation

import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.worldBuilder.adapters.storage.InMemoryWorldBuilderRepository
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderRepository
import com.mkz.tessavar.worldBuilder.usecases.commands.CreateWorld
import com.mkz.tessavar.worldBuilder.usecases.commands.ImportWorld
import com.mkz.tessavar.worldBuilder.usecases.commands.PaintTile
import com.mkz.tessavar.worldBuilder.usecases.commands.ResizeWorld
import com.mkz.tessavar.worldBuilder.usecases.commands.SelectTerrain
import com.mkz.tessavar.worldBuilder.usecases.queries.ExportWorld
import com.mkz.tessavar.worldBuilder.usecases.queries.SearchWorld

class WorldBuilderApi(
    terrainApi: TerrainApi,
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
) {
    private val worldBuilderRepository: WorldBuilderRepository = InMemoryWorldBuilderRepository()

    val createWorld = CreateWorld(worldBuilderRepository, eventBus)
    val resizeWorld = ResizeWorld(worldBuilderRepository, eventBus)
    val selectTerrain = SelectTerrain(worldBuilderRepository, eventBus)
    val paintTile = PaintTile(worldBuilderRepository, terrainApi.hasTerrainTransition, eventBus)
    val importWorld = ImportWorld(worldBuilderRepository, battlefieldApi.parseBattlefield, terrainApi.searchTerrainById, terrainApi.hasTerrainTransition, eventBus)

    val searchWorld = SearchWorld(worldBuilderRepository)
    val exportWorld = ExportWorld(worldBuilderRepository, battlefieldApi.exportBattlefield)
}
