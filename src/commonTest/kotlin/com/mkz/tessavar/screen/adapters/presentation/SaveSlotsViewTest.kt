package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class SaveSlotsViewTest : ViewsForTesting() {
    private val selections = mutableListOf<String>()

    private fun newSaveSlotsView(slots: List<SaveSlotsView.Slot>) =
        SaveSlotsView(MessagesMother.messages()).apply {
            setDelegate(
                object : SaveSlotsView.Delegate {
                    override fun saveSlotSelected(number: Int) {
                        selections += "slot $number"
                    }

                    override fun deleteSaveSelected(number: Int) {
                        selections += "delete $number"
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
            // Then
            selections.shouldContainExactly("slot 2", "delete 3")
        }

    @Test
    fun `display should spread the rows when they are 3`() =
        sharedViewsTest {
            // When
            val saveSlotsView = newSaveSlotsView(slots)
            // Then
            saveSlotsView.rowTops().shouldContainExactly(72.0, 152.0, 232.0)
            saveSlotsView.rowsBottom() shouldBe 280.0
        }
}
