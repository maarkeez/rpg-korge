package com.mkz.rpg.screen.adapters.presentation

import kotlinx.coroutines.CompletableDeferred

// Records what the presenters ask the announcement to announce, instead of drawing it. Each announcement is over at once, unless it is told to hold them.
class FakeQuestAnnouncementView : QuestAnnouncementView(GameSceneResourcesLoader(), MessagesMother.messages()) {
    val announcements = mutableListOf<Pair<Moment, QuestInfo>>()
    var isHolding = false
    private var held = CompletableDeferred<Unit>()

    override suspend fun announce(
        moment: Moment,
        quest: QuestInfo,
    ) {
        announcements += moment to quest
        if (isHolding) held.await()
    }

    /** Fades the announcement being held, so the next one plays, holding it in turn. */
    fun fadeAnnouncement() {
        val faded = held
        held = CompletableDeferred()
        faded.complete(Unit)
    }
}
