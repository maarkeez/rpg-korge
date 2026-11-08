package com.mkz.rpg.quest.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.QuestEvent.RequestCreateQuests
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.usecases.commands.CreateQuests
import com.mkz.rpg.quest.usecases.services.FakeQuestLoader
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestCreateQuestsTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val questRepository = InMemoryQuestRepository()
    private val main = mainQuest(id = "defeat-the-goat-wizard").toDto()
    private val onRequestCreateQuests =
        OnRequestCreateQuests(CreateQuests(FakeQuestLoader(listOf(main)), questRepository, queries.isTileVacant), eventBus)

    @Test
    fun `should create the quests when the request is dispatched`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". ."))
        eventBus.publish(RequestCreateQuests)
        // When
        eventBus.dispatch()
        // Then
        questRepository.searchById("defeat-the-goat-wizard")?.toDto() shouldBe main
    }
}
