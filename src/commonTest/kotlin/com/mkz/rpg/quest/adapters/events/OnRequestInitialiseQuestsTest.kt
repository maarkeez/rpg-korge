package com.mkz.rpg.quest.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.QuestEvent.RequestInitialiseQuests
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.usecases.commands.InitialiseQuests
import com.mkz.rpg.quest.usecases.services.FakeQuestLoader
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestInitialiseQuestsTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val questRepository = InMemoryQuestRepository()
    private val main = mainQuest(id = "defeat-the-goat-wizard").toDto()
    private val onRequestInitialiseQuests =
        OnRequestInitialiseQuests(InitialiseQuests(FakeQuestLoader(listOf(main)), questRepository, queries.canBattlefieldTileBeOccupied), eventBus)

    @Test
    fun `should initialise the quests when the request is dispatched`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". ."))
        eventBus.publish(RequestInitialiseQuests)
        // When
        eventBus.dispatch()
        // Then
        questRepository.searchById("defeat-the-goat-wizard")?.toDto() shouldBe main
    }
}
