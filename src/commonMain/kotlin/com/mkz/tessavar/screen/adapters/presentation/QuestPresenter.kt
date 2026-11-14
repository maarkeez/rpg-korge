package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.npc.adapters.presentation.NpcApi
import com.mkz.tessavar.npc.domain.NpcEvent.NpcRecruited
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.party.domain.PartyEvent.ExplorationInterrupted
import com.mkz.tessavar.party.domain.PartyEvent.ExplorationResumed
import com.mkz.tessavar.party.domain.PartyEvent.GameOver
import com.mkz.tessavar.quest.adapters.presentation.QuestApi
import com.mkz.tessavar.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.tessavar.quest.domain.Quest.Dto.StatusDto
import com.mkz.tessavar.quest.domain.QuestEvent.DefeatCounted
import com.mkz.tessavar.quest.domain.QuestEvent.QuestAbandoned
import com.mkz.tessavar.quest.domain.QuestEvent.QuestCompleted
import com.mkz.tessavar.quest.domain.QuestEvent.QuestPinned
import com.mkz.tessavar.quest.domain.QuestEvent.QuestStarted
import com.mkz.tessavar.quest.domain.QuestEvent.TaskCompleted
import com.mkz.tessavar.save.domain.SaveEvent.GameLoaded
import com.mkz.tessavar.screen.adapters.presentation.QuestAnnouncementView.Moment
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.Subscription
import com.mkz.tessavar.shared.domain.subscribe

