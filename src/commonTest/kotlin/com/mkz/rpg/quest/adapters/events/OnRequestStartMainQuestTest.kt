package com.mkz.rpg.quest.adapters.events

import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestEvent.RequestStartMainQuest
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.usecases.commands.StartMainQuest
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestStartMainQuestTest {
    private val eventBus = InMemoryEventBus()
    private val questRepository = InMemoryQuestRepository()
    private val onRequestStartMainQuest = OnRequestStartMainQuest(StartMainQuest(questRepository, eventBus), eventBus)

    @Test
    fun `should start the main quest when the request is dispatched`() {
        // Given
        questRepository.save(mainQuest(id = "defeat-the-goat-wizard"))
        eventBus.publish(RequestStartMainQuest)
        // When
        eventBus.dispatch()
        // Then
        questRepository
            .searchById("defeat-the-goat-wizard")!!
            .toDto()
            .progress.status shouldBe StatusDto.ACTIVE
    }
}
