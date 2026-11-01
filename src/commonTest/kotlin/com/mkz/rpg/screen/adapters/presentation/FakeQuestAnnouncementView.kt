package com.mkz.rpg.screen.adapters.presentation

// Records what the presenters ask the announcement to display, instead of drawing it.
class FakeQuestAnnouncementView : QuestAnnouncementView(BattleSceneResourcesLoader(), TextsMother.texts()) {
    val announcements = mutableListOf<Pair<Moment, QuestInfo>>()
    var isAnnouncing = false

    override fun display(
        moment: Moment,
        quest: QuestInfo,
    ) {
        announcements += moment to quest
        isAnnouncing = true
    }

    override fun hide() {
        isAnnouncing = false
    }
}
