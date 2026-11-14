package com.mkz.tessavar.quest.usecases.queries

import com.mkz.tessavar.quest.domain.Quest.Dto.QuestMarkerDto
import com.mkz.tessavar.quest.domain.Quest.Dto.TaskDto
import com.mkz.tessavar.quest.domain.QuestRepository

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
