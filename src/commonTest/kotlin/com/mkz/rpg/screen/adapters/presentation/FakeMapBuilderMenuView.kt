package com.mkz.rpg.screen.adapters.presentation

// Records the options the presenter displays in the menu, so the tests can select them.
class FakeMapBuilderMenuView : MapBuilderMenuView() {
    var displayedOptions: List<Option> = emptyList()
        private set

    override fun displayOptions(options: List<Option>) {
        displayedOptions = options
    }

    fun select(label: String) = displayedOptions.single { option -> option.label == label }.onSelected()
}
