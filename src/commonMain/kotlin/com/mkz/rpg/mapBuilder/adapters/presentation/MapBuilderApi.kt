package com.mkz.rpg.mapBuilder.adapters.presentation

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.mapBuilder.adapters.storage.InMemoryMapBuilderRepository
import com.mkz.rpg.mapBuilder.domain.MapBuilderRepository
import com.mkz.rpg.mapBuilder.usecases.commands.CreateMap
import com.mkz.rpg.mapBuilder.usecases.commands.ImportMap
import com.mkz.rpg.mapBuilder.usecases.commands.PaintTile
import com.mkz.rpg.mapBuilder.usecases.commands.ResizeMap
import com.mkz.rpg.mapBuilder.usecases.commands.SelectTerrain
import com.mkz.rpg.mapBuilder.usecases.queries.ExportMap
import com.mkz.rpg.mapBuilder.usecases.queries.SearchMap
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi

class MapBuilderApi(
    terrainApi: TerrainApi,
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
) {
    // Storage
    private val mapBuilderRepository: MapBuilderRepository = InMemoryMapBuilderRepository()

    // Commands
    val createMap = CreateMap(mapBuilderRepository, eventBus)
    val resizeMap = ResizeMap(mapBuilderRepository, eventBus)
    val selectTerrain = SelectTerrain(mapBuilderRepository, eventBus)
    val paintTile = PaintTile(mapBuilderRepository, terrainApi.isTransitionAllowed, eventBus)
    val importMap = ImportMap(mapBuilderRepository, battlefieldApi.parseBattlefield, terrainApi.searchTerrainById, terrainApi.isTransitionAllowed, eventBus)

    // Queries
    val searchMap = SearchMap(mapBuilderRepository)
    val exportMap = ExportMap(mapBuilderRepository, battlefieldApi.exportBattlefield)
}
