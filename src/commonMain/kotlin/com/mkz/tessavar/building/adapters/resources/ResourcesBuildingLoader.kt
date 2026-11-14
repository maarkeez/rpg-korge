package com.mkz.tessavar.building.adapters.resources

import com.mkz.tessavar.building.domain.Building
import com.mkz.tessavar.building.usecases.services.BuildingLoader
import com.mkz.tessavar.shared.adapters.resources.ResourcesBundle
import com.mkz.tessavar.shared.adapters.toml.readTomlFiles
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

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
