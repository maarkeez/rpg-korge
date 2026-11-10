package com.mkz.rpg.quest.usecases.queries

import com.mkz.rpg.quest.domain.Quest.Dto.QuestLineDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestLineDto.KindDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.QuestRepository

class SearchQuestLine(
    private val questRepository: QuestRepository,
) {
    private val kindsInOrder = listOf(setOf(KindDto.TALK_TASK), setOf(KindDto.START, KindDto.START_AGAIN), setOf(KindDto.REMINDER), setOf(KindDto.COMPLETED))

    operator fun invoke(npcId: String): QuestLineDto? {
        val lines =
            questRepository
                .searchAll()
                .sortedWith(compareBy({ quest -> quest.toDto().type != QuestTypeDto.MAIN }, { quest -> quest.toDto().id }))
                .mapNotNull { quest -> quest.lineFor(npcId) }
        return kindsInOrder.firstNotNullOfOrNull { kinds -> lines.firstOrNull { line -> line.kind in kinds } }
    }
}
