package com.mkz.rpg.screen.adapters.presentation

// Records what the presenter asks the map builder header to display, instead of drawing it.
class FakeMapBuilderInfoView : MapBuilderInfoView(TextsMother.texts()) {
    var displayedSize: Pair<Int, Int>? = null
        private set
    val errors = mutableListOf<String>()
    val infos = mutableListOf<String>()

    override fun displaySize(
        rows: Int,
        columns: Int,
    ) {
        displayedSize = rows to columns
    }

    override fun displayError(message: String) {
        errors += message
    }

    override fun displayInfo(message: String) {
        infos += message
    }
}
