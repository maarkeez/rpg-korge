package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.inventory.domain.InventoryEvent
import com.mkz.rpg.inventory.domain.InventoryEvent.InventoryFull
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto.CONSUMABLE
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto.EQUIPPABLE
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

    private val subscriptions =
        listOf(
            // Selecting a battle unit displays its info instead of the inventory.
            eventBus.subscribe<BattlefieldHudEvent.SelectedBattleUnit> { event ->
                val battleUnitId = event.battleUnitId
                val hasInventory = isHuman(battleUnitId)
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
            eventBus.subscribe<InventoryFull> { event ->
                val foundItemName = lootableItemApi.searchLootableItemById(event.lootableItemId)?.name ?: event.lootableItemId
                timeline.play { askWhatToDoWith(event, foundItemName) }
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

    private fun openInventory(battleUnitId: String) {
        val slots = slotsOf(battleUnitId)
        timeline.play { displayInventory(battleUnitId, slots) }
    }

    private fun displayInventory(
        battleUnitId: String,
        slots: List<InventoryView.Slot>,
    ) {
        displayedBattleUnitId = battleUnitId
        displayedSlots = slots
        selectedSlotIndex = null
        inventoryView.display(slots)
        battleHudView.displayInventoryView()
    }

    private fun refreshInventory(battleUnitId: String) {
        val slots = slotsOf(battleUnitId)
        timeline.play {
            if (displayedBattleUnitId != battleUnitId) return@play
            displayedSlots = slots
            selectedSlotIndex = null
            inventoryView.display(slots)
        }
    }

    // Forgets the displayed inventory, e.g.: when the HUD displays the battle unit info instead.
    private fun clearDisplayedInventory() {
        displayedBattleUnitId = null
        displayedSlots = emptyList()
        if (selectedSlotIndex != null) playerCallToActionView.redisplayFinishTurn()
        selectedSlotIndex = null
    }

    /**
     * Opens the inventory and waits for the player to choose: to swap a stored item for the found one, or to leave it on
     * its tile. The inventory is read now, as a previous choice of the same walk may have changed it.
     */
    private suspend fun askWhatToDoWith(
        found: InventoryFull,
        foundItemName: String,
    ) {
        val pendingChoice = CompletableDeferred<Unit>()
        foundItem = found
        choice = pendingChoice
        displayInventory(found.battleUnitId, slotsOf(found.battleUnitId))
        playerCallToActionView.displayInventoryFull(
            foundItemName = foundItemName,
            foundItemId = found.lootableItemId,
            onLeft = { completeChoice() },
        )
        pendingChoice.await()
    }

    private fun completeChoice() {
        val pendingChoice = choice ?: return
        foundItem = null
        choice = null
        playerCallToActionView.redisplayFinishTurn()
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
        if (!isPlayerTurnOf(battleUnitId)) return
        val lootableItem = lootableItemApi.searchLootableItemById(slot.lootableItemId) ?: return
        val isItemEquipped = displayedSlots.any { displayedSlot -> displayedSlot.lootableItemId == slot.lootableItemId && displayedSlot.isEquipped }
        val actions =
            buildList {
                add(action("Cancel") {})
                add(action("Discard", isEnabled = !slot.isEquipped) { inventoryApi.discardItem(battleUnitId, slot.lootableItemId) })
                when (lootableItem.lootType.type) {
                    CONSUMABLE -> add(action("Consume") { inventoryApi.consumeItem(battleUnitId, slot.lootableItemId) })
                    EQUIPPABLE ->
                        if (slot.isEquipped) {
                            add(action("Unequip") { inventoryApi.unequipItem(battleUnitId, slot.lootableItemId) })
                        } else {
                            // Another copy of the item is already equipped.
                            add(action("Equip", isEnabled = !isItemEquipped) { inventoryApi.equipItem(battleUnitId, slot.lootableItemId) })
                        }
                }
            }
        timeline.play {
            selectedSlotIndex = slotIndex
            inventoryView.selectSlot(slotIndex)
            playerCallToActionView.displayItemActions(itemName = lootableItem.name, actions = actions)
        }
    }

    // The call to action displays the finish turn again once the action is selected.
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

    private fun isHuman(battleUnitId: String): Boolean {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return false
        return playerApi.searchPlayerById(battleUnit.playerId)?.type == PlayerTypeDto.HUMAN
    }

    // The inventory can only be used during the turn of its battle unit player.
    private fun isPlayerTurnOf(battleUnitId: String): Boolean {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return false
        return battleApi.searchBattle()?.currentPlayerTurn == battleUnit.playerId
    }

    // The player can only act once the scene shows the current state of the battle, and has chosen what to do with the found item.
    private fun whenIdle(action: () -> Unit) {
        if (!timeline.isPlaying && choice == null) action()
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
            timeline.play {
                clearDisplayedInventory()
                // The info is read again, as the items may have changed the battle unit, e.g.: healed it.
                battleUnitInfoView.display(battleUnit, unit)
                battleHudView.displayBattleUnitInfoView()
            }
        }
}
