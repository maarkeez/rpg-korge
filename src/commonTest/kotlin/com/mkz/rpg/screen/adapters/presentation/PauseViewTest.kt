package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldContainExactly
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class PauseViewTest : ViewsForTesting() {
    private val selections = mutableListOf<String>()

    private fun newPauseView() =
        PauseView(TextsMother.texts()).apply {
            setDelegate(
                object : PauseView.Delegate {
                    override fun pauseResumeSelected() {
                        selections += "resume"
                    }

                    override fun pauseSaveSelected() {
                        selections += "save"
                    }

                    override fun pauseQuitSelected() {
                        selections += "quit"
                    }
                },
            )
        }

    @Test
    fun `PauseView should display Resume, Save and Quit to menu when it is displayed`() =
        sharedViewsTest {
            // When
            val pauseView = newPauseView()
            // Then
            pauseView.optionLabels().shouldContainExactly("Resume", "Save", "Quit to menu")
        }

    @Test
    fun `ClickOption should call the delegate once when each option is tapped`() =
        sharedViewsTest {
            // Given
            val pauseView = newPauseView()
            addChild(pauseView)
            // When
            clickResume(pauseView)
            clickSave(pauseView)
            clickQuit(pauseView)
            // Then
            selections.shouldContainExactly("resume", "save", "quit")
        }
}
