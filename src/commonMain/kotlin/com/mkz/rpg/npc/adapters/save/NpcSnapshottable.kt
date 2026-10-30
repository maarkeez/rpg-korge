package com.mkz.rpg.npc.adapters.save

import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.domain.NpcRepository
import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * The npcs left on the battlefield, with their tiles and stocks: a recruited npc is gone, as it joined the party.
 * Restoring fails when a stock holds a lootable item the game no longer has.
 */
class NpcSnapshottable(
    private val npcRepository: NpcRepository,
    private val searchLootableItemById: SearchLootableItemById,
) : Snapshottable {
    override val key = "npc"

    override fun snapshot(): String =
        SnapshotJson.encodeToString(
            npcRepository.searchAll().map { npc ->
                val dto = npc.toDto()
                NpcSnapshot(id = dto.id, row = dto.row, column = dto.column, unitId = dto.unitId, stock = dto.stock)
            },
        )

    override fun restore(snapshot: String) {
        val npcs = SnapshotJson.decodeFromString<List<NpcSnapshot>>(snapshot).map { npc -> Npc.Dto(id = npc.id, row = npc.row, column = npc.column, unitId = npc.unitId, stock = npc.stock) }
        npcs.flatMap(Npc.Dto::stock).forEach { lootableItemId -> requireNotNull(searchLootableItemById(lootableItemId)) { "Unknown lootable item $lootableItemId" } }
        npcRepository.searchAll().forEach { npc -> npcRepository.delete(npc.toDto().id) }
        npcs.forEach { npc -> npcRepository.save(Npc.restore(npc)) }
    }
}

@Serializable
private data class NpcSnapshot(
    val id: String,
    val row: Int,
    val column: Int,
    val unitId: String?,
    val stock: List<String>,
)
