package com.mkz.rpg.battlefield.adapters.save

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * The occupant of each tile: the battle units, the npcs and the buildings. The map itself is not saved, as it is read
 * from the resources again: restoring fails when a tile is no longer on it.
 */
class BattlefieldSnapshottable(
    private val battlefieldRepository: BattlefieldRepository,
) : Snapshottable {
    override val key = "battlefield"

    override fun snapshot(): String =
        SnapshotJson.encodeToString(
            battlefieldRepository
                .search()
                ?.occupants()
                .orEmpty()
                .map { occupant -> OccupantSnapshot(occupant.occupantId, occupant.row, occupant.column) },
        )

    override fun restore(snapshot: String) {
        val occupants = SnapshotJson.decodeFromString<List<OccupantSnapshot>>(snapshot).map { occupant -> Battlefield.Dto.OccupantDto(occupant.occupantId, occupant.row, occupant.column) }
        val battlefield = requireNotNull(battlefieldRepository.search()) { "The battlefield is not created yet" }
        battlefieldRepository.update(battlefield.restoreOccupants(occupants))
    }
}

@Serializable
private data class OccupantSnapshot(
    val occupantId: String,
    val row: Int,
    val column: Int,
)
