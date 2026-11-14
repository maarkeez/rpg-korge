package com.mkz.tessavar.screen.adapters.presentation

class FakeWorldBuilderInfoView : WorldBuilderInfoView(MessagesMother.messages()) {
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
