package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class ConfirmDialogViewTest : ViewsForTesting() {
    @Test
    fun `ConfirmDialogView should display the message when it asks`() =
        sharedViewsTest {
            // Given
            val confirmDialogView = ConfirmDialogView()
            // When
            confirmDialogView.display("Quit to the menu?")
            // Then
            confirmDialogView.message() shouldBe "Quit to the menu?"
        }

    @Test
    fun `Display should replace the question when it asks again`() =
        sharedViewsTest {
            // Given
            val confirmDialogView = ConfirmDialogView()
            confirmDialogView.display("Quit to the menu?")
            // When
            confirmDialogView.display("Delete?")
            // Then
            confirmDialogView.message() shouldBe "Delete?"
        }
}
