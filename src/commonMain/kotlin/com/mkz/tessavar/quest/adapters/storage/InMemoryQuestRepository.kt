package com.mkz.tessavar.quest.adapters.storage

import com.mkz.tessavar.quest.domain.Quest
import com.mkz.tessavar.quest.domain.QuestRepository

class InMemoryQuestRepository : QuestRepository {
    private val quests = mutableMapOf<String, Quest>()

    override fun save(quest: Quest) {
        quests[quest.toDto().id] = quest
    }

    override fun searchById(id: String) = quests[id]

    override fun searchAll() = quests.values.toList()
}
