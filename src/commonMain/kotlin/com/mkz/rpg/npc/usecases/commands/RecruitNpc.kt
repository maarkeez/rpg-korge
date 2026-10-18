package com.mkz.rpg.npc.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestDeployBattleUnit
import com.mkz.rpg.exploration.domain.ExplorationEvent.RequestJoinParty
import com.mkz.rpg.npc.domain.NpcRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Recruits the npc for the player: it leaves its tile, and a battle unit of its unit is deployed on it and joins the
 * party. The battle unit takes the id of the npc, which is deleted, so it is only recruited once. Nothing happens when
 * the npc does not exist or is not recruitable.
 *
 * The events are dispatched in order: the tile is vacated (`NpcRecruited`) before the battle unit is deployed on it, and
 * the battle unit is deployed before it joins the party.
 */
class RecruitNpc(
    private val npcRepository: NpcRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        npcId: String,
        playerId: String,
    ) {
        val npc = npcRepository.searchById(npcId) ?: return
        if (!npc.isRecruitable()) return
        val npcDto = npc.toDto()
        val (events, _) = npc.recruit().pullEvents()
        npcRepository.delete(npcId)
        eventBus.publish(events)
        eventBus.publish(
            RequestDeployBattleUnit(
                battleUnitId = npcId,
                unitId = npcDto.unitId!!,
                playerId = playerId,
                row = npcDto.row,
                column = npcDto.column,
            ),
        )
        eventBus.publish(RequestJoinParty(battleUnitId = npcId))
    }
}
