package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.inventory.domain.Inventory.Dto.TradeRefusalDto
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemBought
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemSold
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.screen.adapters.presentation.ShopView.Mode
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe

/**
 * The shop of the merchant the party talks to, where one party member at a time buys or sells. It opens to buy or to
 * sell, with the leader as buyer, and every tile dimmed but the buyer's and the merchant's. Tapping an item displays its
 * name and price with Buy, or Sell, or why the buyer can not trade it, and tapping it again clears the selection, as
 * switching the buyer does. The items the buyer can not trade are dimmed. Back is
 * handed to the listener set with [setOnBack], which returns to the merchant's dialog.
 */
class ShopPresenter(
    private val shopView: ShopView,
    private val battlefieldView: BattlefieldView,
    private val partyApi: PartyApi,
    private val inventoryApi: InventoryApi,
    private val lootableItemApi: LootableItemApi,
    private val battleUnitApi: BattleUnitApi,
    private val battlefieldApi: BattlefieldApi,
    private val texts: Texts,
    private val timeline: BattleSceneTimeline,
    eventBus: EventBus,
) : ShopView.Delegate {
    // What the scene displays. Changed in timeline order, like the views.
    private var merchant: Npc.Dto? = null
    private var partyMemberIds: List<String> = emptyList()
    private var buyerIndex = 0
    private var mode = Mode.BUY
    private var displayedSlots: List<ShopView.Slot> = emptyList()
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

    // Displays the items to buy or to sell, dimming the ones the buyer can not trade, and the buyer, clearing the selection.
    private fun displayShop() {
        val merchant = merchant ?: return
        val buyerId = buyerId() ?: return
        val inventory = inventoryApi.searchInventoryByBattleUnitId(buyerId)
        displayedSlots =
            when (mode) {
                Mode.BUY -> merchant.stock.map { lootableItemId -> ShopView.Slot(lootableItemId, isEquipped = false, isTradeable = refusal(buyerId, lootableItemId) == null) }
                Mode.SELL -> {
                    val equippedItems =
                        inventory
                            ?.equippedItems
                            ?.values
                            .orEmpty()
                            .toMutableList()
                    inventory?.storedItems.orEmpty().map { lootableItemId ->
                        ShopView.Slot(lootableItemId, isEquipped = equippedItems.remove(lootableItemId), isTradeable = refusal(buyerId, lootableItemId) == null)
                    }
                }
            }
        val buyer =
            ShopView.Buyer(
                name = battleUnitApi.searchBattleUnitById(buyerId)?.let { battleUnit -> texts.unitName(battleUnit.unitId) }.orEmpty(),
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
                mode == Mode.BUY -> texts[UiText.SHOP_TAP_TO_BUY]
                displayedSlots.isEmpty() -> texts[UiText.SHOP_NOTHING_TO_SELL]
                else -> texts[UiText.SHOP_TAP_TO_SELL]
            },
        )
    }

    // Displays the name and price of the item with Buy, or Sell, or why the buyer can not trade it, disabled. Tapping the
    // selected item, or an empty slot, clears the selection.
    private fun selectItem(slotIndex: Int) {
        val buyerId = buyerId() ?: return
        if (slotIndex == selectedSlotIndex) return clearSelection()
        val lootableItemId = displayedSlots.getOrNull(slotIndex)?.lootableItemId ?: return clearSelection()
        val lootableItem = lootableItemApi.searchLootableItemById(lootableItemId) ?: return
        val (price, actionLabel) =
            when (mode) {
                Mode.BUY -> lootableItem.price to texts[UiText.SHOP_BUY]
                Mode.SELL -> (lootableItemApi.searchSellPrice(lootableItemId) ?: return) to texts[UiText.SHOP_SELL]
            }
        selectedSlotIndex = slotIndex
        shopView.selectSlot(slotIndex)
        val refusal = refusal(buyerId, lootableItemId)
        shopView.displayItem(texts.lootableItemName(lootableItem.id), price, actionLabel = refusal?.let(::refusalText) ?: actionLabel, isActionEnabled = refusal == null)
    }

    private fun refusal(
        buyerId: String,
        lootableItemId: String,
    ): TradeRefusalDto? =
        when (mode) {
            Mode.BUY -> inventoryApi.searchBuyRefusal(buyerId, lootableItemId)
            Mode.SELL -> inventoryApi.searchSellRefusal(buyerId, lootableItemId)
        }

    // The refusals the shop can meet. The others (e.g.: no longer trading) leave Buy or Sell disabled.
    private fun refusalText(refusal: TradeRefusalDto): String? =
        when (refusal) {
            TradeRefusalDto.NOT_ENOUGH_GOLD -> texts[UiText.SHOP_NOT_ENOUGH_GOLD]
            TradeRefusalDto.BAG_FULL -> texts[UiText.SHOP_NO_ROOM]
            TradeRefusalDto.EQUIPPED -> texts[UiText.SHOP_EQUIPPED]
            TradeRefusalDto.GOLD_FULL -> texts[UiText.SHOP_GOLD_FULL]
            TradeRefusalDto.NOT_TRADING, TradeRefusalDto.NOT_SOLD_BY_THE_MERCHANT, TradeRefusalDto.NOT_STORED -> null
        }

    // Buys or sells the selected item, when the buyer can.
    private fun trade() {
        val buyerId = buyerId() ?: return
        val lootableItemId = selectedSlotIndex?.let { slotIndex -> displayedSlots.getOrNull(slotIndex) }?.lootableItemId ?: return
        if (refusal(buyerId, lootableItemId) != null) return
        when (mode) {
            Mode.BUY -> inventoryApi.buyItem(buyerId, lootableItemId)
            Mode.SELL -> inventoryApi.sellItem(buyerId, lootableItemId)
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
