package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.localization.domain.Localization.Dto.LanguageDto

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
