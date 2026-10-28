package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting

// Page object of [ToolbarView].

fun ToolbarView.languageLabelText(): String = languageLabel.text

/** The names of the languages listed, in their order, e.g.: `English`. */
fun ToolbarView.languageNames(): List<String> = languageButtons.values.map { button -> button.label.text }

/** The names of the languages highlighted as selected. */
fun ToolbarView.selectedLanguageNames(): List<String> = languageButtons.values.filter { button -> button.isSelected }.map { button -> button.label.text }

suspend fun ViewsForTesting.clickLanguage(
    toolbarView: ToolbarView,
    languageName: String,
) = clickOn(toolbarView.languageButtons.values.first { button -> button.label.text == languageName })
