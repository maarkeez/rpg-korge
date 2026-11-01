package com.mkz.rpg.quest.usecases.queries

import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.QuestMarkerDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestMarkerDto.SignDto
import com.mkz.rpg.quest.domain.QuestMother.defeatUnits
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.quest.domain.QuestMother.sideQuest
import com.mkz.rpg.quest.domain.QuestMother.talkToNpc
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class SearchQuestMarkersTest {
    private val questRepository = InMemoryQuestRepository()
    private val searchQuestMarkers = SearchQuestMarkers(questRepository)

    private fun Quest.started() = start().pullEvents().second

    @Test
    fun `SearchQuestMarkers should prefer a talk task when an npc has both`() {
        // Given: the elder gives a quest, and the main quest asks to talk to it.
        questRepository.save(sideQuest(id = "errand", giverNpcId = "elder"))
        questRepository.save(mainQuest(id = "main", tasks = listOf(talkToNpc("elder"))).started())
        // When
        val result = searchQuestMarkers()
        // Then
        result.shouldContainExactly(QuestMarkerDto(npcId = "elder", sign = SignDto.TALK_TASK))
    }

    @Test
    fun `SearchQuestMarkers should list each npc with its sign when several have one`() {
        // Given
        questRepository.save(sideQuest(id = "bats", giverNpcId = "farmer"))
        questRepository.save(mainQuest(id = "main", tasks = listOf(talkToNpc("elder"))).started())
        // When
        val result = searchQuestMarkers()
        // Then
        result.shouldContainExactly(QuestMarkerDto(npcId = "farmer", sign = SignDto.QUEST_TO_START), QuestMarkerDto(npcId = "elder", sign = SignDto.TALK_TASK))
    }

    @Test
    fun `SearchQuestMarkers should list no npc when nothing asks for it`() {
        // Given: the farmer's quest is under way, and the talk to the elder is not the current task yet.
        questRepository.save(sideQuest(id = "bats", giverNpcId = "farmer").started())
        questRepository.save(mainQuest(id = "main", tasks = listOf(defeatUnits(), talkToNpc("elder"))).started())
        // When
        val result = searchQuestMarkers()
        // Then
        result.shouldBeEmpty()
    }
}
