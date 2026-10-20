package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class ShopViewTest : ViewsForTesting() {
    // Records what the player does on the shop.
    private class RecordingDelegate : ShopView.Delegate {
        val selectedSlots = mutableListOf<Int>()
        var isClosed = false

        override fun shopSlotSelected(index: Int) {
            selectedSlots += index
        }

        override fun shopClosed() {
            isClosed = true
        }
    }

    @Test
    fun `Init should display 12 empty slots when is initialized`() =
        sharedViewsTest {
            // When
            val shopView = ShopView(loadedResourcesLoader())
            // Then
            shopView.slotCount() shouldBe 12
            shopView.slotBitmaps().all { bitmap -> bitmap == null }.shouldBeTrue()
        }

    @Test
    fun `Display should display the merchant name and the items it sells in order followed by the empty slots`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            val shopView = ShopView(resourcesLoader)
            // When
            shopView.display(merchantName = "Merchant", lootableItemIds = listOf("mana-potion", "hammer"))
            // Then
            shopView.titleText() shouldBe "Merchant"
            shopView.slotBitmaps().take(3).shouldContainExactly(
                resourcesLoader.bitmap("lootableItem/mana-potion.png"),
                resourcesLoader.bitmap("lootableItem/hammer.png"),
                null,
            )
            shopView
                .slotBitmaps()
                .drop(2)
                .all { bitmap -> bitmap == null }
                .shouldBeTrue()
        }

    @Test
    fun `Display should clear the selected slot when is displayed again`() =
        sharedViewsTest {
            // Given
            val shopView = ShopView(loadedResourcesLoader())
            shopView.display(merchantName = "Merchant", lootableItemIds = listOf("hammer"))
            shopView.selectSlot(0)
            // When
            shopView.display(merchantName = "Merchant", lootableItemIds = listOf("hammer"))
            // Then
            shopView.selectedSlotIndexes().shouldBeEmpty()
        }

    @Test
    fun `SelectSlot should frame only the selected slot, or none when no slot is selected`() =
        sharedViewsTest {
            // Given
            val shopView = ShopView(loadedResourcesLoader())
            shopView.display(merchantName = "Merchant", lootableItemIds = listOf("hammer", "health-potion"))
            shopView.selectSlot(0)
            // When
            shopView.selectSlot(1)
            // Then
            shopView.selectedSlotIndexes().shouldContainExactly(1)
            // When
            shopView.selectSlot(null)
            // Then
            shopView.selectedSlotIndexes().shouldBeEmpty()
        }

    @Test
    fun `SlotClick should notify the delegate with the slot when a slot is clicked`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val shopView = ShopView(loadedResourcesLoader())
            shopView.setDelegate(delegate)
            shopView.display(merchantName = "Merchant", lootableItemIds = listOf("hammer", "health-potion"))
            addChild(shopView)
            // When
            clickSlot(shopView, 1)
            // Then
            delegate.selectedSlots.shouldContainExactly(1)
        }

    @Test
    fun `CloseClick should notify the delegate when close is clicked`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val shopView = ShopView(loadedResourcesLoader())
            shopView.setDelegate(delegate)
            addChild(shopView)
            // When
            clickClose(shopView)
            // Then
            delegate.isClosed.shouldBeTrue()
        }
}
