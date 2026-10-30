package com.mkz.rpg.cpuWanderBrain.adapters.save

import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import kotlinx.serialization.Serializable

/**
 * The home of each CPU battle unit, the ones returning home, and the party steps each defeated one has counted until it
 * respawns. The wandering in progress is not saved: the battle units wander again on the next second.
 */
class CpuWanderBrainSnapshottable(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
) : Snapshottable {
    override val key = "cpuWanderBrain"

    override fun snapshot(): String =
        SnapshotJson.encodeToString(
            cpuWanderBrainRepository.search()?.toSnapshot()?.let { cpuWanderBrain ->
                CpuWanderBrainSnapshot(
                    homes =
                        cpuWanderBrain.homes.map { home ->
                            HomeSnapshot(home.battleUnitId, home.row, home.column, home.distancesFromHome.map { distance -> listOf(distance.row, distance.column, distance.distance) })
                        },
                    returningBattleUnitIds = cpuWanderBrain.returningBattleUnitIds.toList(),
                    respawns = cpuWanderBrain.respawns.associate { respawn -> respawn.battleUnitId to respawn.partySteps },
                )
            },
        )

    override fun restore(snapshot: String) {
        val cpuWanderBrain = SnapshotJson.decodeFromString<CpuWanderBrainSnapshot?>(snapshot) ?: return
        cpuWanderBrainRepository.save(
            CpuWanderBrain.restore(
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
                    respawns = cpuWanderBrain.respawns.map { (battleUnitId, partySteps) -> CpuWanderBrain.Dto.RespawnDto(battleUnitId = battleUnitId, partySteps = partySteps) },
                ),
            ),
        )
    }
}

// Respawns: the party steps counted by each defeated battle unit.
@Serializable
private data class CpuWanderBrainSnapshot(
    val homes: List<HomeSnapshot>,
    val returningBattleUnitIds: List<String>,
    val respawns: Map<String, Int>,
)

// Distances from home: [row, column, distance] for each tile, kept short as there is one per tile and home.
@Serializable
private data class HomeSnapshot(
    val battleUnitId: String,
    val row: Int,
    val column: Int,
    val distancesFromHome: List<List<Int>>,
)
