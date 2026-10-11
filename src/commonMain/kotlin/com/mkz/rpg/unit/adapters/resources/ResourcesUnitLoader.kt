package com.mkz.rpg.unit.adapters.resources

import com.mkz.rpg.shared.adapters.toml.fromResource
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.usecases.services.UnitLoader
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import kotlinx.serialization.Serializable

/** Loads one unit per `unit/<unit id>.toml` resource. */
class ResourcesUnitLoader(
    private val unitRoot: VfsFile = resourcesVfs["unit"],
) : UnitLoader {
    private var units = emptyList<Unit.Dto>()

    override suspend fun initResources() {
        units =
            unitRoot.readTomlFiles<UnitDefinition>().map { (unitId, definition) ->
                fromResource("unit/$unitId.toml") { definition.toDto(unitId) }
            }
    }

    override fun loadUnits(): List<Unit.Dto> = units

    @Serializable
    data class UnitDefinition(
        val name: String,
        val healthPoints: Int,
        val manaPoints: Int,
        val movementRange: Int,
        val abilities: List<String>,
    ) {
        fun toDto(unitId: String): Unit.Dto =
            Unit.Dto(
                id = unitId,
                name = name,
                healthPoints = healthPoints,
                manaPoints = manaPoints,
                abilities = abilities,
                movementRange = movementRange,
            )
    }
}
