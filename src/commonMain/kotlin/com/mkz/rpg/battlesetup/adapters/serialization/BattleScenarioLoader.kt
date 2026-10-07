package com.mkz.rpg.battlesetup.adapters.serialization

import com.mkz.rpg.battlesetup.domain.Battlesetup
import korlibs.io.file.std.resourcesVfs
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BattleScenarioJson(
    val seed: Long? = null,
    val battlefield: BattlefieldJson,
    val deployments: List<DeploymentJson>,
) {
    @Serializable
    data class BattlefieldJson(
        val rows: Int,
        val columns: Int,
        val tiles: List<List<String>>,
    )

    @Serializable
    data class DeploymentJson(
        val battleUnitId: String,
        val unitId: String,
        val playerId: String,
        val row: Int,
        val column: Int,
    )
}

class BattleScenarioLoader {
    suspend fun load(path: String): Battlesetup {
        val scenarioJson = Json.decodeFromString<BattleScenarioJson>(resourcesVfs[path].readString())
        return Battlesetup.create(
            Battlesetup.Dto(
                seed = scenarioJson.seed,
                battlefield = Battlesetup.Dto.BattlefieldDto(scenarioJson.battlefield.rows, scenarioJson.battlefield.columns, scenarioJson.battlefield.tiles),
                deployments = scenarioJson.deployments.map { Battlesetup.Dto.DeploymentDto(it.battleUnitId, it.unitId, it.playerId, it.row, it.column) },
            ),
        )
    }
}
