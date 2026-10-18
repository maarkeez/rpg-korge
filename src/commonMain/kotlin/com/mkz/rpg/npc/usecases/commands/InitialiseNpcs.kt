package com.mkz.rpg.npc.usecases.commands

import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.domain.NpcError.NpcTileCanNotBeOccupied
import com.mkz.rpg.npc.domain.NpcEvent
import com.mkz.rpg.npc.domain.NpcRepository
import com.mkz.rpg.npc.usecases.services.NpcLoader
import com.mkz.rpg.shared.domain.EventBus

/**
 * Places every loaded npc on its tile, unless it is already placed, failing when its tile can not be occupied (e.g.:
 * void, out of the battlefield). The battlefield occupies the tile once `NpcPlaced` is handled.
 */
class InitialiseNpcs(
    private val npcLoader: NpcLoader,
    private val npcRepository: NpcRepository,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val events = mutableSetOf<NpcEvent>()
        npcLoader.loadNpcs().forEach { npcDto ->
            if (npcRepository.searchById(npcDto.id) != null) return@forEach
            if (!canBattlefieldTileBeOccupied(npcDto.row, npcDto.column)) throw NpcTileCanNotBeOccupied()
            val (npcEvents, npc) = Npc.place(npcDto).pullEvents()
            npcRepository.save(npc)
            events += npcEvents
        }
        eventBus.publish(events)
    }
}
