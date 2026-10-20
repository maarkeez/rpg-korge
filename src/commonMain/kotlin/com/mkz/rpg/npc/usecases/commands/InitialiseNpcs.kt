package com.mkz.rpg.npc.usecases.commands

import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.domain.NpcError.NpcStockItemNotFound
import com.mkz.rpg.npc.domain.NpcError.NpcTileCanNotBeOccupied
import com.mkz.rpg.npc.domain.NpcError.NpcUnitNotFound
import com.mkz.rpg.npc.domain.NpcEvent
import com.mkz.rpg.npc.domain.NpcRepository
import com.mkz.rpg.npc.usecases.services.NpcLoader
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.usecases.queries.SearchUnitById

/**
 * Places every loaded npc on its tile, unless it is already placed, failing when its tile can not be occupied (e.g.:
 * void, out of the battlefield), when the unit of a recruitable npc does not exist, or when an item sold by a merchant
 * does not exist. The battlefield occupies the tile once `NpcPlaced` is handled.
 */
class InitialiseNpcs(
    private val npcLoader: NpcLoader,
    private val npcRepository: NpcRepository,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
    private val searchUnitById: SearchUnitById,
    private val searchLootableItemById: SearchLootableItemById,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val events = mutableSetOf<NpcEvent>()
        npcLoader.loadNpcs().forEach { npcDto ->
            if (npcRepository.searchById(npcDto.id) != null) return@forEach
            if (!canBattlefieldTileBeOccupied(npcDto.row, npcDto.column)) throw NpcTileCanNotBeOccupied()
            if (npcDto.unitId != null && searchUnitById(npcDto.unitId) == null) throw NpcUnitNotFound()
            if (npcDto.stock.any { lootableItemId -> searchLootableItemById(lootableItemId) == null }) throw NpcStockItemNotFound()
            val (npcEvents, npc) = Npc.place(npcDto).pullEvents()
            npcRepository.save(npc)
            events += npcEvents
        }
        eventBus.publish(events)
    }
}
