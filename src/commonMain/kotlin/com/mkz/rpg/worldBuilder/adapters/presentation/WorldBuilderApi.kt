package com.mkz.rpg.worldBuilder.adapters.presentation

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.worldBuilder.adapters.storage.InMemoryWorldBuilderRepository
import com.mkz.rpg.worldBuilder.domain.WorldBuilderRepository
import com.mkz.rpg.worldBuilder.usecases.commands.CreateWorld
import com.mkz.rpg.worldBuilder.usecases.commands.ImportWorld
import com.mkz.rpg.worldBuilder.usecases.commands.PaintTile
import com.mkz.rpg.worldBuilder.usecases.commands.ResizeWorld
import com.mkz.rpg.worldBuilder.usecases.commands.SelectTerrain
import com.mkz.rpg.worldBuilder.usecases.queries.ExportWorld
import com.mkz.rpg.worldBuilder.usecases.queries.SearchWorld

class WorldBuilderApi(
    terrainApi: TerrainApi,
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
) {
    // Storage
    private val worldBuilderRepository: WorldBuilderRepository = InMemoryWorldBuilderRepository()

    // Commands
    val createWorld = CreateWorld(worldBuilderRepository, eventBus)
    val resizeWorld = ResizeWorld(worldBuilderRepository, eventBus)
    val selectTerrain = SelectTerrain(worldBuilderRepository, eventBus)
    val paintTile = PaintTile(worldBuilderRepository, terrainApi.hasTerrainTransition, eventBus)
    val importWorld = ImportWorld(worldBuilderRepository, battlefieldApi.parseBattlefield, terrainApi.searchTerrainById, terrainApi.hasTerrainTransition, eventBus)

    // Queries
    val searchWorld = SearchWorld(worldBuilderRepository)
    val exportWorld = ExportWorld(worldBuilderRepository, battlefieldApi.exportBattlefield)
}
