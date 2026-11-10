package com.mkz.rpg.quest.usecases.queries

import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.QuestMarkerDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestMarkerDto.SignDto
import com.mkz.rpg.quest.domain.QuestMother.defeat
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import com.mkz.rpg.quest.domain.QuestMother.talk
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class SearchQuestMarkersTest {
    private val questRepository = InMemoryQuestRepository()
    private val searchQuestMarkers = SearchQuestMarkers(questRepository)

    private fun Quest.started() = start().pullEvents().second

    @Test
    fun `SearchQuestMarkers should prefer a talk task when an npc has both`() {
        // Given
        questRepository.save(sideQuest(id = "errand", questGiverNpcId = "elder"))
        questRepository.save(mainQuest(id = "main", tasks = listOf(talk("elder"))).started())
        // When
        val result = searchQuestMarkers()
        // Then
        result.shouldContainExactly(QuestMarkerDto(npcId = "elder", sign = SignDto.TALK_TASK))
    }

    @Test
    fun `SearchQuestMarkers should list each npc with its sign when several have one`() {
        // Given
        questRepository.save(sideQuest(id = "bats", questGiverNpcId = "farmer"))
        questRepository.save(mainQuest(id = "main", tasks = listOf(talk("elder"))).started())
        // When
        val result = searchQuestMarkers()
        // Then
        result.shouldContainExactly(QuestMarkerDto(npcId = "farmer", sign = SignDto.QUEST_TO_START), QuestMarkerDto(npcId = "elder", sign = SignDto.TALK_TASK))
    }

    @Test
    fun `SearchQuestMarkers should list no npc when nothing asks for it`() {
        // Given
        questRepository.save(sideQuest(id = "bats", questGiverNpcId = "farmer").started())
        questRepository.save(mainQuest(id = "main", tasks = listOf(defeat(), talk("elder"))).started())
        // When
        val result = searchQuestMarkers()
        // Then
        result.shouldBeEmpty()
    }
}
