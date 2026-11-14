package com.mkz.tessavar.quest.usecases.queries

import com.mkz.tessavar.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.tessavar.quest.domain.Quest
import com.mkz.tessavar.quest.domain.Quest.Dto.QuestLineDto
import com.mkz.tessavar.quest.domain.Quest.Dto.QuestLineDto.KindDto
import com.mkz.tessavar.quest.domain.QuestMother.defeat
import com.mkz.tessavar.quest.domain.QuestMother.mainQuest
import com.mkz.tessavar.quest.domain.QuestMother.sideQuest
import com.mkz.tessavar.quest.domain.QuestMother.talk
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchQuestLineTest {
    private val questRepository = InMemoryQuestRepository()
    private val searchQuestLine = SearchQuestLine(questRepository)

    private fun Quest.started() = start().pullEvents().second

    private fun Quest.completed() = start().defeated("rat").pullEvents().second

    @Test
    fun `SearchQuestLine should be nothing when no quest has a line for the npc`() {
        // Given
        questRepository.save(sideQuest(id = "bats", questGiverNpcId = "farmer"))
        questRepository.save(mainQuest(id = "main", tasks = listOf(defeat(), talk("elder"))).started())
        // When
        val result = searchQuestLine("elder")
        // Then
        result shouldBe null
    }

    @Test
    fun `SearchQuestLine should prefer a talk task line when an npc also has a quest to start`() {
        // Given
        questRepository.save(sideQuest(id = "errand", questGiverNpcId = "elder"))
        questRepository.save(mainQuest(id = "main", tasks = listOf(talk("elder"), defeat())).started())
        // When
        val result = searchQuestLine("elder")
        // Then
        result shouldBe QuestLineDto(questId = "main", kind = KindDto.TALK_TASK, taskIndex = 0)
    }

    @Test
    fun `SearchQuestLine should prefer a start line when the npc also has a reminder line`() {
        // Given
        questRepository.save(sideQuest(id = "bats", questGiverNpcId = "farmer").started())
        questRepository.save(sideQuest(id = "cow", questGiverNpcId = "farmer"))
        // When
        val result = searchQuestLine("farmer")
        // Then
        result shouldBe QuestLineDto(questId = "cow", kind = KindDto.START, taskIndex = null)
    }

    @Test
    fun `SearchQuestLine should prefer a start again line when the npc also has a reminder line`() {
        // Given
        questRepository.save(sideQuest(id = "bats", questGiverNpcId = "elf").started())
        questRepository.save(sideQuest(id = "rats", questGiverNpcId = "elf", repeatable = true, tasks = listOf(defeat(unitId = "rat"))).completed())
        // When
        val result = searchQuestLine("elf")
        // Then
        result shouldBe QuestLineDto(questId = "rats", kind = KindDto.START_AGAIN, taskIndex = null)
    }

    @Test
    fun `SearchQuestLine should prefer a reminder line when the npc also has a completed line`() {
        // Given
        questRepository.save(sideQuest(id = "bats", questGiverNpcId = "farmer", tasks = listOf(defeat(unitId = "rat"))).completed())
        questRepository.save(sideQuest(id = "cow", questGiverNpcId = "farmer").started())
        // When
        val result = searchQuestLine("farmer")
        // Then
        result shouldBe QuestLineDto(questId = "cow", kind = KindDto.REMINDER, taskIndex = null)
    }

    @Test
    fun `SearchQuestLine should prefer the main quest when two quests ask to talk to the npc`() {
        // Given
        questRepository.save(sideQuest(id = "ask-the-elder", questGiverNpcId = "farmer", tasks = listOf(talk("elder"))).started())
        questRepository.save(
            mainQuest(id = "main", tasks = listOf(defeat(), talk("elder")))
                .start()
                .defeated("goat-wizard")
                .pullEvents()
                .second,
        )
        // When
        val result = searchQuestLine("elder")
        // Then
        result shouldBe QuestLineDto(questId = "main", kind = KindDto.TALK_TASK, taskIndex = 1)
    }

    @Test
    fun `SearchQuestLine should prefer the first quest by id when the npc has two quests to start`() {
        // Given
        questRepository.save(sideQuest(id = "cow", questGiverNpcId = "farmer"))
        questRepository.save(sideQuest(id = "bats", questGiverNpcId = "farmer"))
        // When
        val result = searchQuestLine("farmer")
        // Then
        result shouldBe QuestLineDto(questId = "bats", kind = KindDto.START, taskIndex = null)
    }
}
