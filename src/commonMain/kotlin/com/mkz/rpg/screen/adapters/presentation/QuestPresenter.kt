package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.PartyEvent.ExplorationPaused
import com.mkz.rpg.party.domain.PartyEvent.ExplorationResumed
import com.mkz.rpg.quest.adapters.presentation.QuestApi
import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestEvent.QuestCompleted
import com.mkz.rpg.quest.domain.QuestEvent.QuestStarted
import com.mkz.rpg.quest.domain.QuestEvent.TaskCompleted
import com.mkz.rpg.quest.domain.QuestEvent.TaskProgressed
import com.mkz.rpg.save.domain.SaveEvent.GameRestored
import com.mkz.rpg.screen.adapters.presentation.QuestAnnouncementView.Moment
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe

/**
 * Keeps the quests on the screen: the tracker always names the current task of the main quest, popping when it changes, and
 * stays ticked once completed, and a toast announces any quest starting or completing, one after the other, alongside the
 * timeline, so it holds nothing. The tracker is read only in a battle, and hidden once a player wins, as the winner is
 * drawn across the whole top line.
 *
 * While the party reads the quests, in place of the pad, the journal lists every quest started and the detail view displays
 * one in full: [ExplorationPresenter] opens them when the tracker is tapped, the journal once a side quest is listed, and
 * Back steps out one level at a time. Abandon asks first, and returns to the journal, or to the pad when no side quest is left.
 */
