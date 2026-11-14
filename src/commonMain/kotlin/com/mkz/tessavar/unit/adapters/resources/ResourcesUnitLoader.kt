package com.mkz.tessavar.unit.adapters.resources

import com.mkz.tessavar.shared.adapters.resources.ResourcesBundle
import com.mkz.tessavar.shared.adapters.toml.fromResource
import com.mkz.tessavar.shared.adapters.toml.readTomlFiles
import com.mkz.tessavar.unit.domain.Unit
import com.mkz.tessavar.unit.usecases.services.UnitLoader
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

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
