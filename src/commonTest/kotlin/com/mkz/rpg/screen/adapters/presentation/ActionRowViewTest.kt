package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isFaded
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.math.abs
import kotlin.test.Test

class ActionRowViewTest : ViewsForTesting() {
    private val rowWidth = 390.0
    private val spacing = 10.0

    @Test
    fun `ActionRowView should fill the row when it has one action`() =
        sharedViewsTest {
            // When
            val rowView = ActionRowView(listOf(ActionRowView.Action("Back") {}))
            // Then
            rowView.buttons.single().x shouldBe 0.0
            rowView.buttons.single().width shouldBe rowWidth
        }

    @Test
    fun `ActionRowView should keep the first button on the left when it has several actions`() =
        sharedViewsTest {
            // When
            val rowView = ActionRowView(listOf(ActionRowView.Action("Back") {}, ActionRowView.Action("Abandon") {}))
            // Then
            rowView.buttons.map { button -> button.x }.shouldContainExactly(0.0, 200.0)
            rowView.buttons.map { button -> button.width }.shouldContainExactly(190.0, 190.0)
        }

    @Test
    fun `ActionRowView should share the width in thirds when it has three actions`() =
        sharedViewsTest {
            // When
            val rowView = ActionRowView(listOf("Buy", "Sell", "Leave").map { label -> ActionRowView.Action(label) {} })
            // Then
            val last = rowView.buttons.last()
            abs(last.x + last.width - rowWidth) shouldBeLessThan 0.001
            abs(rowView.buttons[1].x - rowView.buttons[0].width - spacing) shouldBeLessThan 0.001
        }

    @Test
    fun `ActionRowView should display a disabled action faded, not calling it when it is tapped`() =
        sharedViewsTest {
            // Given
            val selected = mutableListOf<String>()
            val rowView = ActionRowView(listOf(ActionRowView.Action("Not enough gold", isEnabled = false) { selected += "action" }))
            addChild(rowView)
            // When
            clickOn(rowView.buttons.single())
            // Then
            rowView.buttons
                .single()
                .enabled
                .shouldBeFalse()
            (rowView.buttons.single().isFaded()) shouldBe true
            selected.shouldBeEmpty()
        }
}
