package com.mkz.tessavar.npc.usecases.commands

import com.mkz.tessavar.battlefield.usecases.queries.IsTileVacant
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.npc.domain.Npc
import com.mkz.tessavar.npc.domain.NpcError.NpcStockItemNotFound
import com.mkz.tessavar.npc.domain.NpcError.NpcTileNotVacant
import com.mkz.tessavar.npc.domain.NpcRepository
import com.mkz.tessavar.npc.usecases.services.NpcLoader
import com.mkz.tessavar.shared.domain.DomainEvent
import com.mkz.tessavar.shared.domain.EventBus

class PlaceNpcs(
    private val npcLoader: NpcLoader,
    private val npcRepository: NpcRepository,
    private val isTileVacant: IsTileVacant,
    private val searchLootableItemById: SearchLootableItemById,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val events = mutableSetOf<DomainEvent>()
        npcLoader.loadNpcs().forEach { npcDto ->
            if (npcRepository.searchById(npcDto.id) != null) return@forEach
            if (!isTileVacant(npcDto.row, npcDto.column)) throw NpcTileNotVacant()
            if (npcDto.stock.any { lootableItemId -> searchLootableItemById(lootableItemId) == null }) throw NpcStockItemNotFound()
            val (npcEvents, npc) = Npc.place(npcDto).pullEvents()
            npcRepository.save(npc)
            events += npcEvents
        }
        eventBus.publish(events)
    }
}
