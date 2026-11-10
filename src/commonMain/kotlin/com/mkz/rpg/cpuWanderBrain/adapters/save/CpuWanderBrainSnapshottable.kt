package com.mkz.rpg.cpuWanderBrain.adapters.save

import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.usecases.commands.LoadCpuWanderBrainSnapshot
import com.mkz.rpg.cpuWanderBrain.usecases.queries.SearchCpuWanderBrainSnapshot
import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class CpuWanderBrainSnapshottable(
    private val searchCpuWanderBrainSnapshot: SearchCpuWanderBrainSnapshot,
    private val loadCpuWanderBrainSnapshot: LoadCpuWanderBrainSnapshot,
) : Snapshottable {
    override val key = "cpuWanderBrain"

    override fun snapshot(): String =
        SnapshotJson.encodeToString(
            searchCpuWanderBrainSnapshot()?.let { cpuWanderBrain ->
                CpuWanderBrainSnapshot(
                    homes =
                        cpuWanderBrain.homes.map { home ->
                            HomeSnapshot(home.battleUnitId, home.row, home.column, home.distancesFromHome.map { distance -> listOf(distance.row, distance.column, distance.distance) })
                        },
                    returningBattleUnitIds = cpuWanderBrain.returningBattleUnitIds.toList(),
                    awaitedRespawns = cpuWanderBrain.awaitedRespawns.associate { respawn -> respawn.battleUnitId to respawn.partySteps },
                )
            },
        )

    override fun load(snapshot: String) =
        loadCpuWanderBrainSnapshot(
            SnapshotJson.decodeFromString<CpuWanderBrainSnapshot?>(snapshot)?.let { cpuWanderBrain ->
                CpuWanderBrain.Dto.SnapshotDto(
                    homes =
                        cpuWanderBrain.homes.map { home ->
                            CpuWanderBrain.Dto.HomeSnapshotDto(
                                battleUnitId = home.battleUnitId,
                                row = home.row,
                                column = home.column,
                                distancesFromHome = home.distancesFromHome.map { (row, column, distance) -> CpuWanderBrain.Dto.DistanceDto(row = row, column = column, distance = distance) },
                            )
                        },
                    returningBattleUnitIds = cpuWanderBrain.returningBattleUnitIds.toSet(),
                    awaitedRespawns = cpuWanderBrain.awaitedRespawns.map { (battleUnitId, partySteps) -> CpuWanderBrain.Dto.AwaitedRespawnDto(battleUnitId = battleUnitId, partySteps = partySteps) },
                )
            },
        )
}

@Serializable
private data class CpuWanderBrainSnapshot(
    val homes: List<HomeSnapshot>,
    val returningBattleUnitIds: List<String>,
    val awaitedRespawns: Map<String, Int>,
)

@Serializable
private data class HomeSnapshot(
    val battleUnitId: String,
    val row: Int,
    val column: Int,
    val distancesFromHome: List<List<Int>>,
)
