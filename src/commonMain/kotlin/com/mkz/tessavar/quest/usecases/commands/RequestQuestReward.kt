package com.mkz.tessavar.quest.usecases.commands

import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.inventory.domain.InventoryEvent.RequestGrantReward
import com.mkz.tessavar.party.usecases.queries.SearchParty
import com.mkz.tessavar.quest.domain.QuestRepository
import com.mkz.tessavar.shared.domain.EventBus

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
