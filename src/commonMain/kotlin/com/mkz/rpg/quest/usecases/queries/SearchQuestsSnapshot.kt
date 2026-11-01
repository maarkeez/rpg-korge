package com.mkz.rpg.quest.usecases.queries

import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.QuestRepository

/** What is saved of the quests: where each one is in its life. The rest is read from the resources again. */
class SearchQuestsSnapshot(
    private val questRepository: QuestRepository,
) {
    operator fun invoke(): List<Quest.Dto.ProgressDto> = questRepository.searchAll().map { quest -> quest.toDto().progress }
}
