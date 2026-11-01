package com.mkz.rpg.quest.domain

interface QuestRepository {
    fun save(quest: Quest)

    fun searchById(id: String): Quest?

    /** Every quest, in the order they were created. */
    fun searchAll(): List<Quest>
}
