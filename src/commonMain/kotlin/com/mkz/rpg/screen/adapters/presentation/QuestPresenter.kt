package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.PartyEvent.ExplorationPaused
import com.mkz.rpg.party.domain.PartyEvent.ExplorationResumed
import com.mkz.rpg.quest.adapters.presentation.QuestApi
import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.QuestEvent.QuestCompleted
import com.mkz.rpg.quest.domain.QuestEvent.QuestStarted
import com.mkz.rpg.quest.domain.QuestEvent.TaskCompleted
import com.mkz.rpg.quest.domain.QuestEvent.TaskProgressed
import com.mkz.rpg.save.domain.SaveEvent.GameRestored
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe

/**
 * Keeps the main quest on the screen: the tracker always names its current task, popping when it changes, and stays ticked
 * once completed. An event naming any other quest is ignored. The tracker is read only in a battle, and hidden once a
 * player wins, as the winner is drawn across the whole top line.
 *
 * The detail view displays the quest in full, in place of the pad, while the party reads it: [ExplorationPresenter] opens
 * it when the tracker is tapped, and Back closes it.
 */
class QuestPresenter(
    private val questTrackerView: QuestTrackerView,
    private val questDetailView: QuestDetailView,
    private val questApi: QuestApi,
    private val partyApi: PartyApi,
    private val texts: Texts,
    private val timeline: BattleSceneTimeline,
    eventBus: EventBus,
) : QuestTrackerView.Delegate,
    QuestDetailView.Delegate {
    /** Whether the scene displays the detail view. */
    var isDetailOpen = false
        private set

    private var onTrackerSelected: (() -> Unit)? = null

    // What the tracker displays, or will once the timeline gets to it, so an event with nothing new draws nothing.
    private var displayedQuest: QuestInfo? = null

    private val subscriptions =
        listOf(
            eventBus.subscribe<QuestStarted> { event ->
                mainQuestNamed(event.questId)?.let(::displayTracker)
            },
            eventBus.subscribe<TaskProgressed> { event -> taskChanged(event.questId) },
            eventBus.subscribe<TaskCompleted> { event -> taskChanged(event.questId) },
            eventBus.subscribe<QuestCompleted> { event ->
                mainQuestNamed(event.questId)?.let(::displayTracker)
            },
            eventBus.subscribe<GameRestored> {
                mainQuest()?.let(::displayTracker)
            },
            eventBus.subscribe<ExplorationPaused> {
                timeline.play { questTrackerView.setTappable(false) }
            },
            eventBus.subscribe<ExplorationResumed> {
                timeline.play { questTrackerView.setTappable(true) }
            },
            eventBus.subscribe<PlayerVictory> {
                timeline.play {
                    questTrackerView.hide()
                    closeDetail()
                }
            },
        )

    init {
        questDetailView.hide()
        questTrackerView.setDelegate(this)
        questDetailView.setDelegate(this)
    }

    /** Hands the taps on the tracker over, e.g.: to open the detail view when it can take the place of the pad. */
    fun setOnTrackerSelected(listener: () -> Unit) {
        onTrackerSelected = listener
    }

    /** Displays the quest in full, read now, as time stops while it is open. To be played in the timeline. */
    fun openDetail() {
        val quest = mainQuest()?.let { mainQuest -> QuestMapper.questInfo(mainQuest, texts) } ?: return
        isDetailOpen = true
        questDetailView.display(quest)
    }

    /** Hides the detail view. Nothing happens when it is not open. */
    fun closeDetail() {
        if (!isDetailOpen) return
        isDetailOpen = false
        questDetailView.hide()
    }

    override fun questTrackerSelected() {
        onTrackerSelected?.invoke()
    }

    // The player can only act once the scene shows the detail view.
    override fun questDetailBackSelected() {
        if (isDetailOpen && !timeline.isPlaying) partyApi.closeQuest()
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    // A last defeat publishes three events: nothing draws while the quest is completed, as QuestCompleted follows. And a
    // defeat completing a task publishes two, both reading the next task, so only the first one finds something new.
    private fun taskChanged(questId: String) {
        val quest = mainQuestNamed(questId)?.let { mainQuest -> QuestMapper.questInfo(mainQuest, texts) } ?: return
        if (quest.isCompleted || quest == displayedQuest) return
        displayedQuest = quest
        timeline.play {
            questTrackerView.display(quest)
            timeline.playAlongside { questTrackerView.pop() }
        }
    }

    private fun displayTracker(mainQuest: Quest.Dto) {
        val quest = QuestMapper.questInfo(mainQuest, texts)
        displayedQuest = quest
        timeline.play { questTrackerView.display(quest) }
    }

    private fun mainQuestNamed(questId: String): Quest.Dto? = mainQuest()?.takeIf { quest -> quest.id == questId }

    private fun mainQuest(): Quest.Dto? = questApi.searchQuests().firstOrNull { quest -> quest.type == QuestTypeDto.MAIN }
}
