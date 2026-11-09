package com.mkz.rpg.battlefield.adapters.save

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.usecases.commands.LoadBattlefieldSnapshot
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefieldSnapshot
import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/** The occupant of each tile: the battle units, the npcs and the buildings, see [SearchBattlefieldSnapshot] and [LoadBattlefieldSnapshot]. */
class BattlefieldSnapshottable(
    private val searchBattlefieldSnapshot: SearchBattlefieldSnapshot,
    private val loadBattlefieldSnapshot: LoadBattlefieldSnapshot,
) : Snapshottable {
    override val key = "battlefield"

    override fun snapshot(): String = SnapshotJson.encodeToString(searchBattlefieldSnapshot().map { occupant -> OccupantSnapshot(occupant.occupantId, occupant.row, occupant.column) })

    override fun load(snapshot: String) =
        loadBattlefieldSnapshot(SnapshotJson.decodeFromString<List<OccupantSnapshot>>(snapshot).map { occupant -> Battlefield.Dto.OccupantDto(occupant.occupantId, occupant.row, occupant.column) })
}

@Serializable
private data class OccupantSnapshot(
    val occupantId: String,
    val row: Int,
    val column: Int,
)
