package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.InventoryView.Owner
import com.mkz.rpg.screen.adapters.presentation.InventoryView.Slot
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
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
        val steps = mutableListOf<Int>()
        var isClosed = false

        override fun slotSelected(index: Int) {
            selectedSlots += index
        }

        override fun inventoryOwnerStepped(step: Int) {
            steps += step
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
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            // Then
            inventoryView.titleText() shouldBe "Inventory"
            inventoryView.arrowsVisible().shouldBeFalse()
            inventoryView.slotCount() shouldBe 12
            inventoryView.slotBitmaps().all { bitmap -> bitmap == null }.shouldBeTrue()
        }

    @Test
    fun `Display should display the stored items in order followed by the empty slots when is displayed`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            val inventoryView = InventoryView(resourcesLoader, TextsMother.texts())
            // When
            inventoryView.display(listOf(Slot("hammer", isEquipped = false), Slot("health-potion", isEquipped = false)), gold = 0)
            // Then
            inventoryView.slotBitmaps().take(3).shouldContainExactly(
                resourcesLoader.bitmap("lootableItem/hammer.png"),
                resourcesLoader.bitmap("lootableItem/health-potion.png"),
                null,
            )
        }

    @Test
    fun `Display should display the gold on the title line when is displayed`() =
        sharedViewsTest {
            // Given
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            // When
            inventoryView.display(listOf(Slot("hammer", isEquipped = false)), gold = 120)
            // Then
            inventoryView.titleText() shouldBe "Inventory"
            inventoryView.goldText() shouldBe "120 G"
        }

    @Test
    fun `Display should frame the equipped items in gold when is displayed`() =
        sharedViewsTest {
            // Given
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            // When
            inventoryView.display(listOf(Slot("health-potion", isEquipped = false), Slot("hammer", isEquipped = true)), gold = 0)
            // Then
            inventoryView.equippedSlotIndexes().shouldContainExactly(1)
        }

    @Test
    fun `Display should clear the selected slot when is displayed again`() =
        sharedViewsTest {
            // Given
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            inventoryView.display(listOf(Slot("hammer", isEquipped = false)), gold = 0)
            inventoryView.selectSlot(0)
            // When
            inventoryView.display(listOf(Slot("hammer", isEquipped = true)), gold = 0)
            // Then
            inventoryView.selectedSlotIndexes().shouldBeEmpty()
        }

    @Test
    fun `SelectSlot should frame only the selected slot when another slot was selected`() =
        sharedViewsTest {
            // Given
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            inventoryView.display(listOf(Slot("hammer", isEquipped = false), Slot("health-potion", isEquipped = false)), gold = 0)
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
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            inventoryView.setDelegate(delegate)
            inventoryView.display(listOf(Slot("hammer", isEquipped = false), Slot("health-potion", isEquipped = false)), gold = 0)
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
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            inventoryView.setDelegate(delegate)
            addChild(inventoryView)
            // When
            clickClose(inventoryView)
            // Then
            delegate.isClosed.shouldBeTrue()
        }

    @Test
    fun `Display should name the owner between both arrows enabled, the gold before the next one`() =
        sharedViewsTest {
            // Given
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            addChild(inventoryView)
            // When
            inventoryView.display(emptyList(), gold = 20, owner = Owner(name = "Viking", partyIndex = 1, partySize = 3))
            // Then
            inventoryView.titleText() shouldBe "Viking"
            inventoryView.goldText() shouldBe "20 G"
            inventoryView.arrowsVisible().shouldBeTrue()
            inventoryView.arrowsEnabled() shouldBe (true to true)
            inventoryView.arrowsFaded() shouldBe (false to false)
            inventoryView.isTitleLaidOut().shouldBeTrue()
        }

    @Test
    fun `Display should disable the arrows at the ends of the party`() =
        sharedViewsTest {
            // Given
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            // When
            inventoryView.display(emptyList(), gold = 0, owner = Owner(name = "Knight", partyIndex = 0, partySize = 1))
            // Then
            inventoryView.arrowsEnabled() shouldBe (false to false)
            inventoryView.arrowsFaded() shouldBe (true to true)
        }

    @Test
    fun `Display should hide the arrows and display the inventory title again when there is no owner`() =
        sharedViewsTest {
            // Given
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            inventoryView.display(emptyList(), gold = 0, owner = Owner(name = "Knight", partyIndex = 0, partySize = 2))
            // When
            inventoryView.display(emptyList(), gold = 0)
            // Then
            inventoryView.titleText() shouldBe "Inventory"
            inventoryView.arrowsVisible().shouldBeFalse()
        }

    @Test
    fun `ArrowClick should notify the delegate of the step when an arrow is clicked`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            inventoryView.setDelegate(delegate)
            inventoryView.display(emptyList(), gold = 0, owner = Owner(name = "Viking", partyIndex = 1, partySize = 3))
            addChild(inventoryView)
            // When
            clickPreviousOwner(inventoryView)
            clickNextOwner(inventoryView)
            // Then
            delegate.steps.shouldContainExactly(-1, 1)
        }

    @Test
    fun `Swipe should notify the delegate of the direction, without selecting a slot`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            inventoryView.setDelegate(delegate)
            inventoryView.display(listOf(Slot("hammer", isEquipped = false)), gold = 0, owner = Owner(name = "Viking", partyIndex = 1, partySize = 3))
            addChild(inventoryView)
            // When
            swipeInventory(inventoryView, dx = -100.0)
            swipeInventory(inventoryView, dx = 100.0)
            // Then
            delegate.steps.shouldContainExactly(-1, 1)
            delegate.selectedSlots.shouldBeEmpty()
        }

    @Test
    fun `Swipe should not notify the delegate when the drag is too short`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val inventoryView = InventoryView(loadedResourcesLoader(), TextsMother.texts())
            inventoryView.setDelegate(delegate)
            addChild(inventoryView)
            // When
            swipeInventory(inventoryView, dx = 20.0)
            // Then
            delegate.steps.shouldBeEmpty()
        }
}
