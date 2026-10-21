package com.mkz.rpg.npc.usecases.commands

import com.mkz.rpg.npc.domain.NpcRepository
import com.mkz.rpg.party.domain.PartyEvent.RequestRecruitMember
import com.mkz.rpg.shared.domain.EventBus

/**
 * Recruits the npc for the party: it leaves its tile, and the party recruits a battle unit of its unit on it. The battle
 * unit takes the id of the npc, which is deleted, so it is only recruited once. Nothing happens when the npc does not
 * exist or is not recruitable.
 *
 * The events are dispatched in order: the tile is vacated (`RequestRemoveOccupant`, right after `NpcRecruited`) before
 * the party recruits the battle unit on it.
 */
class RecruitNpc(
    private val npcRepository: NpcRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(npcId: String) {
        val npc = npcRepository.searchById(npcId) ?: return
        if (!npc.isRecruitable()) return
        val npcDto = npc.toDto()
        val (events, _) = npc.recruit().pullEvents()
        npcRepository.delete(npcId)
        eventBus.publish(events)
        eventBus.publish(RequestRecruitMember(battleUnitId = npcId, unitId = npcDto.unitId!!, row = npcDto.row, column = npcDto.column))
    }
}
