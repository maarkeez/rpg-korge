package com.mkz.rpg.building.adapters.resources

import com.mkz.rpg.building.domain.Building
import com.mkz.rpg.building.usecases.services.BuildingLoader
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

/** Loads one building per `building/<building id>.toml` resource. */
class ResourcesBuildingLoader(
    private val buildingRoot: VfsFile = ResourcesBundle.root["building"],
) : BuildingLoader {
    private var buildings = emptyList<Building.Dto>()

    override suspend fun initResources() {
        buildings =
            buildingRoot.readTomlFiles<BuildingDefinition>().map { (buildingId, definition) ->
                definition.toDto(buildingId)
            }
    }

    override fun loadBuildings(): List<Building.Dto> = buildings

    @Serializable
    data class BuildingDefinition(
        val row: Int,
        val column: Int,
        val rows: Int,
        val columns: Int,
    ) {
        fun toDto(buildingId: String): Building.Dto =
            Building.Dto(
                id = buildingId,
                row = row,
                column = column,
                rows = rows,
                columns = columns,
            )
    }
}
