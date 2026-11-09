package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.npc.domain.NpcEvent.NpcRecruited
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.PartyEvent.ExplorationInterrupted
import com.mkz.rpg.party.domain.PartyEvent.ExplorationResumed
import com.mkz.rpg.quest.adapters.presentation.QuestApi
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.domain.QuestEvent.DefeatCounted
import com.mkz.rpg.quest.domain.QuestEvent.QuestAbandoned
import com.mkz.rpg.quest.domain.QuestEvent.QuestCompleted
import com.mkz.rpg.quest.domain.QuestEvent.QuestPinned
import com.mkz.rpg.quest.domain.QuestEvent.QuestStarted
import com.mkz.rpg.quest.domain.QuestEvent.TaskCompleted
import com.mkz.rpg.save.domain.SaveEvent.GameLoaded
import com.mkz.rpg.screen.adapters.presentation.QuestAnnouncementView.Moment
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe

/**
 * Keeps the quests on the screen: the tracker names the current task of the pinned quest, or else of the main quest, popping
 * when it changes, also when a quest it does not name progresses, and stays ticked once the main quest is completed. A toast
 * announces any quest starting or completing, one after the other, alongside the timeline, so it holds nothing. The tracker
 * is read only in a battle, and hidden once a player wins, as the winner is drawn across the whole top line.
 *
 * While the party reads the quests, in place of the pad, the journal lists every quest started and the detail view displays
 * one in full: [ExplorationPresenter] opens them when the tracker is tapped, the journal once a side quest is listed. Their
 * buttons are in the row of the call to action, where Back steps out one level at a time. Abandon asks first, and returns to
 * the journal, or to the pad when no side quest is left.
 *
 * While exploring, a quest marker over an npc's head says who to talk to. An npc can only be talked to then, so a battle
 * removes them all until it finishes.
 */
class QuestPresenter(
    private val questTrackerView: QuestTrackerView,
    private val questDetailView: QuestDetailView,
    private val questJournalView: QuestJournalView,
    private val confirmDialogView: ConfirmDialogView,
    private val questAnnouncementView: QuestAnnouncementView,
    private val battlefieldView: BattlefieldView,
    private val playerCallToActionView: PlayerCallToActionView,
    private val questApi: QuestApi,
    private val partyApi: PartyApi,
    private val npcApi: NpcApi,
    private val texts: Texts,
    private val timeline: BattleSceneTimeline,
    eventBus: EventBus,
) : QuestTrackerView.Delegate,
    QuestJournalView.Delegate {
    /** Whether the scene displays the journal or the detail view. */
    var isQuestOpen = false
        private set

    // Whether Back in the detail view returns to the journal.
    private var isDetailFromJournal = false

    // The toasts still to play, and whether one is on its way out, so they never overlap.
    private val pendingToasts = ArrayDeque<Pair<Moment, QuestInfo>>()
    private var isPlayingToasts = false

    private var onTrackerSelected: (() -> Unit)? = null

    // What the tracker displays, or will once the timeline gets to it, so nothing new draws nothing.
    private var displayedQuest: QuestInfo? = null

    // Each quest as its last event read it, so a defeat publishing two events pops the tracker once.
    private val lastSeenQuests = mutableMapOf<String, QuestInfo>()

    private var isInBattle = false

    // What the battlefield displays, or will once the timeline gets to it.
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
            displayJournal(quests)
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

    override fun questJournalPinSelected(questId: String) {
        if (canAct()) questApi.pinQuest(questId)
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    // A last defeat publishes three events: nothing draws while the quest is completed, as QuestCompleted follows. And a
    // defeat completing a task publishes two, both reading the next task, so only the first one finds something new.
    private fun taskChanged(questId: String) {
        val quest = questInfos().firstOrNull { quest -> quest.id == questId } ?: return
        val lastSeen = lastSeenQuests.put(questId, quest)
        if (quest.isCompleted || quest == lastSeen) return
        val isNamed = quest.id == trackedQuest()?.id
        if (isNamed) displayedQuest = quest
        timeline.play {
            if (isNamed) questTrackerView.display(quest)
            timeline.playAlongside { questTrackerView.pop() }
        }
    }

    // The reward is the one the quest defines, read as it completes, as RewardGranted names no quest, and never comes when
    // nobody is left to receive it. A side quest starting does not take the pin, so it leaves the tracker as it is.
    private fun announce(
        moment: Moment,
        questId: String,
    ) {
        val quest = questInfos().firstOrNull { quest -> quest.id == questId } ?: return
        lastSeenQuests[questId] = quest
        val trackedQuest = trackedQuest()?.takeIf { tracked -> tracked != displayedQuest && (quest.isMain || moment == Moment.COMPLETED) }
        trackedQuest?.let { tracked -> displayedQuest = tracked }
        timeline.play {
            trackedQuest?.let(questTrackerView::display)
            pendingToasts.addLast(moment to quest)
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

    private fun displayJournal(quests: List<QuestInfo>) {
        questJournalView.display(quests)
        playerCallToActionView.displayActions(journalActions())
    }

    private fun displayDetail(quest: QuestInfo) {
        questDetailView.display(quest)
        playerCallToActionView.displayActions(detailActions(quest))
    }

    private fun journalActions() = listOf(backAction { if (canAct()) partyApi.closeQuest() })

    // Only an active side quest can be abandoned.
    private fun detailActions(quest: QuestInfo) =
        listOfNotNull(
            backAction { detailBackSelected() },
            if (!quest.isMain && !quest.isCompleted) {
                ActionRowView.Action(texts[UiText.QUEST_ABANDON], HudStyle.DANGER, HudStyle.DANGER_PRESSED) { abandonSelected(quest) }
            } else {
                null
            },
        )

    private fun backAction(onSelected: () -> Unit) = ActionRowView.Action(texts[UiText.QUEST_DETAIL_BACK], onSelected = onSelected)

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
        confirmDialogView.display(texts[UiText.QUEST_ABANDON_QUESTION, "quest" to quest.name])
        confirmDialogView.visible = true
        playerCallToActionView.displayConfirmation(
            cancelLabel = texts[UiText.QUEST_DETAIL_BACK],
            confirmLabel = texts[UiText.QUEST_ABANDON],
            onCancelled = { if (canAct()) keepQuest(quest) },
            onConfirmed = { if (canAct()) abandonQuest(quest) },
        )
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
            displayJournal(quests)
        } else {
            partyApi.closeQuest()
        }
    }

    // The main quest first while active, then the active side quests, then the completed quests, each group in the order
    // the quests were created. The side quests not started are hidden, so nothing is spoiled.
    private fun listedQuests(): List<QuestInfo> {
        val startedQuestIds = questApi.searchQuests().filter { quest -> quest.progress.status != StatusDto.NOT_STARTED }.map { quest -> quest.id }
        val (completed, active) =
            questInfos()
                .filter { quest -> quest.isMain || quest.id in startedQuestIds }
                .partition { quest -> quest.isCompleted }
        return active.sortedByDescending { quest -> quest.isMain } + completed
    }

    private fun displayTracker() {
        val quest = trackedQuest()?.takeIf { tracked -> tracked != displayedQuest } ?: return
        displayedQuest = quest
        timeline.play { questTrackerView.display(quest) }
    }

    // Each marker on the tile of its npc. An npc no longer on the battlefield, e.g.: once recruited, has none.
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

    private fun questInfos(): List<QuestInfo> = QuestMapper.questInfos(questApi.searchQuests(), texts)

    private fun trackedQuest(): QuestInfo? = questInfos().firstOrNull { quest -> quest.isPinned }
}
