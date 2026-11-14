package com.mkz.tessavar.quest.domain

interface QuestRepository {
    fun save(quest: Quest)

    fun searchById(id: String): Quest?

    fun searchAll(): List<Quest>
}
