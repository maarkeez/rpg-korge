package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.inventory.domain.Inventory.Dto.GiveRefusalDto
import com.mkz.rpg.inventory.domain.InventoryEvent
import com.mkz.rpg.inventory.domain.InventoryEvent.InventoryFull
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemGiven
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemReceived
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto.CONSUMABLE
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto.EQUIPPABLE
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.PartyEvent.ExplorationPaused
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import kotlinx.coroutines.CompletableDeferred

/**
 * Displays the inventory of the selected battle unit behind the bag, lets the player act on its items, and asks the
 * player what to do with an item found while the inventory is full.
 *
 * While exploring, an item can be given to another party member: Give dims every tile but the ones of the members that
 * can receive it, and tapping one of them gives the item. Until then, every other tap is ignored, but Cancel's.
 */
class InventoryPresenter(
    private val inventoryView: InventoryView,
    private val battleUnitInfoView: BattleUnitInfoView,
    private val battleHudView: BattleHudView,
    private val playerCallToActionView: PlayerCallToActionView,
    private val inventoryApi: InventoryApi,
    private val lootableItemApi: LootableItemApi,
    private val battleUnitApi: BattleUnitApi,
    private val playerApi: PlayerApi,
    private val unitApi: UnitApi,
    private val battleApi: BattleApi,
    private val partyApi: PartyApi,
    private val battlefieldApi: BattlefieldApi,
    private val battlefieldView: BattlefieldView,
    private val texts: Texts,
    private val timeline: BattleSceneTimeline,
    eventBus: EventBus,
) : InventoryView.Delegate {
    // What the scene displays. Changed in timeline order, like the views.
    private var displayedBattleUnitId: String? = null
    private var displayedSlots: List<InventoryView.Slot> = emptyList()
    private var selectedSlotIndex: Int? = null

    // The item found while the inventory was full. The timeline waits until the player chooses what to do with it.
    private var foundItem: InventoryFull? = null
    private var choice: CompletableDeferred<Unit>? = null

    // The item being given, while the player taps the party member who receives it.
    private var giving: Giving? = null

    /** The item of the slot being given, and the tile of each party member that can receive it. */
    private data class Giving(
        val giverBattleUnitId: String,
        val slotIndex: Int,
        val lootableItemId: String,
        val receivers: Map<PositionDto, String>,
    )

    private val subscriptions =
        listOf(
            // Selecting a battle unit displays its info instead of the inventory.
            eventBus.subscribe<BattlefieldHudEvent.SelectedBattleUnit> { event ->
                val battleUnitId = event.battleUnitId
                val hasInventory = hasInventory(battleUnitId)
                timeline.play {
                    clearDisplayedInventory()
                    if (hasInventory) {
                        battleUnitInfoView.displayBag(onBagSelected = { whenIdle { openInventory(battleUnitId) } })
                    } else {
                        battleUnitInfoView.hideBag()
                    }
                }
            },
            eventBus.subscribe<BattlefieldHudEvent.Idle> {
                timeline.play {
                    clearDisplayedInventory()
                    battleUnitInfoView.hideBag()
                }
            },
            eventBus.subscribe<InventoryEvent.ItemStored> { event -> refreshInventory(event.battleUnitId) },
            eventBus.subscribe<InventoryEvent.ItemConsumed> { event -> refreshInventory(event.battleUnitId) },
            eventBus.subscribe<InventoryEvent.ItemDiscarded> { event -> refreshInventory(event.battleUnitId) },
            eventBus.subscribe<InventoryEvent.ItemEquipped> { event -> refreshInventory(event.battleUnitId) },
            eventBus.subscribe<InventoryEvent.ItemUnequipped> { event -> refreshInventory(event.battleUnitId) },
            eventBus.subscribe<InventoryEvent.ItemBought> { event -> refreshInventory(event.battleUnitId) },
            eventBus.subscribe<InventoryEvent.ItemSold> { event -> refreshInventory(event.battleUnitId) },
            // The giver's bag stays open, with nothing selected, and the receiver flashes on its tile.
            eventBus.subscribe<ItemGiven> { event ->
                refreshInventory(event.battleUnitId)
                val giverPosition = battlefieldApi.searchPosition(event.battleUnitId)
                timeline.play { finishGiving(giverPosition) }
            },
            eventBus.subscribe<ItemReceived> { event ->
                refreshInventory(event.battleUnitId)
                flashReceiver(event.battleUnitId)
            },
            // A battle starting abandons the giving, as the battle HUD takes over.
            eventBus.subscribe<ExplorationPaused> { timeline.play { abandonGiving() } },
            // The giver leaves the party, or a receiver can no longer receive: back to the item's actions, when the giver remains.
            eventBus.subscribe<BattleUnitDefeated> { event -> timeline.play { abandonGivingOf(event.battleUnitId) } },
            eventBus.subscribe<InventoryFull> { event ->
                val foundItem = lootableItemApi.searchLootableItemById(event.lootableItemId) ?: return@subscribe
                timeline.play { askWhatToDoWith(event, foundItemName = texts.lootableItemName(foundItem.id), foundItemPrice = foundItem.price) }
            },
        )

    init {
        inventoryView.setDelegate(this)
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    /** The stored items of the battle unit, marking the equipped copy of each equipped item. */
    private fun slotsOf(battleUnitId: String): List<InventoryView.Slot> {
        val inventory = inventoryApi.searchInventoryByBattleUnitId(battleUnitId) ?: return emptyList()
        val equippedItems = inventory.equippedItems.values.toMutableList()
        return inventory.storedItems.map { lootableItemId -> InventoryView.Slot(lootableItemId, isEquipped = equippedItems.remove(lootableItemId)) }
    }

    private fun goldOf(battleUnitId: String): Int = inventoryApi.searchInventoryByBattleUnitId(battleUnitId)?.gold ?: 0

    private fun openInventory(battleUnitId: String) {
        val slots = slotsOf(battleUnitId)
        val gold = goldOf(battleUnitId)
        timeline.play { displayInventory(battleUnitId, slots, gold) }
    }

    private fun displayInventory(
        battleUnitId: String,
        slots: List<InventoryView.Slot>,
        gold: Int,
    ) {
        displayedBattleUnitId = battleUnitId
        displayedSlots = slots
        selectedSlotIndex = null
        inventoryView.display(slots, gold)
        battleHudView.displayInventoryView()
    }

    private fun refreshInventory(battleUnitId: String) {
        val slots = slotsOf(battleUnitId)
        val gold = goldOf(battleUnitId)
        timeline.play {
            if (displayedBattleUnitId != battleUnitId) return@play
            displayedSlots = slots
            selectedSlotIndex = null
            inventoryView.display(slots, gold)
        }
    }

    // Forgets the displayed inventory, e.g.: when the HUD displays the battle unit info instead.
    private fun clearDisplayedInventory() {
        giving = null
        displayedBattleUnitId = null
        displayedSlots = emptyList()
        if (selectedSlotIndex != null) playerCallToActionView.dismiss()
        selectedSlotIndex = null
    }

    /**
     * Opens the inventory and waits for the player to choose: to swap a stored item for the found one, or to leave it on
     * its tile. The inventory is read now, as a previous choice of the same walk may have changed it.
     */
    private suspend fun askWhatToDoWith(
        found: InventoryFull,
        foundItemName: String,
        foundItemPrice: Int,
    ) {
        val pendingChoice = CompletableDeferred<Unit>()
        foundItem = found
        choice = pendingChoice
        displayInventory(found.battleUnitId, slotsOf(found.battleUnitId), goldOf(found.battleUnitId))
        playerCallToActionView.displayInventoryFull(
            foundItemName = foundItemName,
            foundItemPrice = foundItemPrice,
            foundItemId = found.lootableItemId,
            onLeft = { completeChoice() },
        )
        pendingChoice.await()
    }

    private fun completeChoice() {
        val pendingChoice = choice ?: return
        foundItem = null
        choice = null
        playerCallToActionView.dismiss()
        pendingChoice.complete(Unit)
    }

    // An equipped item can not be discarded, so it can not be swapped either.
    private fun swap(
        found: InventoryFull,
        slotIndex: Int,
    ) {
        val slot = displayedSlots.getOrNull(slotIndex) ?: return
        if (slot.isEquipped) return
        inventoryApi.discardItem(battleUnitId = found.battleUnitId, lootableItemId = slot.lootableItemId)
        inventoryApi.storeLootedItem(battleUnitId = found.battleUnitId, lootableItemId = found.lootableItemId, row = found.row, column = found.column)
        completeChoice()
    }

    private fun selectItem(slotIndex: Int) {
        val battleUnitId = displayedBattleUnitId ?: return
        val slot = displayedSlots.getOrNull(slotIndex) ?: return
        if (!canUseInventoryOf(battleUnitId)) return
        val lootableItem = lootableItemApi.searchLootableItemById(slot.lootableItemId) ?: return
        val isItemEquipped = displayedSlots.any { displayedSlot -> displayedSlot.lootableItemId == slot.lootableItemId && displayedSlot.isEquipped }
        val actions =
            buildList {
                add(action(texts[UiText.ITEM_ACTIONS_CANCEL]) {})
                add(action(texts[UiText.ITEM_ACTIONS_DISCARD], isEnabled = !slot.isEquipped) { inventoryApi.discardItem(battleUnitId, slot.lootableItemId) })
                // Only while exploring: the battle actions stay as they are.
                if (isExploring()) {
                    val refusal = inventoryApi.searchGiveRefusal(battleUnitId, slot.lootableItemId)
                    add(
                        ItemActionsView.Action(label = giveLabel(refusal), isEnabled = refusal == null) {
                            if (!timeline.isPlaying) startGiving(battleUnitId, slotIndex, slot.lootableItemId, texts.lootableItemName(lootableItem.id))
                        },
                    )
                }
                when (lootableItem.lootType.type) {
                    // An item teaching an ability its battle unit can not learn (e.g.: already known) can not be consumed.
                    CONSUMABLE ->
                        add(
                            action(texts[UiText.ITEM_ACTIONS_CONSUME], isEnabled = inventoryApi.canConsumeItem(battleUnitId, slot.lootableItemId)) {
                                inventoryApi.consumeItem(battleUnitId, slot.lootableItemId)
                            },
                        )
                    EQUIPPABLE ->
                        if (slot.isEquipped) {
                            add(action(texts[UiText.ITEM_ACTIONS_UNEQUIP]) { inventoryApi.unequipItem(battleUnitId, slot.lootableItemId) })
                        } else {
                            // Another copy of the item is already equipped.
                            add(action(texts[UiText.ITEM_ACTIONS_EQUIP], isEnabled = !isItemEquipped) { inventoryApi.equipItem(battleUnitId, slot.lootableItemId) })
                        }
                }
            }
        timeline.play {
            selectedSlotIndex = slotIndex
            inventoryView.selectSlot(slotIndex)
            playerCallToActionView.displayItemActions(itemName = texts.lootableItemName(lootableItem.id), itemPrice = lootableItem.price, actions = actions)
        }
    }

    // The call to action is dismissed once the action is selected.
    private fun action(
        label: String,
        isEnabled: Boolean = true,
        act: () -> Unit,
    ) = ItemActionsView.Action(label = label, isEnabled = isEnabled) {
        if (!timeline.isPlaying) act()
        timeline.play {
            selectedSlotIndex = null
            inventoryView.selectSlot(null)
        }
    }

    // A disabled Give reads why when no party member can receive the item.
    private fun giveLabel(refusal: GiveRefusalDto?): String =
        when (refusal) {
            GiveRefusalDto.NO_ONE -> texts[UiText.ITEM_ACTIONS_NO_ONE]
            GiveRefusalDto.BAGS_FULL -> texts[UiText.ITEM_ACTIONS_BAGS_FULL]
            else -> texts[UiText.ITEM_ACTIONS_GIVE]
        }

    // Every tile is dimmed but the ones of the party members that can receive the item, and the call to action asks to tap one.
    private fun startGiving(
        giverBattleUnitId: String,
        slotIndex: Int,
        lootableItemId: String,
        itemName: String,
    ) {
        val receivers =
            inventoryApi
                .searchGiveReceivers(giverBattleUnitId)
                .mapNotNull { receiverId -> battlefieldApi.searchPosition(receiverId)?.let { position -> position to receiverId } }
                .toMap()
        if (receivers.isEmpty()) return
        timeline.play {
            giving = Giving(giverBattleUnitId = giverBattleUnitId, slotIndex = slotIndex, lootableItemId = lootableItemId, receivers = receivers)
            battlefieldView.resetTiles()
            battlefieldView.dimTilesExcept(receivers.keys)
            playerCallToActionView.displayGiveItem(itemName = itemName, onCancelled = { if (!timeline.isPlaying) cancelGiving() })
        }
    }

    // Back to the item's actions, with the giver selected again.
    private fun cancelGiving() {
        val current = giving ?: return
        val giverPosition = battlefieldApi.searchPosition(current.giverBattleUnitId)
        timeline.play {
            giving = null
            restoreGiverSelection(giverPosition)
        }
        selectItem(current.slotIndex)
    }

    private fun finishGiving(giverPosition: PositionDto?) {
        if (giving == null) return
        giving = null
        selectedSlotIndex = null
        inventoryView.selectSlot(null)
        playerCallToActionView.dismiss()
        restoreGiverSelection(giverPosition)
    }

    // The tiles look as when the giver was selected: every tile dimmed but its own one.
    private fun restoreGiverSelection(giverPosition: PositionDto?) {
        battlefieldView.resetTiles()
        if (giverPosition == null) return
        battlefieldView.displayTileSelection(row = giverPosition.row, column = giverPosition.column)
        battlefieldView.dimTilesExcept(setOf(giverPosition))
    }

    private fun abandonGiving() {
        if (giving == null) return
        giving = null
        selectedSlotIndex = null
        inventoryView.selectSlot(null)
        playerCallToActionView.dismiss()
        battlefieldView.resetTiles()
    }

    private fun abandonGivingOf(battleUnitId: String) {
        val current = giving ?: return
        when (battleUnitId) {
            current.giverBattleUnitId -> abandonGiving()
            in current.receivers.values -> cancelGiving()
        }
    }

    private fun flashReceiver(receiverBattleUnitId: String) {
        val receiver = battleUnitApi.searchBattleUnitById(receiverBattleUnitId) ?: return
        val position = battlefieldApi.searchPosition(receiverBattleUnitId) ?: return
        // Alongside the timeline, so the player can give the next item straight away.
        timeline.playAlongside { battlefieldView.flashBattleUnit(row = position.row, column = position.column, unitId = receiver.unitId) }
    }

    private fun isExploring(): Boolean = partyApi.searchParty()?.isExploring == true

    /**
     * Gives the item to the party member on the tile, while giving. Returns whether the tap was taken, so the battlefield
     * ignores it: every tap is taken while giving, also the ones on a tile no receiver stands on.
     */
    fun tileSelected(
        row: Int,
        column: Int,
    ): Boolean {
        val current = giving ?: return false
        val receiverId = current.receivers[PositionDto(row = row, column = column)] ?: return true
        if (!timeline.isPlaying) {
            inventoryApi.giveItem(giverBattleUnitId = current.giverBattleUnitId, receiverBattleUnitId = receiverId, lootableItemId = current.lootableItemId)
        }
        return true
    }

    // Only the battle units of the human player have an inventory, the summoned ones excluded.
    private fun hasInventory(battleUnitId: String): Boolean {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return false
        return playerApi.searchPlayerById(battleUnit.playerId)?.type == PlayerTypeDto.HUMAN && battleUnit.remainingTurns == null
    }

    // In a battle, the inventory can only be used during the turn of its battle unit player. Out of the battles (e.g.:
    // while exploring), it can always be used.
    private fun canUseInventoryOf(battleUnitId: String): Boolean {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return false
        val battle = battleApi.searchBattle()?.takeIf { battle -> !battle.isFinished } ?: return true
        return battle.currentPlayerTurn == battleUnit.playerId
    }

    // The player can only act once the scene shows the current state of the battle, has chosen what to do with the found
    // item, and has given the item or cancelled giving it.
    private fun whenIdle(action: () -> Unit) {
        if (!timeline.isPlaying && choice == null && giving == null) action()
    }

    // Delegates
    override fun slotSelected(index: Int) {
        val found = foundItem
        if (found != null) {
            swap(found, index)
        } else {
            whenIdle { selectItem(index) }
        }
    }

    override fun inventoryClosed() =
        whenIdle {
            val battleUnitId = displayedBattleUnitId ?: return@whenIdle
            val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return@whenIdle
            val unit = unitApi.searchUnitById(battleUnit.unitId) ?: return@whenIdle
            val abilityAvailabilities = battleUnitApi.searchAbilityAvailabilities(battleUnitId)
            timeline.play {
                clearDisplayedInventory()
                // The info is read again, as the items may have changed the battle unit, e.g.: healed it.
                battleUnitInfoView.display(battleUnit, unit, abilityAvailabilities)
                battleHudView.displayBattleUnitInfoView()
            }
        }
}
