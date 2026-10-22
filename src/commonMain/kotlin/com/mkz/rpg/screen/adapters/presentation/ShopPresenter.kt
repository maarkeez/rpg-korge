package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemBought
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemSold
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.screen.adapters.presentation.ShopView.Mode
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.unit.adapters.presentation.UnitApi

/**
 * The shop of the merchant the party talks to, where one party member at a time buys or sells. It opens to buy or to
 * sell, with the leader as buyer, and every tile dimmed but the buyer's and the merchant's. Tapping an item displays its
 * name and price with Buy, or Sell, and tapping it again clears the selection, as switching the buyer does. Back is
 * handed to the listener set with [setOnBack], which returns to the merchant's dialog.
 */
class ShopPresenter(
    private val shopView: ShopView,
    private val battlefieldView: BattlefieldView,
    private val partyApi: PartyApi,
    private val inventoryApi: InventoryApi,
    private val lootableItemApi: LootableItemApi,
    private val battleUnitApi: BattleUnitApi,
    private val unitApi: UnitApi,
    private val battlefieldApi: BattlefieldApi,
    private val timeline: BattleSceneTimeline,
    eventBus: EventBus,
) : ShopView.Delegate {
    // What the scene displays. Changed in timeline order, like the views.
    private var merchant: Npc.Dto? = null
    private var partyMemberIds: List<String> = emptyList()
    private var buyerIndex = 0
    private var mode = Mode.BUY
    private var displayedSlots: List<InventoryView.Slot> = emptyList()
    private var selectedSlotIndex: Int? = null
    private var onBack: (() -> Unit)? = null

    /** Whether the scene displays the shop. */
    val isOpen: Boolean get() = merchant != null

    private val subscriptions =
        listOf(
            // After buying, the item stays selected, to buy another one. After selling, the item left.
            eventBus.subscribe<ItemBought> { event -> timeline.play { refresh(event.battleUnitId, keepsSelection = true) } },
            eventBus.subscribe<ItemSold> { event -> timeline.play { refresh(event.battleUnitId, keepsSelection = false) } },
        )

    init {
        shopView.visible = false
        shopView.setDelegate(this)
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    fun setOnBack(listener: () -> Unit) {
        onBack = listener
    }

    /** Displays the shop of the merchant, to buy or to sell, with the leader as buyer. To be played in the timeline. */
    suspend fun open(
        merchant: Npc.Dto,
        mode: Mode,
    ) {
        this.merchant = merchant
        partyMemberIds = partyApi.searchParty()?.battleUnitIds.orEmpty()
        buyerIndex = 0
        this.mode = mode
        displayShop()
        shopView.visible = true
        highlightBuyer()
    }

    /** Hides the shop and restores the tiles. Nothing happens when it is not open. */
    fun close() {
        if (merchant == null) return
        merchant = null
        partyMemberIds = emptyList()
        displayedSlots = emptyList()
        selectedSlotIndex = null
        shopView.visible = false
        battlefieldView.resetTiles()
    }

    private fun buyerId(): String? = partyMemberIds.getOrNull(buyerIndex)

    // Displays the items to buy or to sell and the buyer, clearing the selection.
    private fun displayShop() {
        val merchant = merchant ?: return
        val buyerId = buyerId() ?: return
        val inventory = inventoryApi.searchInventoryByBattleUnitId(buyerId)
        displayedSlots =
            when (mode) {
                Mode.BUY -> merchant.stock.map { lootableItemId -> InventoryView.Slot(lootableItemId, isEquipped = false) }
                Mode.SELL -> {
                    val equippedItems =
                        inventory
                            ?.equippedItems
                            ?.values
                            .orEmpty()
                            .toMutableList()
                    inventory?.storedItems.orEmpty().map { lootableItemId -> InventoryView.Slot(lootableItemId, isEquipped = equippedItems.remove(lootableItemId)) }
                }
            }
        val buyer =
            ShopView.Buyer(
                name = battleUnitApi.searchBattleUnitById(buyerId)?.let { battleUnit -> unitApi.searchUnitById(battleUnit.unitId)?.name }.orEmpty(),
                gold = inventory?.gold ?: 0,
                usedStorageSpace = inventory?.storedItems?.size ?: 0,
                maximumStorageSpace = inventory?.maximumStorageSpace ?: 0,
                partyIndex = buyerIndex,
                partySize = partyMemberIds.size,
            )
        shopView.display(slots = displayedSlots, buyer = buyer)
        clearSelection()
    }

    // Every tile but the buyer's and the merchant's is dimmed, and the battlefield scrolls to the buyer.
    private suspend fun highlightBuyer() {
        val merchant = merchant ?: return
        val buyerPosition = buyerId()?.let { buyerId -> battlefieldApi.searchPosition(buyerId) } ?: return
        battlefieldView.resetTiles()
        battlefieldView.dimTilesExcept(setOf(buyerPosition, PositionDto(row = merchant.row, column = merchant.column)))
        battlefieldView.scrollToTile(buyerPosition.row, buyerPosition.column)
    }

    // The hint tells what tapping an item does, or that the buyer has nothing to sell.
    private fun clearSelection() {
        selectedSlotIndex = null
        shopView.selectSlot(null)
        shopView.displayHint(
            when {
                mode == Mode.BUY -> "Tap an item to buy"
                displayedSlots.isEmpty() -> "Nothing to sell"
                else -> "Tap an item to sell"
            },
        )
    }

    // Displays the name and price of the item with Buy, or Sell, enabled when the buyer can trade it. Tapping the selected
    // item, or an empty slot, clears the selection.
    private fun selectItem(slotIndex: Int) {
        val buyerId = buyerId() ?: return
        if (slotIndex == selectedSlotIndex) return clearSelection()
        val lootableItemId = displayedSlots.getOrNull(slotIndex)?.lootableItemId ?: return clearSelection()
        val lootableItem = lootableItemApi.searchLootableItemById(lootableItemId) ?: return
        selectedSlotIndex = slotIndex
        shopView.selectSlot(slotIndex)
        when (mode) {
            Mode.BUY -> shopView.displayItem(lootableItem.name, lootableItem.price, actionLabel = "Buy", isActionEnabled = inventoryApi.canBuyItem(buyerId, lootableItemId))
            Mode.SELL -> shopView.displayItem(lootableItem.name, lootableItem.sellPrice, actionLabel = "Sell", isActionEnabled = inventoryApi.canSellItem(buyerId, lootableItemId))
        }
    }

    // Buys or sells the selected item, when the buyer can.
    private fun trade() {
        val buyerId = buyerId() ?: return
        val lootableItemId = selectedSlotIndex?.let { slotIndex -> displayedSlots.getOrNull(slotIndex) }?.lootableItemId ?: return
        when (mode) {
            Mode.BUY -> if (inventoryApi.canBuyItem(buyerId, lootableItemId)) inventoryApi.buyItem(buyerId, lootableItemId)
            Mode.SELL -> if (inventoryApi.canSellItem(buyerId, lootableItemId)) inventoryApi.sellItem(buyerId, lootableItemId)
        }
    }

    // Displays the buyer's new gold and bag once it traded.
    private fun refresh(
        battleUnitId: String,
        keepsSelection: Boolean,
    ) {
        if (!isOpen || battleUnitId != buyerId()) return
        val selectedSlot = selectedSlotIndex
        displayShop()
        if (keepsSelection && selectedSlot != null) selectItem(selectedSlot)
    }

    private fun switchBuyer(partyIndex: Int) =
        whenIdle {
            if (partyIndex !in partyMemberIds.indices || partyIndex == buyerIndex) return@whenIdle
            timeline.play {
                buyerIndex = partyIndex
                displayShop()
                highlightBuyer()
            }
        }

    // The player can only act once the scene shows the current state of the shop.
    private fun whenIdle(action: () -> Unit) {
        if (!timeline.isPlaying && isOpen) action()
    }

    // Delegates
    override fun shopSlotSelected(index: Int) = whenIdle { timeline.play { selectItem(index) } }

    override fun shopBuyerStepped(step: Int) = switchBuyer(buyerIndex + step)

    override fun shopActionSelected() = whenIdle { trade() }

    override fun shopBackSelected() = whenIdle { onBack?.invoke() }
}
