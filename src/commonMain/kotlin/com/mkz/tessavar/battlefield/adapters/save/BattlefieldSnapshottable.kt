package com.mkz.tessavar.battlefield.adapters.save

import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.battlefield.usecases.commands.LoadBattlefieldSnapshot
import com.mkz.tessavar.battlefield.usecases.queries.SearchBattlefieldSnapshot
import com.mkz.tessavar.shared.adapters.save.SnapshotJson
import com.mkz.tessavar.shared.domain.Snapshottable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

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
