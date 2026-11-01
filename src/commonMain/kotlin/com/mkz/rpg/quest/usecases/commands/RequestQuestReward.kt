package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.inventory.domain.InventoryEvent.RequestGrantReward
import com.mkz.rpg.party.usecases.queries.SearchParty
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Requests the reward of a completed quest for whoever leads, on the tile it stands on, which is why it runs on `QuestCompleted`: it is handled
 * after the requests already waiting on the bus, so a leader that just moved is on its new tile, and a leader just defeated has left the party.
 * With no leader, nobody receives it.
 */
class RequestQuestReward(
    private val questRepository: QuestRepository,
    private val searchParty: SearchParty,
    private val searchPosition: SearchPosition,
    private val eventBus: EventBus,
) {
    operator fun invoke(questId: String) {
        val reward = questRepository.searchById(questId)?.toDto()?.reward ?: return
        val leaderBattleUnitId = searchParty()?.leaderBattleUnitId ?: return
        val position = searchPosition(leaderBattleUnitId) ?: return
        eventBus.publish(
            RequestGrantReward(
                battleUnitId = leaderBattleUnitId,
                gold = reward.gold,
                lootableItemIds = reward.lootableItemIds,
                row = position.row,
                column = position.column,
            ),
        )
    }
}
