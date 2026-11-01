package com.mkz.rpg.quest.usecases.services

import com.mkz.rpg.quest.domain.Quest

// Loads the given quests, instead of reading them from the resources.
class FakeQuestLoader(
    private val quests: List<Quest.Dto> = emptyList(),
) : QuestLoader {
    override fun loadQuests(): List<Quest.Dto> = quests
}
