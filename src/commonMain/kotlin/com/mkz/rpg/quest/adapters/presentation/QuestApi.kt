package com.mkz.rpg.quest.adapters.presentation

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.quest.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.quest.adapters.events.OnBattleUnitMoved
import com.mkz.rpg.quest.adapters.events.OnNpcTalkFinished
import com.mkz.rpg.quest.adapters.events.OnQuestCompleted
import com.mkz.rpg.quest.adapters.events.OnRequestCreateQuests
import com.mkz.rpg.quest.adapters.save.QuestSnapshottable
import com.mkz.rpg.quest.adapters.storage.InMemoryQuestRepository
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.commands.AbandonQuest
import com.mkz.rpg.quest.usecases.commands.CreateQuests
import com.mkz.rpg.quest.usecases.commands.LoadQuestsSnapshot
import com.mkz.rpg.quest.usecases.commands.PinQuest
import com.mkz.rpg.quest.usecases.commands.ProgressDefeatTasks
import com.mkz.rpg.quest.usecases.commands.ProgressVisitTasks
import com.mkz.rpg.quest.usecases.commands.RequestQuestReward
import com.mkz.rpg.quest.usecases.commands.ResolveNpcTalk
import com.mkz.rpg.quest.usecases.commands.StartMainQuest
import com.mkz.rpg.quest.usecases.queries.SearchQuestLine
import com.mkz.rpg.quest.usecases.queries.SearchQuestMarkers
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
    private val questRepository: QuestRepository = InMemoryQuestRepository()

    private val searchQuestsSnapshot = SearchQuestsSnapshot(questRepository)
    private val loadQuestsSnapshot = LoadQuestsSnapshot(questRepository)
    val snapshottable: Snapshottable = QuestSnapshottable(searchQuestsSnapshot, loadQuestsSnapshot)

    private val publishQuestProgress = PublishQuestProgress(questRepository, eventBus)

    val searchQuests = SearchQuests(questRepository)
    val searchQuestMarkers = SearchQuestMarkers(questRepository)
    val searchQuestLine = SearchQuestLine(questRepository)

    val createQuests = CreateQuests(questLoader, questRepository, battlefieldApi.isTileVacant)
    val startMainQuest = StartMainQuest(questRepository, partyApi.searchParty, publishQuestProgress)
    val progressDefeatTasks = ProgressDefeatTasks(questRepository, partyApi.searchParty, publishQuestProgress)
    val progressVisitTasks = ProgressVisitTasks(questRepository, partyApi.searchParty, publishQuestProgress)
    val resolveNpcTalk = ResolveNpcTalk(searchQuestLine, questRepository, publishQuestProgress)
    val abandonQuest = AbandonQuest(questRepository, publishQuestProgress)
    val pinQuest = PinQuest(questRepository, publishQuestProgress, eventBus)
    val requestQuestReward = RequestQuestReward(questRepository, partyApi.searchParty, battlefieldApi.searchPosition, eventBus)

    private val onRequestCreateQuests = OnRequestCreateQuests(createQuests, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(progressDefeatTasks, eventBus)
    private val onBattleUnitMoved = OnBattleUnitMoved(startMainQuest, progressVisitTasks, eventBus)
    private val onNpcTalkFinished = OnNpcTalkFinished(resolveNpcTalk, eventBus)
    private val onQuestCompleted = OnQuestCompleted(requestQuestReward, eventBus)

    suspend fun init() {
        questLoader.initResources()
    }
}
