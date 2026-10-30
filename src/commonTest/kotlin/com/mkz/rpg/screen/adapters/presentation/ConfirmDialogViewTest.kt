package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class ConfirmDialogViewTest : ViewsForTesting() {
    private val choices = mutableListOf<String>()

    private fun newConfirmDialogView() =
        ConfirmDialogView().apply {
            display(
                message = "Quit to the menu?",
                cancelLabel = "Cancel",
                confirmLabel = "Quit to menu",
                onCancelled = { choices += "cancel" },
                onConfirmed = { choices += "confirm" },
            )
        }

    @Test
    fun `ConfirmDialogView should display the message over both choices when it asks`() =
        sharedViewsTest {
            // When
            val confirmDialogView = newConfirmDialogView()
            // Then
            confirmDialogView.message() shouldBe "Quit to the menu?"
            confirmDialogView.choiceLabels().shouldContainExactly("Cancel", "Quit to menu")
        }

    @Test
    fun `ClickChoice should call the action of the choice tapped`() =
        sharedViewsTest {
            // Given
            val confirmDialogView = newConfirmDialogView()
            addChild(confirmDialogView)
            // When
            clickCancel(confirmDialogView)
            clickConfirm(confirmDialogView)
            // Then
            choices.shouldContainExactly("cancel", "confirm")
        }

    @Test
    fun `Display should replace the question and its choices when it asks again`() =
        sharedViewsTest {
            // Given
            val confirmDialogView = newConfirmDialogView()
            addChild(confirmDialogView)
            // When
            confirmDialogView.display(message = "Delete?", cancelLabel = "Keep", confirmLabel = "Delete", onCancelled = {}, onConfirmed = { choices += "delete" })
            clickConfirm(confirmDialogView)
            // Then
            confirmDialogView.message() shouldBe "Delete?"
            confirmDialogView.choiceLabels().shouldContainExactly("Keep", "Delete")
            choices.shouldContainExactly("delete")
        }
}
