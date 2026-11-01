package com.mkz.rpg.quest.adapters.presentation

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.quest.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.quest.adapters.events.OnBattleUnitMoved
import com.mkz.rpg.quest.adapters.events.OnNpcTalkFinished
import com.mkz.rpg.quest.adapters.events.OnQuestCompleted
import com.mkz.rpg.quest.adapters.events.OnRequestInitialiseQuests
import com.mkz.rpg.quest.adapters.events.OnRequestStartMainQuest
import com.mkz.rpg.quest.adapters.save.QuestSnapshottable
import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.commands.AbandonQuest
import com.mkz.rpg.quest.usecases.commands.InitialiseQuests
import com.mkz.rpg.quest.usecases.commands.PinQuest
import com.mkz.rpg.quest.usecases.commands.ProgressDefeatTasks
import com.mkz.rpg.quest.usecases.commands.ProgressReachTasks
import com.mkz.rpg.quest.usecases.commands.RequestQuestReward
import com.mkz.rpg.quest.usecases.commands.ResolveNpcTalk
import com.mkz.rpg.quest.usecases.commands.RestoreQuests
import com.mkz.rpg.quest.usecases.commands.StartMainQuest
import com.mkz.rpg.quest.usecases.queries.SearchQuests
import com.mkz.rpg.quest.usecases.queries.SearchQuestsSnapshot
import com.mkz.rpg.quest.usecases.services.PublishQuestProgress
import com.mkz.rpg.quest.usecases.services.QuestLoader
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Snapshottable

class QuestApi(
    battlefieldApi: BattlefieldApi,
    partyApi: PartyApi,
    eventBus: EventBus,
    private val questLoader: QuestLoader,
) {
    // Storage
    private val questRepository: QuestRepository = InMemoryQuestRepository()

    // Save
    private val searchQuestsSnapshot = SearchQuestsSnapshot(questRepository)
    private val restoreQuests = RestoreQuests(questRepository)
    val snapshottable: Snapshottable = QuestSnapshottable(searchQuestsSnapshot, restoreQuests)

    // Services
    private val publishQuestProgress = PublishQuestProgress(questRepository, eventBus)

    // Commands
    val initialiseQuests = InitialiseQuests(questLoader, questRepository, battlefieldApi.canBattlefieldTileBeOccupied)
    val startMainQuest = StartMainQuest(questRepository, eventBus)
    val progressDefeatTasks = ProgressDefeatTasks(questRepository, partyApi.searchParty, publishQuestProgress)
    val progressReachTasks = ProgressReachTasks(questRepository, partyApi.searchParty, publishQuestProgress)
    val resolveNpcTalk = ResolveNpcTalk(questRepository, publishQuestProgress)
    val abandonQuest = AbandonQuest(questRepository, publishQuestProgress)
    val pinQuest = PinQuest(questRepository, publishQuestProgress, eventBus)
    val requestQuestReward = RequestQuestReward(questRepository, partyApi.searchParty, battlefieldApi.searchPosition, eventBus)

    // Queries
    val searchQuests = SearchQuests(questRepository)

    // Events
    private val onRequestInitialiseQuests = OnRequestInitialiseQuests(initialiseQuests, eventBus)
    private val onRequestStartMainQuest = OnRequestStartMainQuest(startMainQuest, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(progressDefeatTasks, eventBus)
    private val onBattleUnitMoved = OnBattleUnitMoved(progressReachTasks, eventBus)
    private val onNpcTalkFinished = OnNpcTalkFinished(resolveNpcTalk, eventBus)
    private val onQuestCompleted = OnQuestCompleted(requestQuestReward, eventBus)

    suspend fun init() {
        questLoader.initResources()
    }
}
