package com.mkz.tessavar.screen.adapters.presentation

class FakeQuestTrackerView : QuestTrackerView(MessagesMother.messages()) {
    val displayedQuests = mutableListOf<QuestInfo>()

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
