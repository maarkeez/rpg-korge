package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.nextFrame
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
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

    @Test
    fun `DisplaySaved should display Saved under the options, and hide it once its time is over`() =
        sharedViewsTest {
            // Given
            val pauseView = newPauseView()
            addChild(pauseView)
            // When
            pauseView.displaySaved(isDisplayed = true)
            // Then
            pauseView.isSavedShown().shouldBeTrue()
            pauseView.savedLabel() shouldBe "Saved"
            // When
            repeat((PauseView.SAVED_DURATION / frameTime).toInt() + 2) { nextFrame() }
            // Then
            pauseView.isSavedShown().shouldBeFalse()
        }
}
