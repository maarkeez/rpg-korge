package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.InventoryView.Slot
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class InventoryViewTest : ViewsForTesting() {
    // Records what the player does on the inventory.
    private class RecordingDelegate : InventoryView.Delegate {
        val selectedSlots = mutableListOf<Int>()
        var isClosed = false

        override fun slotSelected(index: Int) {
            selectedSlots += index
        }

        override fun inventoryClosed() {
            isClosed = true
        }
    }

    @Test
    fun `Init should display the inventory title and 12 empty slots when is initialized`() =
        sharedViewsTest {
            // Given
            // When
            val inventoryView = InventoryView(loadedResourcesLoader())
            // Then
            inventoryView.titleText() shouldBe "Inventory"
            inventoryView.slotCount() shouldBe 12
            inventoryView.slotBitmaps().all { bitmap -> bitmap == null }.shouldBeTrue()
        }

    @Test
    fun `Display should display the stored items in order followed by the empty slots when is displayed`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            val inventoryView = InventoryView(resourcesLoader)
            // When
            inventoryView.display(listOf(Slot("hammer", isEquipped = false), Slot("health-potion", isEquipped = false)))
            // Then
            inventoryView.slotBitmaps().take(3).shouldContainExactly(
                resourcesLoader.bitmap("lootableItem/hammer.png"),
                resourcesLoader.bitmap("lootableItem/health-potion.png"),
                null,
            )
        }

    @Test
    fun `Display should frame the equipped items in gold when is displayed`() =
        sharedViewsTest {
            // Given
            val inventoryView = InventoryView(loadedResourcesLoader())
            // When
            inventoryView.display(listOf(Slot("health-potion", isEquipped = false), Slot("hammer", isEquipped = true)))
            // Then
            inventoryView.equippedSlotIndexes().shouldContainExactly(1)
        }

    @Test
    fun `Display should clear the selected slot when is displayed again`() =
        sharedViewsTest {
            // Given
            val inventoryView = InventoryView(loadedResourcesLoader())
            inventoryView.display(listOf(Slot("hammer", isEquipped = false)))
            inventoryView.selectSlot(0)
            // When
            inventoryView.display(listOf(Slot("hammer", isEquipped = true)))
            // Then
            inventoryView.selectedSlotIndexes().shouldBeEmpty()
        }

    @Test
    fun `SelectSlot should frame only the selected slot when another slot was selected`() =
        sharedViewsTest {
            // Given
            val inventoryView = InventoryView(loadedResourcesLoader())
            inventoryView.display(listOf(Slot("hammer", isEquipped = false), Slot("health-potion", isEquipped = false)))
            inventoryView.selectSlot(0)
            // When
            inventoryView.selectSlot(1)
            // Then
            inventoryView.selectedSlotIndexes().shouldContainExactly(1)
        }

    @Test
    fun `SlotClick should notify the delegate with the slot when a slot is clicked`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val inventoryView = InventoryView(loadedResourcesLoader())
            inventoryView.setDelegate(delegate)
            inventoryView.display(listOf(Slot("hammer", isEquipped = false), Slot("health-potion", isEquipped = false)))
            addChild(inventoryView)
            // When
            clickSlot(inventoryView, 1)
            // Then
            delegate.selectedSlots.shouldContainExactly(1)
        }

    @Test
    fun `CloseClick should notify the delegate when close is clicked`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val inventoryView = InventoryView(loadedResourcesLoader())
            inventoryView.setDelegate(delegate)
            addChild(inventoryView)
            // When
            clickClose(inventoryView)
            // Then
            delegate.isClosed.shouldBeTrue()
        }
}
