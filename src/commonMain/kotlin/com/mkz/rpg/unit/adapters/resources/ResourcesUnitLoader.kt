package com.mkz.rpg.unit.adapters.resources

import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import com.mkz.rpg.shared.adapters.toml.fromResource
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.usecases.services.UnitLoader
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

/** Loads one unit per `unit/<unit id>.toml` resource. Starting gold: 0 when it has none. */
class ResourcesUnitLoader(
    private val unitRoot: VfsFile = ResourcesBundle.root["unit"],
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
        val baseMaximumHealth: Int,
        val baseMaximumMana: Int,
        val maximumSteps: Int,
        val startingAbilities: List<String>,
        val startingGold: Int = 0,
    ) {
        fun toDto(unitId: String): Unit.Dto =
            Unit.Dto(
                id = unitId,
                baseMaximumHealth = baseMaximumHealth,
                baseMaximumMana = baseMaximumMana,
                startingAbilities = startingAbilities,
                maximumSteps = maximumSteps,
                startingGold = startingGold,
            )
    }
}
