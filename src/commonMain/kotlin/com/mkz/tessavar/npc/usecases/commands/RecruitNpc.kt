package com.mkz.tessavar.npc.usecases.commands

import com.mkz.tessavar.npc.domain.NpcRepository
import com.mkz.tessavar.party.domain.PartyEvent.RequestRecruitMember
import com.mkz.tessavar.shared.domain.EventBus

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
