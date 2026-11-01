package com.mkz.rpg.quest.usecases.queries

import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.QuestRepository

/** Every quest, with where it is in its life, in the order they were created. */
class SearchQuests(
    private val questRepository: QuestRepository,
) {
    operator fun invoke(): List<Quest.Dto> = questRepository.searchAll().map(Quest::toDto)
}
