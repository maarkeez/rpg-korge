package com.mkz.rpg.quest.adapters.storage

import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.QuestRepository

class InMemoryQuestRepository : QuestRepository {
    private val quests = mutableMapOf<String, Quest>()

    override fun save(quest: Quest) {
        quests[quest.toDto().id] = quest
    }

    override fun searchById(id: String) = quests[id]

    override fun searchAll() = quests.values.toList()
}
