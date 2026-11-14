package com.mkz.tessavar.quest.usecases.queries

import com.mkz.tessavar.quest.domain.Quest
import com.mkz.tessavar.quest.domain.QuestRepository

class SearchQuestsSnapshot(
    private val questRepository: QuestRepository,
) {
    operator fun invoke(): List<Quest.Dto.ProgressDto> = questRepository.searchAll().map { quest -> quest.toDto().progress }
}
