package com.mkz.rpg.screen.adapters.presentation

// Records what the presenters ask the tracker to display, instead of drawing it.
class FakeQuestTrackerView : QuestTrackerView(MessagesMother.messages()) {
    val displayedQuests = mutableListOf<QuestInfo>()

    // Each state drawn, in order: the id of a quest, "empty" for the empty tracker, or "cleared".
    val drawnStates = mutableListOf<String>()
    val tappableChanges = mutableListOf<Boolean>()
    var pops = 0
    var hides = 0

    override fun display(quest: QuestInfo) {
        displayedQuests += quest
        drawnStates += quest.id
    }

    override fun displayEmpty() {
        drawnStates += "empty"
    }

    override fun clear() {
        drawnStates += "cleared"
    }

    override fun setTappable(isTappable: Boolean) {
        tappableChanges += isTappable
    }

    override fun hide() {
        hides++
    }

    override suspend fun pop() {
        pops++
    }
}
