package com.mkz.rpg.npc.usecases.commands

import com.mkz.rpg.npc.domain.NpcRepository
import com.mkz.rpg.party.domain.PartyEvent.RequestRecruitMember
import com.mkz.rpg.shared.domain.EventBus

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
