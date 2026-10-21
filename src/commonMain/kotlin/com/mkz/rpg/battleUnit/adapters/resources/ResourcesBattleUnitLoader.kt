package com.mkz.rpg.battleUnit.adapters.resources

import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

/** Loads one battle unit deployment per `battleUnit/<battle unit id>.toml` resource. */
class ResourcesBattleUnitLoader(
    private val battleUnitRoot: VfsFile = ResourcesBundle.root["battleUnit"],
) : BattleUnitLoader {
    private var deployments = emptyList<Deployment>()

    override suspend fun initResources() {
        deployments =
            battleUnitRoot.readTomlFiles<DeploymentDefinition>().map { (battleUnitId, definition) ->
                definition.toDeployment(battleUnitId)
            }
    }

    override fun loadDeployments(): List<Deployment> = deployments

    @Serializable
    data class DeploymentDefinition(
        val unitId: String,
        val playerId: String,
        val row: Int,
        val column: Int,
    ) {
        fun toDeployment(battleUnitId: String): Deployment =
            Deployment(
                battleUnitId = battleUnitId,
                unitId = unitId,
                playerId = playerId,
                row = row,
                column = column,
            )
    }
}
