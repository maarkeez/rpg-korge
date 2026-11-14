package com.mkz.tessavar.quest.usecases.services

import com.mkz.tessavar.quest.domain.Quest

class FakeQuestLoader(
    private val quests: List<Quest.Dto> = emptyList(),
) : QuestLoader {
    override fun loadQuests(): List<Quest.Dto> = quests
}
