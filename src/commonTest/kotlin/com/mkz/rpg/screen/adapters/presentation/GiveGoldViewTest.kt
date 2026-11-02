package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.GiveGoldView.Member
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeStrictlyIncreasing
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class GiveGoldViewTest : ViewsForTesting() {
    // Records what the player does on the panel.
    private class RecordingDelegate : GiveGoldView.Delegate {
        val amounts = mutableListOf<Int>()
        val settledAmounts = mutableListOf<Int>()
        val steps = mutableListOf<Int>()

        override fun amountChanged(amount: Int) {
            amounts += amount
        }

        override fun amountSettled(amount: Int) {
            settledAmounts += amount
        }

        override fun receiverStepped(step: Int) {
            steps += step
        }
    }

    private val knight = Member(name = "Knight", unitId = "knight", gold = 120)
    private val viking = Member(name = "Viking", unitId = "viking", gold = 100)

    private suspend fun giveGoldView(
        amount: Int = 25,
        maximum: Int = 120,
        canStepToPrevious: Boolean = false,
        canStepToNext: Boolean = false,
    ) = GiveGoldView(loadedResourcesLoader(), TextsMother.texts()).apply {
        display(giver = knight, receiver = viking, amount = amount, maximum = maximum, canStepToPrevious = canStepToPrevious, canStepToNext = canStepToNext)
    }

    @Test
    fun `Display should write what the giver and the receiver would carry`() =
        sharedViewsTest {
            // When
            val giveGoldView = giveGoldView(amount = 25)
            // Then
            giveGoldView.giverTexts() shouldBe ("Knight" to "120 G > 95 G")
            giveGoldView.receiverTexts() shouldBe ("Viking" to "100 G > 125 G")
            giveGoldView.amountLabel() shouldBe "25 G"
        }

    @Test
    fun `Display should hide the arrows when there is a single receiver`() =
        sharedViewsTest {
            // When
            val giveGoldView = giveGoldView()
            // Then
            giveGoldView.arrowsVisible().shouldBeFalse()
        }

    @Test
    fun `Display should disable the arrow at each end of the receivers`() =
        sharedViewsTest {
            // When
            val giveGoldView = giveGoldView(canStepToNext = true)
            // Then
            giveGoldView.arrowsVisible().shouldBeTrue()
            giveGoldView.arrowsEnabled() shouldBe (false to true)
            giveGoldView.arrowsFaded() shouldBe (true to false)
        }

    @Test
    fun `Arrows should call the delegate with the step when they are tapped`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val giveGoldView = giveGoldView(canStepToPrevious = true, canStepToNext = true)
            giveGoldView.setDelegate(delegate)
            addChild(giveGoldView)
            // When
            clickPreviousReceiver(giveGoldView)
            clickNextReceiver(giveGoldView)
            // Then
            delegate.steps.shouldContainExactly(-1, 1)
        }

    @Test
    fun `Slider should call the delegate with each whole amount, without going above the maximum, when it is dragged`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val giveGoldView = giveGoldView(amount = 25, maximum = 120)
            giveGoldView.setDelegate(delegate)
            addChild(giveGoldView)
            // When
            dragSlider(giveGoldView, dx = 500.0)
            // Then
            delegate.amounts.size shouldBeGreaterThan 1
            delegate.amounts.shouldBeStrictlyIncreasing()
            delegate.amounts.last() shouldBe 120
            delegate.settledAmounts.shouldContainExactly(120)
            giveGoldView.amountLabel() shouldBe "120 G"
            giveGoldView.receiverTexts().second shouldBe "100 G > 220 G"
        }

    @Test
    fun `Slider should stop at 1 when it is dragged to the start`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val giveGoldView = giveGoldView(amount = 25, maximum = 120)
            giveGoldView.setDelegate(delegate)
            addChild(giveGoldView)
            // When
            dragSlider(giveGoldView, dx = -500.0)
            // Then
            delegate.amounts.last() shouldBe 1
            giveGoldView.amountLabel() shouldBe "1 G"
        }

    @Test
    fun `Minus and plus should change the amount by 1, stopping at 1 and at the maximum`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val giveGoldView = giveGoldView(amount = 1, maximum = 2)
            giveGoldView.setDelegate(delegate)
            addChild(giveGoldView)
            // Then
            giveGoldView.minusButton.enabled.shouldBeFalse()
            // When
            clickPlus(giveGoldView)
            // Then
            giveGoldView.amountLabel() shouldBe "2 G"
            giveGoldView.plusButton.enabled.shouldBeFalse()
            // When
            clickPlus(giveGoldView)
            clickMinus(giveGoldView)
            // Then
            giveGoldView.amountLabel() shouldBe "1 G"
            delegate.amounts.shouldContainExactly(2, 1)
            delegate.settledAmounts.shouldContainExactly(2, 1)
        }

    @Test
    fun `Display should not call the delegate, as only the player changes the amount`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val giveGoldView = giveGoldView(amount = 25)
            giveGoldView.setDelegate(delegate)
            // When
            giveGoldView.display(giver = knight, receiver = viking, amount = 30, maximum = 120, canStepToPrevious = false, canStepToNext = false)
            // Then
            delegate.amounts.shouldBeEmpty()
            giveGoldView.amountLabel() shouldBe "30 G"
        }
}
