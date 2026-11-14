package com.mkz.tessavar.screen.adapters.presentation

class FakeWorldBuilderMenuView : WorldBuilderMenuView() {
    var displayedOptions: List<Option> = emptyList()
        private set

    override fun displayOptions(options: List<Option>) {
        displayedOptions = options
    }

    fun select(label: String) = displayedOptions.single { option -> option.label == label }.onSelected()
}
