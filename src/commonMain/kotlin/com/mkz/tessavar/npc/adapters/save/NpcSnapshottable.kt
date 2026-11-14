package com.mkz.tessavar.npc.adapters.save

import com.mkz.tessavar.npc.domain.Npc
import com.mkz.tessavar.npc.usecases.commands.LoadNpcsSnapshot
import com.mkz.tessavar.npc.usecases.queries.SearchNpcsSnapshot
import com.mkz.tessavar.shared.adapters.save.SnapshotJson
import com.mkz.tessavar.shared.domain.Snapshottable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class NpcSnapshottable(
    private val searchNpcsSnapshot: SearchNpcsSnapshot,
    private val loadNpcsSnapshot: LoadNpcsSnapshot,
) : Snapshottable {
    override val key = "npc"

    override fun snapshot(): String =
        SnapshotJson.encodeToString(searchNpcsSnapshot().map { npc -> NpcSnapshot(id = npc.id, row = npc.row, column = npc.column, unitId = npc.unitId, stock = npc.stock) })

    override fun load(snapshot: String) =
        loadNpcsSnapshot(SnapshotJson.decodeFromString<List<NpcSnapshot>>(snapshot).map { npc -> Npc.Dto(id = npc.id, row = npc.row, column = npc.column, unitId = npc.unitId, stock = npc.stock) })
}

@Serializable
private data class NpcSnapshot(
    val id: String,
    val row: Int,
    val column: Int,
    val unitId: String?,
    val stock: List<String>,
)
