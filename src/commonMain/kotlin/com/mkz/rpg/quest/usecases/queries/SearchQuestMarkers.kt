package com.mkz.rpg.quest.usecases.queries

import com.mkz.rpg.quest.domain.Quest.Dto.QuestMarkerDto
import com.mkz.rpg.quest.domain.Quest.Dto.TaskDto
import com.mkz.rpg.quest.domain.QuestRepository

/** The sign each npc any quest names has right now: one per npc, the talk task winning, as the talk does both. */
class SearchQuestMarkers(
    private val questRepository: QuestRepository,
) {
    operator fun invoke(): List<QuestMarkerDto> {
        val quests = questRepository.searchAll()
        val npcIds =
            quests
                .map { quest -> quest.toDto() }
                .flatMap { quest -> listOfNotNull(quest.questGiverNpcId) + quest.tasks.filter { task -> task.type == TaskDto.TypeDto.TALK }.mapNotNull { task -> task.talk?.npcId } }
                .distinct()
        return npcIds.mapNotNull { npcId ->
            val markers = quests.mapNotNull { quest -> quest.markerFor(npcId) }
            markers.firstOrNull { marker -> marker.sign == QuestMarkerDto.SignDto.TALK_TASK } ?: markers.firstOrNull()
        }
    }
}
