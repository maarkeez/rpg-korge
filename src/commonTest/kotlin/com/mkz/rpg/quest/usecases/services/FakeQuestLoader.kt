package com.mkz.rpg.quest.usecases.services

import com.mkz.rpg.quest.domain.Quest

class FakeQuestLoader(
    private val quests: List<Quest.Dto> = emptyList(),
) : QuestLoader {
    override fun loadQuests(): List<Quest.Dto> = quests
}
