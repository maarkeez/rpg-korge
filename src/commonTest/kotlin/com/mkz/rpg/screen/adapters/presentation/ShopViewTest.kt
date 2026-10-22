package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.InventoryView.Slot
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class ShopViewTest : ViewsForTesting() {
    // Records what the player does on the shop.
    private class RecordingDelegate : ShopView.Delegate {
        val selectedSlots = mutableListOf<Int>()
        val steps = mutableListOf<Int>()
        var actions = 0
        var isBackSelected = false

        override fun shopSlotSelected(index: Int) {
            selectedSlots += index
        }

        override fun shopBuyerStepped(step: Int) {
            steps += step
        }

        override fun shopActionSelected() {
            actions++
        }

        override fun shopBackSelected() {
            isBackSelected = true
        }
    }

    // The second of three party members, with 20 G and 11 of its 12 storage spaces used.
    private val knight = ShopView.Buyer(name = "Knight", gold = 20, usedStorageSpace = 11, maximumStorageSpace = 12, partyIndex = 1, partySize = 3)

    private fun ShopView.displayMerchant(
        slots: List<Slot> = listOf(Slot("mana-potion", isEquipped = false), Slot("hammer", isEquipped = false)),
        buyer: ShopView.Buyer = knight,
    ) = display(slots = slots, buyer = buyer)

    @Test
    fun `Init should display 12 empty slots, under the header and above the footer, without Buy nor Sell`() =
        sharedViewsTest {
            // When
            val shopView = ShopView(loadedResourcesLoader())
            // Then
            shopView.slotCount() shouldBe 12
            shopView.slotBitmaps().all { bitmap -> bitmap == null }.shouldBeTrue()
            shopView.isLaidOut().shouldBeTrue()
            shopView.action().shouldBeNull()
        }

    @Test
    fun `Display should display the items in order followed by the empty slots`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            val shopView = ShopView(resourcesLoader)
            // When
            shopView.displayMerchant()
            // Then
            shopView.slotBitmaps().take(3).shouldContainExactly(
                resourcesLoader.bitmap("lootableItem/mana-potion.png"),
                resourcesLoader.bitmap("lootableItem/hammer.png"),
                null,
            )
        }

    @Test
    fun `Display should frame the equipped items in gold`() =
        sharedViewsTest {
            // Given
            val shopView = ShopView(loadedResourcesLoader())
            // When
            shopView.displayMerchant(slots = listOf(Slot("health-potion", isEquipped = false), Slot("hammer", isEquipped = true)))
            // Then
            shopView.equippedSlotIndexes().shouldContainExactly(1)
        }

    @Test
    fun `Display should display the buyer's name, gold and bag, between both arrows enabled`() =
        sharedViewsTest {
            // Given
            val shopView = ShopView(loadedResourcesLoader())
            // When
            shopView.displayMerchant()
            // Then
            shopView.buyerText() shouldBe "Knight · 20 G · Bag 11/12"
            shopView.arrowsEnabled() shouldBe (true to true)
        }

    @Test
    fun `Display should display the bag is full, and disable the arrows at the ends of the party`() =
        sharedViewsTest {
            // Given
            val shopView = ShopView(loadedResourcesLoader())
            // When
            shopView.displayMerchant(buyer = knight.copy(usedStorageSpace = 12, partyIndex = 0, partySize = 2))
            // Then
            shopView.buyerText() shouldBe "Knight · 20 G · Bag full"
            shopView.arrowsEnabled() shouldBe (false to true)
            // When
            shopView.displayMerchant(buyer = knight.copy(partyIndex = 1, partySize = 2))
            // Then
            shopView.arrowsEnabled() shouldBe (true to false)
        }

    @Test
    fun `SelectSlot should frame only the selected slot, or none when no slot is selected`() =
        sharedViewsTest {
            // Given
            val shopView = ShopView(loadedResourcesLoader())
            shopView.displayMerchant()
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
    fun `DisplayItem should display the item's name, price and action, and DisplayHint the hint without action`() =
        sharedViewsTest {
            // Given
            val shopView = ShopView(loadedResourcesLoader())
            // When
            shopView.displayItem(itemName = "Hammer", itemPrice = 50, actionLabel = "Buy", isActionEnabled = false)
            // Then
            shopView.footerText() shouldBe "Hammer"
            shopView.footerPrice() shouldBe "50 G"
            shopView.action() shouldBe ("Buy" to false)
            // When
            shopView.displayHint("Tap an item to buy")
            // Then
            shopView.footerText() shouldBe "Tap an item to buy"
            shopView.footerPrice() shouldBe ""
            shopView.action().shouldBeNull()
        }

    private suspend fun ViewsForTesting.newShopView(delegate: RecordingDelegate): ShopView {
        val shopView = ShopView(loadedResourcesLoader())
        shopView.setDelegate(delegate)
        shopView.displayMerchant()
        views.stage.addChild(shopView)
        return shopView
    }

    @Test
    fun `Click should notify the delegate of the selected slot, the arrows, the action and Back`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val shopView = newShopView(delegate)
            shopView.displayItem(itemName = "Hammer", itemPrice = 50, actionLabel = "Buy", isActionEnabled = true)
            // When
            clickSlot(shopView, 1)
            clickPreviousBuyer(shopView)
            clickNextBuyer(shopView)
            clickAction(shopView)
            clickBack(shopView)
            // Then
            delegate.selectedSlots.shouldContainExactly(1)
            delegate.steps.shouldContainExactly(-1, 1)
            delegate.actions shouldBe 1
            delegate.isBackSelected.shouldBeTrue()
        }

    @Test
    fun `Swipe should notify the delegate of the direction, without selecting a slot`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val shopView = newShopView(delegate)
            // When
            swipeShop(shopView, dx = -100.0)
            swipeShop(shopView, dx = 100.0)
            // Then
            delegate.steps.shouldContainExactly(-1, 1)
            delegate.selectedSlots.shouldBeEmpty()
        }

    @Test
    fun `Swipe should not notify the delegate when the drag is too short`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val shopView = newShopView(delegate)
            // When
            swipeShop(shopView, dx = 20.0)
            // Then
            delegate.steps.shouldBeEmpty()
        }
}
