package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.localization.domain.Localization.Dto.LanguageDto

// Records the languages the presenter asks the toolbar to display, instead of drawing them.
class FakeToolbarView : ToolbarView() {
    val selectedLanguageCodes = mutableListOf<String>()
    var displayedLanguages: List<LanguageDto> = emptyList()
        private set
    var delegate: Delegate? = null
        private set

    override fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    override fun display(
        languages: List<LanguageDto>,
        selectedLanguageCode: String,
    ) {
        displayedLanguages = languages
        selectedLanguageCodes += selectedLanguageCode
    }
}
