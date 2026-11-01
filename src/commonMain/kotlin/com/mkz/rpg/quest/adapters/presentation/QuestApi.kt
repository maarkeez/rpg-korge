package com.mkz.rpg.quest.adapters.presentation

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.quest.adapters.events.OnRequestInitialiseQuests
import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.commands.InitialiseQuests
import com.mkz.rpg.quest.usecases.queries.SearchQuests
import com.mkz.rpg.quest.usecases.services.QuestLoader
import com.mkz.rpg.shared.domain.EventBus

class QuestApi(
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
    private val questLoader: QuestLoader,
) {
    // Storage
    private val questRepository: QuestRepository = InMemoryQuestRepository()

    // Commands
    val initialiseQuests = InitialiseQuests(questLoader, questRepository, battlefieldApi.canBattlefieldTileBeOccupied)

    // Queries
    val searchQuests = SearchQuests(questRepository)

    // Events
    private val onRequestInitialiseQuests = OnRequestInitialiseQuests(initialiseQuests, eventBus)

    suspend fun init() {
        questLoader.initResources()
    }
}
