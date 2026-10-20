package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.InventoryView.Slot
import com.mkz.rpg.screen.adapters.presentation.ShopView.Tab
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
        val selectedTabs = mutableListOf<Tab>()
        val swipes = mutableListOf<Int>()
        val selectedBuyers = mutableListOf<Int>()
        var isClosed = false

        override fun shopSlotSelected(index: Int) {
            selectedSlots += index
        }

        override fun shopTabSelected(tab: Tab) {
            selectedTabs += tab
        }

        override fun shopBuyerSwiped(step: Int) {
            swipes += step
        }

        override fun shopBuyerSelected(partyIndex: Int) {
            selectedBuyers += partyIndex
        }

        override fun shopClosed() {
            isClosed = true
        }
    }

    // The second of three party members, with 20 G and 11 of its 12 storage spaces used.
    private val knight = ShopView.Buyer(name = "Knight", gold = 20, usedStorageSpace = 11, maximumStorageSpace = 12, partyIndex = 1, partySize = 3)

    private fun ShopView.displayMerchant(
        tab: Tab = Tab.BUY,
        slots: List<Slot> = listOf(Slot("mana-potion", isEquipped = false), Slot("hammer", isEquipped = false)),
        buyer: ShopView.Buyer = knight,
    ) = display(merchantName = "Merchant", tab = tab, slots = slots, buyer = buyer)

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
    fun `Display should display the merchant name and the items in order followed by the empty slots`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            val shopView = ShopView(resourcesLoader)
            // When
            shopView.displayMerchant()
            // Then
            shopView.titleText() shouldBe "Merchant"
            shopView.slotBitmaps().take(3).shouldContainExactly(
                resourcesLoader.bitmap("lootableItem/mana-potion.png"),
                resourcesLoader.bitmap("lootableItem/hammer.png"),
                null,
            )
        }

    @Test
    fun `Display should highlight the active tab`() =
        sharedViewsTest {
            // Given
            val shopView = ShopView(loadedResourcesLoader())
            // When
            shopView.displayMerchant(tab = Tab.SELL)
            // Then
            shopView.activeTab() shouldBe Tab.SELL
            shopView.tabColors().second shouldBe ShopView.ACTIVE_TAB_COLOR
            (shopView.tabColors().first == ShopView.ACTIVE_TAB_COLOR) shouldBe false
        }

    @Test
    fun `Display should frame the equipped items in gold`() =
        sharedViewsTest {
            // Given
            val shopView = ShopView(loadedResourcesLoader())
            // When
            shopView.displayMerchant(tab = Tab.SELL, slots = listOf(Slot("health-potion", isEquipped = false), Slot("hammer", isEquipped = true)))
            // Then
            shopView.equippedSlotIndexes().shouldContainExactly(1)
        }

    @Test
    fun `Display should display the buyer's name, gold and bag, and one dot per party member, the buyer's one filled`() =
        sharedViewsTest {
            // Given
            val shopView = ShopView(loadedResourcesLoader())
            // When
            shopView.displayMerchant()
            // Then
            shopView.buyerText() shouldBe "Knight · 20 G · Bag 11/12"
            shopView.dotColors().shouldContainExactly(ShopView.PARTY_MEMBER_DOT_COLOR, ShopView.BUYER_DOT_COLOR, ShopView.PARTY_MEMBER_DOT_COLOR)
        }

    @Test
    fun `Display should display the bag is full when every storage space is used`() =
        sharedViewsTest {
            // Given
            val shopView = ShopView(loadedResourcesLoader())
            // When
            shopView.displayMerchant(buyer = knight.copy(usedStorageSpace = 12, partyIndex = 0, partySize = 1))
            // Then
            shopView.buyerText() shouldBe "Knight · 20 G · Bag full"
            shopView.dotColors().shouldContainExactly(ShopView.BUYER_DOT_COLOR)
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

    private suspend fun ViewsForTesting.newShopView(delegate: RecordingDelegate): ShopView {
        val shopView = ShopView(loadedResourcesLoader())
        shopView.setDelegate(delegate)
        shopView.displayMerchant()
        views.stage.addChild(shopView)
        return shopView
    }

    @Test
    fun `Click should notify the delegate of the selected slot, tab and party member, and when the shop is closed`() =
        sharedViewsTest {
            // Given
            val delegate = RecordingDelegate()
            val shopView = newShopView(delegate)
            // When
            clickSlot(shopView, 1)
            clickSellTab(shopView)
            clickBuyTab(shopView)
            clickDot(shopView, 2)
            clickClose(shopView)
            // Then
            delegate.selectedSlots.shouldContainExactly(1)
            delegate.selectedTabs.shouldContainExactly(Tab.SELL, Tab.BUY)
            delegate.selectedBuyers.shouldContainExactly(2)
            delegate.isClosed.shouldBeTrue()
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
            delegate.swipes.shouldContainExactly(-1, 1)
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
            delegate.swipes.shouldBeEmpty()
        }
}
