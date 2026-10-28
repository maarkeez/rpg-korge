package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.localization.domain.Localization.Dto.LanguageDto
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.addTo
import korlibs.korge.view.container
import korlibs.korge.view.position
import korlibs.korge.view.text

/**
 * The globals, shared by every story, above the story info: the language of the views on show, each language named in
 * its own language, e.g.: `Español`, the one selected highlighted. Tapping another one asks to select it.
 */
open class ToolbarView : Container() {
    companion object {
        const val HEIGHT = StorybookButton.HEIGHT
        private const val LABEL_WIDTH = 96.0
        private const val BUTTON_GAP = 6.0
    }

    interface Delegate {
        fun languageSelected(languageCode: String)
    }

    private var delegate: Delegate? = null

    internal val languageLabel: Text = text("Language", textSize = 16.0, color = StoryInfoView.SECONDARY_COLOR).position(0.0, (HEIGHT - 16.0) / 2)
    internal val languages: Container = container().position(LABEL_WIDTH, 0.0)

    // By the code of their language, in the order they are listed.
    internal var languageButtons: Map<String, StorybookButton> = emptyMap()
        private set

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Lists the languages, in their order, highlighting the selected one. */
    open fun display(
        languages: List<LanguageDto>,
        selectedLanguageCode: String,
    ) {
        this.languages.removeChildren()
        var x = 0.0
        languageButtons =
            languages.associate { language ->
                val size = StorybookButton.sizeFor(language.name)
                val button = StorybookButton(language.name, size) { delegate?.languageSelected(language.code) }.addTo(this.languages).position(x, 0.0)
                button.isSelected = language.code == selectedLanguageCode
                x += size.width + BUTTON_GAP
                language.code to button
            }
    }
}
