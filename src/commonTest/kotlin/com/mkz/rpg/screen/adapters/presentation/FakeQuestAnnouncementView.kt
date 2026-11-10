package com.mkz.rpg.screen.adapters.presentation

import kotlinx.coroutines.CompletableDeferred

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

    fun fadeAnnouncement() {
        val faded = held
        held = CompletableDeferred()
        faded.complete(Unit)
    }
}
