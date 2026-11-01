package com.mkz.rpg.quest.usecases.services

import com.mkz.rpg.quest.domain.Quest

interface QuestLoader {
    suspend fun initResources() {}

    fun loadQuests(): List<Quest.Dto>
}
