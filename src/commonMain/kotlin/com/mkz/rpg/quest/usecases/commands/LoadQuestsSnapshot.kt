package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.QuestRepository

/** Puts back how far each quest went when a save took it, publishing no events. Fails when it names a quest, or a task, the game no longer has. */
class LoadQuestsSnapshot(
    private val questRepository: QuestRepository,
) {
    operator fun invoke(progress: List<Quest.Dto.ProgressDto>) {
        progress
            .map { questProgress -> requireNotNull(questRepository.searchById(questProgress.questId)) { "Unknown quest ${questProgress.questId}" }.loadProgress(questProgress) }
            .forEach(questRepository::save)
    }
}
