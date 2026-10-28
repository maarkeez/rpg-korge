package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.storybook.domain.Storybook.Dto.ActionDto
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class ActionsViewTest : ViewsForTesting() {
    private var clears = 0

    private fun newActionsView() =
        ActionsView().apply {
            setDelegate(
                object : ActionsView.Delegate {
                    override fun actionsCleared() {
                        clears++
                    }
                },
            )
        }

    @Test
    fun `ActionsView should display its title and no action when it is displayed`() =
        sharedViewsTest {
            // When
            val actionsView = newActionsView()
            // Then
            actionsView.titleText() shouldBe "Actions (0)"
            actionsView.actionTexts().shouldBeEmpty()
            actionsView.emptyHintText() shouldBe "Tap the view in the phone: the callbacks it makes, if any, are listed here."
        }

    @Test
    fun `Display should list the actions, the newest first, each with its arguments, and count them in the title`() =
        sharedViewsTest {
            // Given
            val actionsView = newActionsView()
            addChild(actionsView)
            // When
            actionsView.display(
                listOf(
                    ActionDto(name = "shopBuyerStepped", arguments = listOf("-1")),
                    ActionDto(name = "shopBackSelected", arguments = emptyList()),
                    ActionDto(name = "itemMoved", arguments = listOf("2", "5")),
                ),
            )
            // Then
            actionsView.titleText() shouldBe "Actions (3)"
            actionsView.actionTexts().shouldContainExactly("shopBuyerStepped(-1)", "shopBackSelected()", "itemMoved(2, 5)")
            actionsView.isListedTopDown().shouldBeTrue()
            actionsView.emptyHintText().shouldBeNull()
        }

    @Test
    fun `Display should list only the newest actions that fit when there are more`() =
        sharedViewsTest {
            // Given
            val actionsView = newActionsView()
            addChild(actionsView)
            // When
            actionsView.display((9 downTo 0).map { index -> ActionDto(name = "shopSlotSelected", arguments = listOf(index.toString())) })
            // Then
            actionsView.titleText() shouldBe "Actions (10)"
            actionsView.actionTexts().shouldContainExactly((9 downTo 5).map { index -> "shopSlotSelected($index)" })
            actionsView.isListedTopDown().shouldBeTrue()
        }

    @Test
    fun `Display should show the hint again when the actions are emptied`() =
        sharedViewsTest {
            // Given
            val actionsView = newActionsView()
            addChild(actionsView)
            actionsView.display(listOf(ActionDto(name = "shopBackSelected", arguments = emptyList())))
            // When
            actionsView.display(emptyList())
            // Then
            actionsView.actionTexts().shouldBeEmpty()
            actionsView.emptyHintText() shouldBe ActionsView.EMPTY_HINT
        }

    @Test
    fun `ClickClear should ask to empty the actions when it is clicked`() =
        sharedViewsTest {
            // Given
            val actionsView = newActionsView()
            addChild(actionsView)
            actionsView.display(listOf(ActionDto(name = "shopBackSelected", arguments = emptyList())))
            // When
            clickClear(actionsView)
            // Then
            clears shouldBe 1
        }
}
