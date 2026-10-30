package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class SaveSlotsViewTest : ViewsForTesting() {
    private val selections = mutableListOf<String>()

    private fun newSaveSlotsView(slots: List<SaveSlotsView.Slot>) =
        SaveSlotsView(TextsMother.texts()).apply {
            setDelegate(
                object : SaveSlotsView.Delegate {
                    override fun saveSlotSelected(number: Int) {
                        selections += "slot $number"
                    }

                    override fun saveSlotDeleteSelected(number: Int) {
                        selections += "delete $number"
                    }

                    override fun saveSlotsBackSelected() {
                        selections += "back"
                    }
                },
            )
            display(title = "Save the game", slots = slots)
        }

    private val slots =
        listOf(
            SaveSlotsView.Slot(number = 1, label = "Empty", isEnabled = false, isDeletable = false),
            SaveSlotsView.Slot(number = 2, label = "2026-10-30 18:42 · Knight · party of 3", isEnabled = true, isDeletable = true),
            SaveSlotsView.Slot(number = 3, label = "Incompatible save", isEnabled = false, isDeletable = true),
        )

    @Test
    fun `SaveSlotsView should display the title and what each slot holds, from the first, when it is displayed`() =
        sharedViewsTest {
            // When
            val saveSlotsView = newSaveSlotsView(slots)
            // Then
            saveSlotsView.title() shouldBe "Save the game"
            saveSlotsView.slotLabels().shouldContainExactly("Empty", "2026-10-30 18:42 · Knight · party of 3", "Incompatible save")
            saveSlotsView.enabledSlots().shouldContainExactly(false, true, false)
            saveSlotsView.deletableSlots().shouldContainExactly(2, 3)
            saveSlotsView.isLaidOut().shouldBeTrue()
        }

    @Test
    fun `ClickSlot should call the delegate with its number when an enabled slot is tapped, and nothing for a faded one`() =
        sharedViewsTest {
            // Given
            val saveSlotsView = newSaveSlotsView(slots)
            addChild(saveSlotsView)
            // When
            clickSlot(saveSlotsView, 1)
            clickSlot(saveSlotsView, 2)
            clickDelete(saveSlotsView, 3)
            clickBack(saveSlotsView)
            // Then
            selections.shouldContainExactly("slot 2", "delete 3", "back")
        }
}
