package com.mkz.tessavar.quest.usecases.queries

import com.mkz.tessavar.quest.domain.Quest
import com.mkz.tessavar.quest.domain.QuestRepository

class SearchQuests(
    private val questRepository: QuestRepository,
) {
    operator fun invoke(): List<Quest.Dto> = questRepository.searchAll().map(Quest::toDto)
}
