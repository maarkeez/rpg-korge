package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.localization.domain.Localization.Dto.LanguageDto
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class ToolbarViewTest : ViewsForTesting() {
    private val selectedLanguageCodes = mutableListOf<String>()
    private val languages = listOf(LanguageDto(code = "en", name = "English"), LanguageDto(code = "es", name = "Español"))

    private fun newToolbarView() =
        ToolbarView().apply {
            setDelegate(
                object : ToolbarView.Delegate {
                    override fun languageSelected(languageCode: String) {
                        selectedLanguageCodes += languageCode
                    }
                },
            )
        }

    @Test
    fun `ToolbarView should display the language label and no language when it is displayed`() =
        sharedViewsTest {
            // When
            val toolbarView = newToolbarView()
            // Then
            toolbarView.languageLabelText() shouldBe "Language"
            toolbarView.languageNames().shouldBeEmpty()
        }

    @Test
    fun `Display should list the languages in their order, highlighting only the selected one`() =
        sharedViewsTest {
            // Given
            val toolbarView = newToolbarView()
            // When
            toolbarView.display(languages, selectedLanguageCode = "es")
            // Then
            toolbarView.languageNames().shouldContainExactly("English", "Español")
            toolbarView.selectedLanguageNames().shouldContainExactly("Español")
        }

    @Test
    fun `ClickLanguage should select the language when it is clicked`() =
        sharedViewsTest {
            // Given
            val toolbarView = newToolbarView()
            addChild(toolbarView)
            toolbarView.display(languages, selectedLanguageCode = "en")
            // When
            clickLanguage(toolbarView, "Español")
            // Then
            selectedLanguageCodes.shouldContainExactly("es")
        }
}
