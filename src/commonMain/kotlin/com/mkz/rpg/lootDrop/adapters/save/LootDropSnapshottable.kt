package com.mkz.rpg.lootDrop.adapters.save

import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import kotlinx.serialization.Serializable

/**
 * The items lying on the battlefield, by the unit that dropped them. The loot tables of each unit are not saved, as they
 * are read from the resources again: restoring fails when a unit no longer drops loot.
 */
class LootDropSnapshottable(
    private val lootDropRepository: LootDropRepository,
) : Snapshottable {
    override val key = "lootDrop"

    override fun snapshot(): String =
        SnapshotJson.encodeToString(
            lootDropRepository.searchAll().map { lootDrop ->
                val dto = lootDrop.toDto()
                LootDropSnapshot(
                    unitId = dto.unitId,
                    droppedItems = dto.droppedItems.map { (position, lootableItemIds) -> DroppedItemsSnapshot(position.row, position.column, lootableItemIds) },
                )
            },
        )

    override fun restore(snapshot: String) {
        val lootDrops = SnapshotJson.decodeFromString<List<LootDropSnapshot>>(snapshot).associateBy(LootDropSnapshot::unitId)
        lootDrops.keys.forEach { unitId -> requireNotNull(lootDropRepository.searchByUnitId(unitId)) { "Unit $unitId drops no loot" } }
        lootDropRepository.searchAll().forEach { lootDrop ->
            val droppedItems =
                lootDrops[lootDrop.toDto().unitId]
                    ?.droppedItems
                    .orEmpty()
                    .associate { tile -> LootDrop.Dto.PositionDto(row = tile.row, column = tile.column) to tile.lootableItemIds }
            lootDropRepository.update(lootDrop.restoreDroppedItems(droppedItems))
        }
    }
}

@Serializable
private data class LootDropSnapshot(
    val unitId: String,
    val droppedItems: List<DroppedItemsSnapshot>,
)

// The items lying on a tile, in the order they were dropped.
@Serializable
private data class DroppedItemsSnapshot(
    val row: Int,
    val column: Int,
    val lootableItemIds: List<String>,
)
