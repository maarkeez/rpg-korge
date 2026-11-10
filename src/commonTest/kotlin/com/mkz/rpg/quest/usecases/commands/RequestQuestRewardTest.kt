package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.inventory.domain.InventoryEvent.RequestGrantReward
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.reward
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import kotlin.test.BeforeTest
import kotlin.test.Test

class RequestQuestRewardTest {
    private val queries = InMemoryQueries()
    private val questRepository = InMemoryQuestRepository()
    private val eventBus = FakeEventBus()
    private val requestQuestReward = RequestQuestReward(questRepository, queries.searchParty, queries.searchPosition, eventBus)

    @BeforeTest
    fun setUp() {
        queries.battlefieldRepository.create(battlefield("A B", occupants = mapOf('A' to "knight-1", 'B' to "knight-2")))
        questRepository.save(mainQuest(id = "defeat-the-goat-wizard", reward = reward(gold = 150, lootableItemIds = listOf("hammer"))))
    }

    @Test
    fun `RequestQuestReward should request the reward for the leader on its tile when a quest completes`() {
        // Given
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")))
        // When
        requestQuestReward(questId = "defeat-the-goat-wizard")
        // Then
        eventBus.shouldHavePublishedEvents(RequestGrantReward(battleUnitId = "knight-1", gold = 150, lootableItemIds = listOf("hammer"), row = 0, column = 0))
    }

    @Test
    fun `RequestQuestReward should request the reward for the next member when the leader was defeated`() {
        // Given
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")).leave("knight-1"))
        // When
        requestQuestReward(questId = "defeat-the-goat-wizard")
        // Then
        eventBus.shouldHavePublishedEvents(RequestGrantReward(battleUnitId = "knight-2", gold = 150, lootableItemIds = listOf("hammer"), row = 0, column = 1))
    }

    @Test
    fun `RequestQuestReward should request no reward when the whole party is gone`() {
        // Given
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1")).leave("knight-1"))
        // When
        requestQuestReward(questId = "defeat-the-goat-wizard")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `RequestQuestReward should request no reward when there is no party`() {
        // When
        requestQuestReward(questId = "defeat-the-goat-wizard")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
