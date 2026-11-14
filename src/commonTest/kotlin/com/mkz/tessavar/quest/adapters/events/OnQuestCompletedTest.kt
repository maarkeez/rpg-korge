package com.mkz.tessavar.quest.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.inventory.domain.InventoryEvent.RequestGrantReward
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.tessavar.quest.domain.QuestEvent.QuestCompleted
import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.quest.domain.QuestMother.reward
import com.mkz.tessavar.quest.usecases.commands.RequestQuestReward
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnQuestCompletedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val questRepository = InMemoryQuestRepository()
    private val onQuestCompleted = OnQuestCompleted(RequestQuestReward(questRepository, queries.searchParty, queries.searchPosition, eventBus), eventBus)

    @Test
    fun `should request the reward when a quest completes`() {
        // Given
        queries.battlefieldRepository.create(battlefield("A .", occupants = mapOf('A' to "knight-1")))
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        questRepository.save(mainQuest(id = "defeat-the-goat-wizard", reward = reward(gold = 150)))
        val requests = mutableListOf<RequestGrantReward>()
        eventBus.subscribe<RequestGrantReward> { request -> requests += request }
        eventBus.publish(QuestCompleted(questId = "defeat-the-goat-wizard"))
        // When
        eventBus.dispatch()
        // Then
        requests.shouldContainExactly(RequestGrantReward(battleUnitId = "knight-1", gold = 150, lootableItemIds = emptyList(), row = 0, column = 0))
    }
}
