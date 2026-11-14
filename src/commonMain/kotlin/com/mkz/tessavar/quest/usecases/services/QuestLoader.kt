package com.mkz.tessavar.quest.usecases.services

import com.mkz.tessavar.quest.domain.Quest

interface QuestLoader {
    suspend fun initResources() {}

    fun loadQuests(): List<Quest.Dto>
}
