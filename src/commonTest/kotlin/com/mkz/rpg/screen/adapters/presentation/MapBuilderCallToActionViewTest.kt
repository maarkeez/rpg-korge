package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class MapBuilderCallToActionViewTest : ViewsForTesting() {
    private val actions = mutableListOf<String>()

    @Test
    fun `DisplayExportAndBack should display going back and exporting when it is displayed`() =
        sharedViewsTest {
            // Given
            val callToActionView = MapBuilderCallToActionView(TextsMother.texts())
            // When
            callToActionView.displayExportAndBack(onExported = {}, onBack = {})
            // Then
            callToActionView.actionLabels() shouldBe ("Back" to "Export")
        }

    @Test
    fun `DisplayExportAndBack should export and go back when they are clicked`() =
        sharedViewsTest {
            // Given
            val callToActionView = MapBuilderCallToActionView(TextsMother.texts())
            callToActionView.displayExportAndBack(onExported = { actions += "export" }, onBack = { actions += "back" })
            addChild(callToActionView)
            // When
            clickPrimaryAction(callToActionView)
            clickSecondaryAction(callToActionView)
            // Then
            actions.shouldContainExactly("export", "back")
        }

    @Test
    fun `DisplayLeaveConfirmation should replace the actions with staying and leaving when it is displayed`() =
        sharedViewsTest {
            // Given
            val callToActionView = MapBuilderCallToActionView(TextsMother.texts())
            callToActionView.displayExportAndBack(onExported = {}, onBack = {})
            // When
            callToActionView.displayLeaveConfirmation(onLeft = {}, onStayed = {})
            // Then
            callToActionView.actionLabels() shouldBe ("Stay" to "Leave")
        }

    @Test
    fun `DisplayLeaveConfirmation should leave and stay when they are clicked`() =
        sharedViewsTest {
            // Given
            val callToActionView = MapBuilderCallToActionView(TextsMother.texts())
            callToActionView.displayLeaveConfirmation(onLeft = { actions += "leave" }, onStayed = { actions += "stay" })
            addChild(callToActionView)
            // When
            clickPrimaryAction(callToActionView)
            clickSecondaryAction(callToActionView)
            // Then
            actions.shouldContainExactly("leave", "stay")
        }
}