class QuestPresenter(
    private val questTrackerView: QuestTrackerView,
    private val questDetailView: QuestDetailView,
    private val questJournalView: QuestJournalView,
    private val confirmDialogView: ConfirmDialogView,
    private val questAnnouncementView: QuestAnnouncementView,
    private val battlefieldView: BattlefieldView,
    private val callToActionView: CallToActionView,
    private val questApi: QuestApi,
    private val partyApi: PartyApi,
    private val npcApi: NpcApi,
    private val messages: Messages,
    private val timeline: GameSceneTimeline,
    eventBus: EventBus,
) : QuestTrackerView.Delegate,
    QuestJournalView.Delegate {
    var isQuestOpen = false
        private set

    private var isDetailFromJournal = false

    private val pendingAnnouncements = ArrayDeque<Pair<Moment, QuestInfo>>()
    private var isPlayingAnnouncements = false

    private var onTrackerSelected: (() -> Unit)? = null

    private var displayedTracker: Tracker = Tracker.Cleared

    private val lastSeenQuests = mutableMapOf<String, QuestInfo>()

    private var isInBattle = false

    private var displayedMarkers = emptyMap<TilePosition, QuestMarkerView.Sign>()

    private val subscriptions =
        listOf(
            eventBus.subscribe<QuestStarted> { event ->
                announce(Moment.STARTED, event.questId)
                displayMarkers()
            },
            eventBus.subscribe<DefeatCounted> { event -> taskChanged(event.questId) },
            eventBus.subscribe<TaskCompleted> { event ->
                taskChanged(event.questId)
                displayMarkers()
            },
            eventBus.subscribe<QuestCompleted> { event ->
                announce(Moment.COMPLETED, event.questId)
                displayMarkers()
            },
            eventBus.subscribe<QuestPinned> {
                displayTracker()
                val quests = listedQuests()
                timeline.play { if (questJournalView.visible) questJournalView.display(quests) }
            },
            eventBus.subscribe<QuestAbandoned> { event ->
                lastSeenQuests.remove(event.questId)
                displayTracker()
                displayMarkers()
            },
            eventBus.subscribe<NpcRecruited> { displayMarkers() },
            eventBus.subscribe<GameLoaded> {
                displayTracker()
                displayMarkers()
            },
            eventBus.subscribe<ExplorationInterrupted> {
                isInBattle = true
                timeline.play { questTrackerView.setTappable(false) }
                moveMarkersTo(emptyMap())
            },
            eventBus.subscribe<ExplorationResumed> {
                isInBattle = false
                timeline.play { questTrackerView.setTappable(true) }
                displayMarkers()
            },
            eventBus.subscribe<GameOver> {
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
        questJournalView.setDelegate(this)
    }

    fun setOnTrackerSelected(listener: () -> Unit) {
        onTrackerSelected = listener
    }

    fun openQuest() {
        val quests = listedQuests()
        if (quests.isEmpty()) return
        isQuestOpen = true
        if (quests.any { quest -> !quest.isMain }) {
            displayJournal(quests)
        } else {
            isDetailFromJournal = false
            displayDetail(quests.single())
        }
    }

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

    override fun questJournalSelected(questId: String) {
        if (!canAct()) return
        val quest = listedQuests().firstOrNull { quest -> quest.id == questId } ?: return
        isDetailFromJournal = true
        questJournalView.hide()
        displayDetail(quest)
    }

    override fun questJournalPinSelected(questId: String) {
        if (canAct()) questApi.pinQuest(questId)
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    private fun taskChanged(questId: String) {
        val quest = questInfos().firstOrNull { quest -> quest.id == questId } ?: return
        val lastSeen = lastSeenQuests.put(questId, quest)
        if (quest.isCompleted || quest == lastSeen) return
        val isNamed = quest.id == trackedQuest()?.id
        if (isNamed) displayedTracker = Tracker.Named(quest)
        timeline.play {
            if (isNamed) questTrackerView.display(quest)
            timeline.playAlongside { questTrackerView.pop() }
        }
    }

    private fun announce(
        moment: Moment,
        questId: String,
    ) {
        val quest = questInfos().firstOrNull { quest -> quest.id == questId } ?: return
        lastSeenQuests[questId] = quest
        val tracker = tracker().takeIf { tracker -> tracker != displayedTracker }
        tracker?.let { changed -> displayedTracker = changed }
        timeline.play {
            tracker?.let(::draw)
            pendingAnnouncements.addLast(moment to quest)
            if (!isPlayingAnnouncements) {
                isPlayingAnnouncements = true
                timeline.playAlongside { playAnnouncements() }
            }
        }
    }

    private suspend fun playAnnouncements() {
        while (pendingAnnouncements.isNotEmpty()) {
            val (moment, quest) = pendingAnnouncements.removeFirst()
            questAnnouncementView.announce(moment, quest)
        }
        isPlayingAnnouncements = false
    }

    private fun canAct() = isQuestOpen && !timeline.isPlaying

    private fun displayJournal(quests: List<QuestInfo>) {
        questJournalView.display(quests)
        callToActionView.displayButtons(journalActions())
    }

    private fun displayDetail(quest: QuestInfo) {
        questDetailView.display(quest)
        callToActionView.displayButtons(detailActions(quest))
    }

    private fun journalActions() = listOf(backAction { if (canAct()) partyApi.closeQuest() })

    private fun detailActions(quest: QuestInfo) =
        listOfNotNull(
            backAction { detailBackSelected() },
            if (!quest.isMain && !quest.isCompleted) {
                ButtonRowView.Action(messages[UiMessage.QUEST_ABANDON], HudStyle.DANGER, HudStyle.DANGER_PRESSED) { abandonSelected(quest) }
            } else {
                null
            },
        )

    private fun backAction(onSelected: () -> Unit) = ButtonRowView.Action(messages[UiMessage.QUEST_DETAIL_BACK], onSelected = onSelected)

    private fun detailBackSelected() {
        if (!canAct()) return
        if (isDetailFromJournal) {
            questDetailView.hide()
            displayJournal(listedQuests())
        } else {
            partyApi.closeQuest()
        }
    }

    private fun abandonSelected(quest: QuestInfo) {
        if (!canAct()) return
        questDetailView.hide()
        confirmDialogView.display(messages[UiMessage.QUEST_ABANDON_QUESTION, "quest" to quest.name])
        confirmDialogView.visible = true
        callToActionView.displayConfirmation(
            cancelLabel = messages[UiMessage.QUEST_DETAIL_BACK],
            confirmLabel = messages[UiMessage.QUEST_ABANDON],
            onCancelled = { if (canAct()) keepQuest(quest) },
            onConfirmed = { if (canAct()) abandonQuest(quest) },
        )
    }

    private fun keepQuest(quest: QuestInfo) {
        confirmDialogView.visible = false
        displayDetail(quest)
    }

    private fun abandonQuest(quest: QuestInfo) {
        questApi.abandonQuest(quest.id)
        val quests = listedQuests()
        if (quests.any { listed -> !listed.isMain }) {
            confirmDialogView.visible = false
            displayJournal(quests)
        } else {
            partyApi.closeQuest()
        }
    }

    private fun listedQuests(): List<QuestInfo> {
        val startedQuestIds = questApi.searchQuests().filter { quest -> quest.progress.status != StatusDto.NOT_STARTED }.map { quest -> quest.id }
        val (completed, active) =
            questInfos()
                .filter { quest -> quest.id in startedQuestIds }
                .partition { quest -> quest.isCompleted }
        return active.sortedByDescending { quest -> quest.isMain } + completed
    }

    private fun displayTracker() {
        val tracker = tracker().takeIf { tracker -> tracker != displayedTracker } ?: return
        displayedTracker = tracker
        timeline.play { draw(tracker) }
    }

    private fun tracker(): Tracker {
        trackedQuest()?.let { quest -> return Tracker.Named(quest) }
        val isSideQuestActive = questApi.searchQuests().any { quest -> quest.type == QuestTypeDto.SIDE && quest.progress.status == StatusDto.ACTIVE }
        return if (isSideQuestActive) Tracker.Empty else Tracker.Cleared
    }

    private fun draw(tracker: Tracker) =
        when (tracker) {
            is Tracker.Named -> questTrackerView.display(tracker.quest)
            Tracker.Empty -> questTrackerView.displayEmpty()
            Tracker.Cleared -> questTrackerView.clear()
        }

    private fun displayMarkers() {
        if (isInBattle) return
        moveMarkersTo(
            questApi.searchQuestMarkers().mapNotNull { marker -> npcApi.searchNpcById(marker.npcId)?.let { npc -> TilePosition(npc.row, npc.column) to QuestMapper.signOf(marker.sign) } }.toMap(),
        )
    }

    private fun moveMarkersTo(markers: Map<TilePosition, QuestMarkerView.Sign>) {
        val gone = displayedMarkers.keys - markers.keys
        val changed = markers.filter { (position, sign) -> displayedMarkers[position] != sign }
        if (gone.isEmpty() && changed.isEmpty()) return
        displayedMarkers = markers
        timeline.play {
            gone.forEach { position -> battlefieldView.hideQuestMarker(position.row, position.column) }
            changed.forEach { (position, sign) -> battlefieldView.displayQuestMarker(position.row, position.column, sign) }
        }
    }

    private fun questInfos(): List<QuestInfo> = QuestMapper.questInfos(questApi.searchQuests(), messages)

    private fun trackedQuest(): QuestInfo? = questInfos().firstOrNull { quest -> quest.isPinned }

    private sealed interface Tracker {
        data class Named(
            val quest: QuestInfo,
        ) : Tracker

        data object Empty : Tracker

        data object Cleared : Tracker
    }
}
