package com.mkz.tessavar.quest.usecases.commands

import com.mkz.tessavar.quest.domain.Quest.Dto.QuestLineDto.KindDto
import com.mkz.tessavar.quest.domain.QuestRepository
import com.mkz.tessavar.quest.usecases.queries.SearchQuestLine
import com.mkz.tessavar.quest.usecases.services.PublishQuestProgress

class ResolveNpcTalk(
    private val searchQuestLine: SearchQuestLine,
    private val questRepository: QuestRepository,
    private val publishQuestProgress: PublishQuestProgress,
) {
    operator fun invoke(npcId: String) {
        val line = searchQuestLine(npcId) ?: return
        val quest = questRepository.searchById(line.questId) ?: return
        when (line.kind) {
            KindDto.TALK_TASK -> publishQuestProgress(quest.talkedTo(npcId))
            KindDto.START, KindDto.START_AGAIN -> publishQuestProgress(quest.start())
            KindDto.REMINDER, KindDto.COMPLETED -> Unit
        }
    }
}
