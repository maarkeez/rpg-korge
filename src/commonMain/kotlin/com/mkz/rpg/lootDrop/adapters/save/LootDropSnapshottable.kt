package com.mkz.rpg.lootDrop.adapters.save

import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.usecases.commands.LoadLootDropsSnapshot
import com.mkz.rpg.lootDrop.usecases.queries.SearchLootDropsSnapshot
import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class LootDropSnapshottable(
    private val searchLootDropsSnapshot: SearchLootDropsSnapshot,
    private val loadLootDropsSnapshot: LoadLootDropsSnapshot,
) : Snapshottable {
    override val key = "lootDrop"

    override fun snapshot(): String =
        SnapshotJson.encodeToString(
            searchLootDropsSnapshot().map { lootDrop ->
                LootDropSnapshot(
                    unitId = lootDrop.unitId,
                    droppedItems = lootDrop.droppedItems.map { (position, lootableItemIds) -> DroppedItemsSnapshot(position.row, position.column, lootableItemIds) },
                )
            },
        )

    override fun load(snapshot: String) =
        loadLootDropsSnapshot(
            SnapshotJson.decodeFromString<List<LootDropSnapshot>>(snapshot).associate { lootDrop ->
                lootDrop.unitId to lootDrop.droppedItems.associate { tile -> LootDrop.Dto.PositionDto(row = tile.row, column = tile.column) to tile.lootableItemIds }
            },
        )
}

@Serializable
private data class LootDropSnapshot(
    val unitId: String,
    val droppedItems: List<DroppedItemsSnapshot>,
)

@Serializable
private data class DroppedItemsSnapshot(
    val row: Int,
    val column: Int,
    val lootableItemIds: List<String>,
)
