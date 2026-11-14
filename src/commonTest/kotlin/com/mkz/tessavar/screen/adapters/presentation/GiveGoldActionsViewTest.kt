package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import com.mkz.tessavar.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class GiveGoldActionsViewTest : ViewsForTesting() {
    @Test
    fun `DisplayAmount should write the amount on Give, and disable All at the maximum`() =
        sharedViewsTest {
            // Given
            val actionsView = GiveGoldActionsView(MessagesMother.messages(), onCancelled = {}, onAll = {}, onGiven = {})
            // When
            actionsView.displayAmount(amount = 25, maximum = 120)
            // Then
            actionsView.giveLabel() shouldBe "Give 25 G"
            actionsView.isAllEnabled().shouldBeTrue()
            // When
            actionsView.displayAmount(amount = 120, maximum = 120)
            // Then
            actionsView.giveLabel() shouldBe "Give 120 G"
            actionsView.isAllEnabled().shouldBeFalse()
            actionsView.isAllFaded().shouldBeTrue()
        }

    @Test
    fun `Buttons should be as tall as the finish turn, and call back when they are tapped`() =
        sharedViewsTest {
            // Given
            val calls = mutableListOf<String>()
            val actionsView = GiveGoldActionsView(MessagesMother.messages(), onCancelled = { calls += "cancel" }, onAll = { calls += "all" }, onGiven = { calls += "give" })
            actionsView.displayAmount(amount = 25, maximum = 120)
            addChild(actionsView)
            // When
            clickOn(actionsView.cancelButton)
            clickOn(actionsView.allButton)
            clickOn(actionsView.giveButton)
            // Then
            calls.shouldContainExactly("cancel", "all", "give")
            actionsView.isAsTallAsFinishPlayerTurn().shouldBeTrue()
        }
}
