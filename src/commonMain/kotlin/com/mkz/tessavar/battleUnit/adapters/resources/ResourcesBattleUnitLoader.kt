package com.mkz.tessavar.battleUnit.adapters.resources

import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitLoader
import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.tessavar.shared.adapters.resources.ResourcesBundle
import com.mkz.tessavar.shared.adapters.toml.readTomlFiles
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

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
