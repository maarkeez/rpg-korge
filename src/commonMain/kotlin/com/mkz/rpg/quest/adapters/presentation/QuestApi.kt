package com.mkz.rpg.quest.adapters.presentation

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.quest.adapters.events.OnRequestInitialiseQuests
import com.mkz.rpg.quest.adapters.events.OnRequestStartMainQuest
import com.mkz.rpg.quest.adapters.save.QuestSnapshottable
import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.commands.InitialiseQuests
import com.mkz.rpg.quest.usecases.commands.RestoreQuests
import com.mkz.rpg.quest.usecases.commands.StartMainQuest
import com.mkz.rpg.quest.usecases.queries.SearchQuests
import com.mkz.rpg.quest.usecases.queries.SearchQuestsSnapshot
import com.mkz.rpg.quest.usecases.services.QuestLoader
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Snapshottable

class QuestApi(
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
    private val questLoader: QuestLoader,
) {
    // Storage
    private val questRepository: QuestRepository = InMemoryQuestRepository()

    // Save
    private val searchQuestsSnapshot = SearchQuestsSnapshot(questRepository)
    private val restoreQuests = RestoreQuests(questRepository)
    val snapshottable: Snapshottable = QuestSnapshottable(searchQuestsSnapshot, restoreQuests)

    // Commands
    val initialiseQuests = InitialiseQuests(questLoader, questRepository, battlefieldApi.canBattlefieldTileBeOccupied)
    val startMainQuest = StartMainQuest(questRepository, eventBus)

    // Queries
    val searchQuests = SearchQuests(questRepository)

    // Events
    private val onRequestInitialiseQuests = OnRequestInitialiseQuests(initialiseQuests, eventBus)
    private val onRequestStartMainQuest = OnRequestStartMainQuest(startMainQuest, eventBus)

    suspend fun init() {
        questLoader.initResources()
    }
}