class QuestPresenter(
    private val questTrackerView: QuestTrackerView,
    private val questDetailView: QuestDetailView,
    private val questJournalView: QuestJournalView,
    private val confirmDialogView: ConfirmDialogView,
    private val questAnnouncementView: QuestAnnouncementView,
    private val questApi: QuestApi,
    private val partyApi: PartyApi,
    private val texts: Texts,
    private val timeline: BattleSceneTimeline,
    eventBus: EventBus,
) : QuestTrackerView.Delegate,
    QuestDetailView.Delegate,
    QuestJournalView.Delegate {
    /** Whether the scene displays the journal or the detail view. */
    var isQuestOpen = false
        private set

    // Whether Back in the detail view returns to the journal.
    private var isDetailFromJournal = false

    // The quest the detail view displays, so Abandon knows which one.
    private var detailedQuest: QuestInfo? = null

    // The toasts still to play, and whether one is on its way out, so they never overlap.
    private val pendingToasts = ArrayDeque<Pair<Moment, QuestInfo>>()
    private var isPlayingToasts = false

    private var onTrackerSelected: (() -> Unit)? = null

    // What the tracker displays, or will once the timeline gets to it, so an event with nothing new draws nothing.
    private var displayedQuest: QuestInfo? = null

    private val subscriptions =
        listOf(
            eventBus.subscribe<QuestStarted> { event -> announce(Moment.STARTED, event.questId) },
            eventBus.subscribe<TaskProgressed> { event -> taskChanged(event.questId) },
            eventBus.subscribe<TaskCompleted> { event -> taskChanged(event.questId) },
            eventBus.subscribe<QuestCompleted> { event -> announce(Moment.COMPLETED, event.questId) },
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
                    closeQuest()
                }
            },
        )

    init {
        questDetailView.hide()
        questJournalView.hide()
        confirmDialogView.visible = false
        questTrackerView.setDelegate(this)
        questDetailView.setDelegate(this)
        questJournalView.setDelegate(this)
    }

    /** Hands the taps on the tracker over, e.g.: to open the quest when it can take the place of the pad. */
    fun setOnTrackerSelected(listener: () -> Unit) {
        onTrackerSelected = listener
    }

    /**
     * Displays the journal once a side quest is listed, and otherwise the main quest in full, so there is no list of one row.
     * Read now, as time stops while it is open. To be played in the timeline.
     */
    fun openQuest() {
        val quests = listedQuests()
        if (quests.isEmpty()) return
        isQuestOpen = true
        if (quests.any { quest -> !quest.isMain }) {
            questJournalView.display(quests)
        } else {
            isDetailFromJournal = false
            displayDetail(quests.single())
        }
    }

    /** Hides whichever is open. Nothing happens when none is. */
    fun closeQuest() {
        if (!isQuestOpen) return
        isQuestOpen = false
        questDetailView.hide()
        questJournalView.hide()
        confirmDialogView.visible = false
    }

    override fun questTrackerSelected() {
        onTrackerSelected?.invoke()
    }

    // The player can only act once the scene shows them.
    override fun questJournalSelected(questId: String) {
        if (!canAct()) return
        val quest = listedQuests().firstOrNull { quest -> quest.id == questId } ?: return
        isDetailFromJournal = true
        questJournalView.hide()
        displayDetail(quest)
    }

    override fun questJournalBackSelected() {
        if (canAct()) partyApi.closeQuest()
    }

    override fun questDetailBackSelected() {
        if (!canAct()) return
        if (isDetailFromJournal) {
            questDetailView.hide()
            questJournalView.display(listedQuests())
        } else {
            partyApi.closeQuest()
        }
    }

    override fun questDetailAbandonSelected() {
        val quest = detailedQuest
        if (!canAct() || quest == null) return
        questDetailView.hide()
        confirmDialogView.display(
            message = texts[UiText.QUEST_ABANDON_QUESTION, "quest" to quest.name],
            cancelLabel = texts[UiText.QUEST_DETAIL_BACK],
            confirmLabel = texts[UiText.QUEST_ABANDON],
            onCancelled = { if (canAct()) keepQuest(quest) },
            onConfirmed = { if (canAct()) abandonQuest(quest) },
        )
        confirmDialogView.visible = true
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

    // The reward is the one the quest defines, read as it completes, as RewardGranted names no quest, and never comes when
    // nobody is left to receive it. The tracker only follows the main quest.
    private fun announce(
        moment: Moment,
        questId: String,
    ) {
        val quest = questApi.searchQuests().firstOrNull { quest -> quest.id == questId } ?: return
        val questInfo = QuestMapper.questInfo(quest, texts)
        val isMain = quest.type == QuestTypeDto.MAIN
        if (isMain) displayedQuest = questInfo
        timeline.play {
            if (isMain) questTrackerView.display(questInfo)
            pendingToasts.addLast(moment to questInfo)
            if (!isPlayingToasts) {
                isPlayingToasts = true
                timeline.playAlongside { playToasts() }
            }
        }
    }

    private suspend fun playToasts() {
        while (pendingToasts.isNotEmpty()) {
            val (moment, quest) = pendingToasts.removeFirst()
            questAnnouncementView.announce(moment, quest)
        }
        isPlayingToasts = false
    }

    private fun canAct() = isQuestOpen && !timeline.isPlaying

    private fun displayDetail(quest: QuestInfo) {
        detailedQuest = quest
        questDetailView.display(quest)
    }

    private fun keepQuest(quest: QuestInfo) {
        confirmDialogView.visible = false
        displayDetail(quest)
    }

    // With it gone from the list, the journal is displayed when a side quest is still listed, and otherwise the pad.
    private fun abandonQuest(quest: QuestInfo) {
        questApi.abandonQuest(quest.id)
        val quests = listedQuests()
        if (quests.any { listed -> !listed.isMain }) {
            confirmDialogView.visible = false
            questJournalView.display(quests)
        } else {
            partyApi.closeQuest()
        }
    }

    // The main quest first while active, then the active side quests, then the completed quests, each group in the order
    // the quests were created. The side quests not started are hidden, so nothing is spoiled.
    private fun listedQuests(): List<QuestInfo> {
        val (completed, active) =
            questApi
                .searchQuests()
                .filter { quest -> quest.type == QuestTypeDto.MAIN || quest.progress.status != StatusDto.NOT_STARTED }
                .map { quest -> QuestMapper.questInfo(quest, texts) }
                .partition { quest -> quest.isCompleted }
        return active.sortedByDescending { quest -> quest.isMain } + completed
    }

    private fun displayTracker(mainQuest: Quest.Dto) {
        val quest = QuestMapper.questInfo(mainQuest, texts)
        displayedQuest = quest
        timeline.play { questTrackerView.display(quest) }
    }

    private fun mainQuestNamed(questId: String): Quest.Dto? = mainQuest()?.takeIf { quest -> quest.id == questId }

    private fun mainQuest(): Quest.Dto? = questApi.searchQuests().firstOrNull { quest -> quest.type == QuestTypeDto.MAIN }
}
