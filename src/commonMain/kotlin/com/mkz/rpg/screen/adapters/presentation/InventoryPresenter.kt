package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.inventory.domain.Inventory.Dto.GiveGoldRefusalDto
import com.mkz.rpg.inventory.domain.Inventory.Dto.GiveItemRefusalDto
import com.mkz.rpg.inventory.domain.InventoryEvent
import com.mkz.rpg.inventory.domain.InventoryEvent.GoldGiven
import com.mkz.rpg.inventory.domain.InventoryEvent.GoldReceived
import com.mkz.rpg.inventory.domain.InventoryEvent.InventoryFull
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemGiven
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemReceived
import com.mkz.rpg.inventory.domain.InventoryEvent.RewardGranted
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto.CONSUMABLE
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto.EQUIPPABLE
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.PartyEvent.ExplorationInterrupted
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import kotlinx.coroutines.CompletableDeferred

/**
 * Displays the inventory of the selected battle unit behind the inventory button, lets the player act on its items, and asks the
 * player what to do with an item found while the inventory is full. Discarding an item, also for the found
 * one, asks first.
 *
 * While exploring, an item can be given to another party member: Give dims every tile but the ones of the members that
 * can receive it, and tapping one of them gives the item. Until then, every other tap is ignored, but Cancel's.
 *
 * While exploring, the gold button opens the panel to give gold in place of the inventory, with Cancel, All and Give in the call
 * to action, or reads why no gold can be given. Every tile is dimmed but the receivers' ones, and tapping one chooses it.
 *
 * While exploring, the arrows (or a swipe) switch the inventory to the previous or next party member, selecting it on the
 * battlefield through the selector set with [setBattleUnitSelector], so its inventory stays open meanwhile.
 *
 * The inventory button opens the inventory straight away: it is hidden while an ability of the battle unit is on show, as the battle
 * unit info collapses its header, so it is never tapped over a dimmed battlefield.
 */
class InventoryPresenter(
    private val inventoryView: InventoryView,
    private val giveGoldView: GiveGoldView,
    private val battleUnitInfoView: BattleUnitInfoView,
    private val hudView: HudView,
    private val callToActionView: CallToActionView,
    private val inventoryApi: InventoryApi,
    private val lootableItemApi: LootableItemApi,
    private val battleUnitApi: BattleUnitApi,
    private val playerApi: PlayerApi,
    private val battleApi: BattleApi,
    private val partyApi: PartyApi,
    private val battlefieldApi: BattlefieldApi,
    private val battlefieldView: BattlefieldView,
    private val messages: Messages,
    private val timeline: GameSceneTimeline,
    eventBus: EventBus,
) : InventoryView.Delegate,
    GiveGoldView.Delegate {
    // What the scene displays. Changed in timeline order, like the views.
    private var displayedBattleUnitId: String? = null
    private var displayedSlots: List<InventoryView.Slot> = emptyList()
    private var selectedSlotIndex: Int? = null

    // The item found while the inventory was full. The timeline waits until the player chooses what to do with it.
    private var foundItem: FoundItem? = null
    private var choice: CompletableDeferred<Unit>? = null

    // The item being given, while the player taps the party member who receives it.
    private var giving: Giving? = null

    // The gold being given, while the panel is on show.
    private var givingGold: GivingGold? = null

    // Why no gold can be given is on show, until OK or until the inventory it was read from goes away.
    private var isReadingGoldRefusal = false

    // The party member the inventory switches to, until the selection has selected it.
    private var switchingTo: String? = null
    private var battleUnitSelector: ((battleUnitId: String) -> Unit)? = null

    /** An item found while the inventory was full, and what the call to action reads about it. */
    private data class FoundItem(
        val event: InventoryFull,
        val name: String,
        val sellPrice: Int,
    )

    /** The item of the slot being given, and the tile of each party member that can receive it. */
    private data class Giving(
        val giverBattleUnitId: String,
        val slotIndex: Int,
        val lootableItemId: String,
        val receivers: Map<PositionDto, String>,
    )

    /** Who gives the gold and carries how much, who can receive it, in party order, which of them is chosen, and the amount. */
    private data class GivingGold(
        val giverBattleUnitId: String,
        val giverGold: Int,
        val receivers: List<GoldReceiver>,
        val receiverIndex: Int,
        val amount: Int,
    ) {
        val receiver: GoldReceiver = receivers[receiverIndex]
        val maximum: Int = minOf(giverGold, receiver.room)

        // Another receiver may have less room, so the amount is lowered to fit.
        fun updated(
            newReceivers: List<GoldReceiver> = receivers,
            newIndex: Int = receiverIndex,
            newGiverGold: Int = giverGold,
        ): GivingGold = copy(giverGold = newGiverGold, receivers = newReceivers, receiverIndex = newIndex, amount = amount.coerceAtMost(minOf(newGiverGold, newReceivers[newIndex].room)))
    }

    /** A party member who can receive gold, and the tile it stands on. */
    private data class GoldReceiver(
        val battleUnitId: String,
        val unitId: String,
        val gold: Int,
        val room: Int,
        val position: PositionDto,
    )

    private val subscriptions =
        listOf(
            // Selecting a battle unit displays its info instead of the inventory, but the party member the inventory switches to.
            eventBus.subscribe<SelectionEvent.BattleUnitSelected> { event ->
                val battleUnitId = event.battleUnitId
                val hasInventory = hasInventory(battleUnitId)
                val isSwitchedTo = switchingTo == battleUnitId
                switchingTo = null
                timeline.play {
                    clearDisplayedInventory()
                    if (hasInventory) {
                        battleUnitInfoView.displayInventoryButton(onInventoryButtonSelected = { whenIdle { openInventory(battleUnitId) } })
                    } else {
                        battleUnitInfoView.hideInventoryButton()
                    }
                }
                if (isSwitchedTo && hasInventory) openInventory(battleUnitId)
            },
            eventBus.subscribe<SelectionEvent.Deselected> {
                timeline.play {
                    clearDisplayedInventory()
                    battleUnitInfoView.hideInventoryButton()
                }
            },
            eventBus.subscribe<InventoryEvent.ItemStored> { event -> refreshInventory(event.battleUnitId) },
            eventBus.subscribe<InventoryEvent.ItemConsumed> { event -> refreshInventory(event.battleUnitId) },
            eventBus.subscribe<InventoryEvent.ItemDiscarded> { event -> refreshInventory(event.battleUnitId) },
            eventBus.subscribe<InventoryEvent.ItemEquipped> { event -> refreshInventory(event.battleUnitId) },
            eventBus.subscribe<InventoryEvent.ItemUnequipped> { event -> refreshInventory(event.battleUnitId) },
            eventBus.subscribe<InventoryEvent.ItemBought> { event -> refreshInventory(event.battleUnitId) },
            eventBus.subscribe<InventoryEvent.ItemSold> { event -> refreshInventory(event.battleUnitId) },
            // The giver's inventory stays open, with nothing selected, and the receiver blinks on its tile.
            eventBus.subscribe<ItemGiven> { event ->
                refreshInventory(event.battleUnitId)
                val giverPosition = battlefieldApi.searchPosition(event.battleUnitId)
                timeline.play { finishGiving(giverPosition) }
            },
            eventBus.subscribe<ItemReceived> { event ->
                refreshInventory(event.battleUnitId)
                blinkReceiver(event.battleUnitId)
            },
            // A battle starting abandons the giving, as the HUD takes over.
            eventBus.subscribe<ExplorationInterrupted> { timeline.play { abandonGiving() } },
            // The giver leaves the party, or a receiver can no longer receive: back to the item's actions, when the giver remains.
            eventBus.subscribe<BattleUnitDefeated> { event -> timeline.play { abandonGivingOf(event.battleUnitId) } },
            // The inventory is displayed again with the new gold, so the next give can start straight away, and the receiver blinks on its tile.
            eventBus.subscribe<GoldGiven> { event -> timeline.play { finishGivingGold(event.battleUnitId) } },
            eventBus.subscribe<GoldReceived> { event ->
                refreshInventory(event.battleUnitId)
                blinkReceiver(event.battleUnitId)
            },
            // The gold and the room are read again, e.g.: a wandering rat dies on lava and completes a task.
            eventBus.subscribe<RewardGranted> { event -> timeline.play { readGivingGoldAgain(event.battleUnitId) } },
            eventBus.subscribe<ExplorationInterrupted> { timeline.play { abandonGivingGold() } },
            eventBus.subscribe<BattleUnitDefeated> { event -> timeline.play { abandonGivingGoldOf(event.battleUnitId) } },
            eventBus.subscribe<InventoryFull> { event ->
                val sellPrice = lootableItemApi.searchSellPrice(event.lootableItemId) ?: return@subscribe
                timeline.play { askWhatToDoWith(FoundItem(event, name = messages.lootableItemName(event.lootableItemId), sellPrice = sellPrice)) }
            },
        )

    init {
        inventoryView.setDelegate(this)
        giveGoldView.setDelegate(this)
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    /** Selects the battle unit on the battlefield, as if the player had tapped its tile, e.g.: the party member switched to. */
    fun setBattleUnitSelector(selector: (battleUnitId: String) -> Unit) {
        battleUnitSelector = selector
    }

    /** The stored items of the battle unit, marking the equipped copy of each equipped item. */
    private fun slotsOf(battleUnitId: String): List<InventoryView.Slot> {
        val inventory = inventoryApi.searchInventoryByBattleUnitId(battleUnitId) ?: return emptyList()
        val equippedItems = inventory.equippedItems.values.toMutableList()
        return inventory.storedItems.map { lootableItemId -> InventoryView.Slot(lootableItemId, isEquipped = equippedItems.remove(lootableItemId)) }
    }

    private fun goldOf(battleUnitId: String): Int = inventoryApi.searchInventoryByBattleUnitId(battleUnitId)?.gold ?: 0

    // While exploring, the party member and its position in the party, to switch to the members beside it.
    private fun ownerOf(battleUnitId: String): InventoryView.Owner? {
        val party = partyApi.searchParty()?.takeIf { party -> party.isExploring } ?: return null
        val partyIndex = party.battleUnitIds.indexOf(battleUnitId).takeIf { index -> index >= 0 } ?: return null
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return null
        return InventoryView.Owner(
            name = messages.unitName(battleUnit.unitId),
            partyIndex = partyIndex,
            partySize = party.battleUnitIds.size,
            canGiveGold = inventoryApi.searchGiveGoldRefusal(battleUnitId) == null,
        )
    }

    private fun openInventory(battleUnitId: String) {
        val slots = slotsOf(battleUnitId)
        val gold = goldOf(battleUnitId)
        val owner = ownerOf(battleUnitId)
        timeline.play { displayInventory(battleUnitId, slots, gold, owner) }
    }

    private fun displayInventory(
        battleUnitId: String,
        slots: List<InventoryView.Slot>,
        gold: Int,
        owner: InventoryView.Owner?,
    ) {
        displayedBattleUnitId = battleUnitId
        displayedSlots = slots
        selectedSlotIndex = null
        inventoryView.display(slots, gold, owner)
        hudView.displayInventoryView()
    }

    private fun refreshInventory(battleUnitId: String) {
        val slots = slotsOf(battleUnitId)
        val gold = goldOf(battleUnitId)
        val owner = ownerOf(battleUnitId)
        timeline.play {
            if (displayedBattleUnitId != battleUnitId) return@play
            displayedSlots = slots
            selectedSlotIndex = null
            inventoryView.display(slots, gold, owner)
        }
    }

    // Selects the previous or next party member on the battlefield, whose inventory is then displayed. Stops at the ends of the party.
    private fun switchOwner(step: Int) {
        val battleUnitId = displayedBattleUnitId ?: return
        val party = partyApi.searchParty()?.takeIf { party -> party.isExploring } ?: return
        val partyIndex = party.battleUnitIds.indexOf(battleUnitId).takeIf { index -> index >= 0 } ?: return
        val switchedToId = party.battleUnitIds.getOrNull(partyIndex + step) ?: return
        val selector = battleUnitSelector ?: return
        switchingTo = switchedToId
        selector(switchedToId)
    }

    // Forgets the displayed inventory, e.g.: when the HUD displays the battle unit info instead.
    private fun clearDisplayedInventory() {
        giving = null
        displayedBattleUnitId = null
        displayedSlots = emptyList()
        if (selectedSlotIndex != null || isReadingGoldRefusal || givingGold != null) callToActionView.dismiss()
        selectedSlotIndex = null
        isReadingGoldRefusal = false
        givingGold = null
    }

    /**
     * Opens the inventory and waits for the player to choose: to discard a stored item for the found one, or to leave it on
     * its tile. The inventory is read now, as a previous choice of the same walk may have changed it.
     */
    private suspend fun askWhatToDoWith(found: FoundItem) {
        val pendingChoice = CompletableDeferred<Unit>()
        val battleUnitId = found.event.battleUnitId
        foundItem = found
        choice = pendingChoice
        displayInventory(battleUnitId, slotsOf(battleUnitId), goldOf(battleUnitId), ownerOf(battleUnitId))
        displayFoundItem(found)
        pendingChoice.await()
    }

    private fun displayFoundItem(found: FoundItem) {
        callToActionView.displayInventoryFull(
            foundItemName = found.name,
            foundItemSellPrice = found.sellPrice,
            foundItemId = found.event.lootableItemId,
            onLeft = { completeChoice() },
        )
    }

    private fun completeChoice() {
        val pendingChoice = choice ?: return
        foundItem = null
        choice = null
        callToActionView.dismiss()
        pendingChoice.complete(Unit)
    }

    // An equipped item can not be discarded, so the found one can not take its place either. Tapping another item moves the question to it.
    // The timeline is waiting for the choice, so the views change straight away.
    private fun askToDiscardFor(
        found: FoundItem,
        slotIndex: Int,
    ) {
        val slot = displayedSlots.getOrNull(slotIndex) ?: return
        if (slot.isEquipped) return
        selectedSlotIndex = slotIndex
        inventoryView.selectSlot(slotIndex)
        callToActionView.displayDiscardQuestion(
            itemName = messages.lootableItemName(slot.lootableItemId),
            onBack = {
                clearDiscardSelection()
                displayFoundItem(found)
            },
            onDiscarded = { discardFor(found.event, slot.lootableItemId) },
        )
    }

    private fun clearDiscardSelection() {
        selectedSlotIndex = null
        inventoryView.selectSlot(null)
    }

    private fun discardFor(
        found: InventoryFull,
        lootableItemId: String,
    ) {
        clearDiscardSelection()
        inventoryApi.discardItem(battleUnitId = found.battleUnitId, lootableItemId = lootableItemId)
        inventoryApi.storeLootedItem(battleUnitId = found.battleUnitId, lootableItemId = found.lootableItemId, row = found.row, column = found.column)
        completeChoice()
    }

    private fun selectItem(slotIndex: Int) {
        val battleUnitId = displayedBattleUnitId ?: return
        val slot = displayedSlots.getOrNull(slotIndex) ?: return
        if (!canUseInventoryOf(battleUnitId)) return
        val lootableItem = lootableItemApi.searchLootableItemById(slot.lootableItemId) ?: return
        val sellPrice = lootableItemApi.searchSellPrice(slot.lootableItemId) ?: return
        val isItemEquipped = displayedSlots.any { displayedSlot -> displayedSlot.lootableItemId == slot.lootableItemId && displayedSlot.isEquipped }
        val actions =
            buildList {
                add(action(messages[UiMessage.ITEM_BUTTONS_CANCEL]) {})
                add(
                    ItemButtonsView.Button(label = messages[UiMessage.ITEM_BUTTONS_DISCARD], isEnabled = !slot.isEquipped) {
                        if (!timeline.isPlaying) askToDiscard(battleUnitId, slotIndex, slot.lootableItemId, messages.lootableItemName(lootableItem.id))
                    },
                )
                // Only while exploring: the battle actions stay as they are.
                if (isExploring()) {
                    val refusal = inventoryApi.searchGiveItemRefusal(battleUnitId, slot.lootableItemId)
                    add(
                        ItemButtonsView.Button(label = giveLabel(refusal), isEnabled = refusal == null) {
                            if (!timeline.isPlaying) startGiving(battleUnitId, slotIndex, slot.lootableItemId, messages.lootableItemName(lootableItem.id))
                        },
                    )
                }
                when (lootableItem.lootType.type) {
                    // An item teaching an ability its battle unit can not learn (e.g.: already known) can not be consumed.
                    CONSUMABLE ->
                        add(
                            primaryAction(messages[UiMessage.ITEM_BUTTONS_CONSUME], isEnabled = inventoryApi.canConsumeItem(battleUnitId, slot.lootableItemId)) {
                                inventoryApi.consumeItem(battleUnitId, slot.lootableItemId)
                            },
                        )
                    EQUIPPABLE ->
                        if (slot.isEquipped) {
                            add(primaryAction(messages[UiMessage.ITEM_BUTTONS_UNEQUIP]) { inventoryApi.unequipItem(battleUnitId, slot.lootableItemId) })
                        } else {
                            // Another copy of the item is already equipped.
                            add(primaryAction(messages[UiMessage.ITEM_BUTTONS_EQUIP], isEnabled = !isItemEquipped) { inventoryApi.equipItem(battleUnitId, slot.lootableItemId) })
                        }
                }
            }
        timeline.play {
            selectedSlotIndex = slotIndex
            inventoryView.selectSlot(slotIndex)
            callToActionView.displayItemButtons(itemName = messages.lootableItemName(lootableItem.id), itemSellPrice = sellPrice, actions = actions)
        }
    }

    // The call to action is dismissed once the action is selected.
    private fun action(
        label: String,
        isEnabled: Boolean = true,
        act: () -> Unit,
    ) = ItemButtonsView.Button(label = label, isEnabled = isEnabled) {
        if (!timeline.isPlaying) act()
        timeline.play {
            selectedSlotIndex = null
            inventoryView.selectSlot(null)
        }
    }

    private fun primaryAction(
        label: String,
        isEnabled: Boolean = true,
        act: () -> Unit,
    ) = action(label, isEnabled, act).copy(color = ConfirmButton.COLOR, pressedColor = ConfirmButton.PRESSED_COLOR)

    // The item stays framed, and Back displays its actions again. Tapping another item, the arrows or Close drop the question.
    private fun askToDiscard(
        battleUnitId: String,
        slotIndex: Int,
        lootableItemId: String,
        itemName: String,
    ) {
        timeline.play {
            callToActionView.displayDiscardQuestion(
                itemName = itemName,
                onBack = { whenIdle { selectItem(slotIndex) } },
                onDiscarded = {
                    whenIdle {
                        inventoryApi.discardItem(battleUnitId, lootableItemId)
                        callToActionView.dismiss()
                        timeline.play {
                            selectedSlotIndex = null
                            inventoryView.selectSlot(null)
                        }
                    }
                },
            )
        }
    }

    // A disabled Give reads why when no party member can receive the item.
    private fun giveLabel(refusal: GiveItemRefusalDto?): String =
        when (refusal) {
            GiveItemRefusalDto.NO_ONE -> messages[UiMessage.ITEM_BUTTONS_NO_ONE]
            GiveItemRefusalDto.INVENTORIES_FULL -> messages[UiMessage.ITEM_BUTTONS_NO_SPACE]
            else -> messages[UiMessage.ITEM_BUTTONS_GIVE]
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
                .searchItemReceivers(giverBattleUnitId)
                .mapNotNull { receiverId -> battlefieldApi.searchPosition(receiverId)?.let { position -> position to receiverId } }
                .toMap()
        if (receivers.isEmpty()) return
        timeline.play {
            giving = Giving(giverBattleUnitId = giverBattleUnitId, slotIndex = slotIndex, lootableItemId = lootableItemId, receivers = receivers)
            battlefieldView.resetTiles()
            battlefieldView.dimTilesExcept(BattlefieldMapper.positions(receivers.keys))
            callToActionView.displayGiveItem(itemName = itemName, onCancelled = { if (!timeline.isPlaying) cancelGiving() })
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
        callToActionView.dismiss()
        restoreGiverSelection(giverPosition)
    }

    // The tiles look as when the giver was selected: every tile dimmed but its own one.
    private fun restoreGiverSelection(giverPosition: PositionDto?) {
        battlefieldView.resetTiles()
        if (giverPosition == null) return
        battlefieldView.displayTileSelection(row = giverPosition.row, column = giverPosition.column)
        battlefieldView.dimTilesExcept(setOf(BattlefieldMapper.position(giverPosition)))
    }

    private fun abandonGiving() {
        if (giving == null) return
        giving = null
        selectedSlotIndex = null
        inventoryView.selectSlot(null)
        callToActionView.dismiss()
        battlefieldView.resetTiles()
    }

    private fun abandonGivingOf(battleUnitId: String) {
        val current = giving ?: return
        when (battleUnitId) {
            current.giverBattleUnitId -> abandonGiving()
            in current.receivers.values -> cancelGiving()
        }
    }

    private fun blinkReceiver(receiverBattleUnitId: String) {
        val receiver = battleUnitApi.searchBattleUnitById(receiverBattleUnitId) ?: return
        val position = battlefieldApi.searchPosition(receiverBattleUnitId) ?: return
        // Alongside the timeline, so the player can give the next item straight away.
        timeline.playAlongside { battlefieldView.blinkBattleUnit(row = position.row, column = position.column, unitId = receiver.unitId) }
    }

    // Reads why no gold can be given, or opens the panel. Either way, the selected item is cleared.
    private fun selectGold() {
        val giverBattleUnitId = displayedBattleUnitId ?: return
        val refusal = inventoryApi.searchGiveGoldRefusal(giverBattleUnitId)
        if (refusal == null) {
            startGivingGold(giverBattleUnitId)
        } else {
            val message = refusalMessage(refusal) ?: return
            timeline.play {
                clearSelectedSlot()
                isReadingGoldRefusal = true
                callToActionView.displayGiveGoldRefusal(message, onDismissed = { isReadingGoldRefusal = false })
            }
        }
    }

    // A party that does not explore never displays the gold button.
    private fun refusalMessage(refusal: GiveGoldRefusalDto): String? =
        when (refusal) {
            GiveGoldRefusalDto.NOT_EXPLORING -> null
            GiveGoldRefusalDto.NO_GOLD -> messages[UiMessage.GIVE_GOLD_NO_GOLD]
            GiveGoldRefusalDto.NO_ONE -> messages[UiMessage.GIVE_GOLD_NO_ONE]
            GiveGoldRefusalDto.GOLD_FULL -> messages[UiMessage.GIVE_GOLD_GOLD_FULL]
        }

    private fun clearSelectedSlot() {
        selectedSlotIndex = null
        inventoryView.selectSlot(null)
    }

    private fun goldReceiversOf(giverBattleUnitId: String): List<GoldReceiver> =
        inventoryApi.searchGoldReceivers(giverBattleUnitId).mapNotNull { receiver ->
            val unitId = battleUnitApi.searchBattleUnitById(receiver.battleUnitId)?.unitId ?: return@mapNotNull null
            val position = battlefieldApi.searchPosition(receiver.battleUnitId) ?: return@mapNotNull null
            GoldReceiver(battleUnitId = receiver.battleUnitId, unitId = unitId, gold = receiver.gold, room = receiver.room, position = position)
        }

    private fun startGivingGold(giverBattleUnitId: String) {
        val receivers = goldReceiversOf(giverBattleUnitId)
        if (receivers.isEmpty()) return
        val current = GivingGold(giverBattleUnitId, giverGold = goldOf(giverBattleUnitId), receivers = receivers, receiverIndex = 0, amount = 1)
        timeline.play {
            clearSelectedSlot()
            isReadingGoldRefusal = false
            givingGold = current
            displayGivingGold(current)
            callToActionView.displayGiveGold(
                amount = current.amount,
                maximum = current.maximum,
                onCancelled = { if (!timeline.isPlaying) cancelGivingGold() },
                onAll = { giveAllGold() },
                onGiven = { giveGold() },
            )
            hudView.displayGiveGoldView()
        }
    }

    // Displays the panel and the receivers' tiles, as anything but the slider changes what they show.
    private fun displayGivingGold(current: GivingGold) {
        val giverUnitId = battleUnitApi.searchBattleUnitById(current.giverBattleUnitId)?.unitId ?: return
        giveGoldView.display(
            giver = GiveGoldView.Member(name = messages.unitName(giverUnitId), unitId = giverUnitId, gold = current.giverGold),
            receiver = GiveGoldView.Member(name = messages.unitName(current.receiver.unitId), unitId = current.receiver.unitId, gold = current.receiver.gold),
            amount = current.amount,
            maximum = current.maximum,
            canStepToPrevious = current.receiverIndex > 0,
            canStepToNext = current.receiverIndex < current.receivers.lastIndex,
        )
        callToActionView.updateGiveGold(amount = current.amount, maximum = current.maximum)
        battlefieldView.resetTiles()
        battlefieldView.dimTilesExcept(BattlefieldMapper.positions(current.receivers.map { receiver -> receiver.position }.toSet()))
        battlefieldView.displayTileSelection(row = current.receiver.position.row, column = current.receiver.position.column)
    }

    private fun updateGivingGold(change: (GivingGold) -> GivingGold) {
        timeline.play {
            val current = givingGold ?: return@play
            val changed = change(current)
            givingGold = changed
            displayGivingGold(changed)
        }
    }

    private fun giveAllGold() = updateGivingGold { current -> current.copy(amount = current.maximum) }

    private fun chooseReceiver(index: Int) = updateGivingGold { current -> if (index in current.receivers.indices) current.updated(newIndex = index) else current }

    private fun giveGold() {
        val current = givingGold ?: return
        if (!timeline.isPlaying) inventoryApi.giveGold(giverBattleUnitId = current.giverBattleUnitId, receiverBattleUnitId = current.receiver.battleUnitId, amount = current.amount)
    }

    private fun finishGivingGold(giverBattleUnitId: String) {
        if (givingGold == null) return
        displayInventoryButtonAgain(giverBattleUnitId)
        restoreGiverSelection(battlefieldApi.searchPosition(giverBattleUnitId))
    }

    // Back to the inventory with nothing moved, with the giver selected again.
    private fun cancelGivingGold() {
        val current = givingGold ?: return
        timeline.play { finishGivingGold(current.giverBattleUnitId) }
    }

    private fun displayInventoryButtonAgain(giverBattleUnitId: String) {
        givingGold = null
        callToActionView.dismiss()
        displayInventory(giverBattleUnitId, slotsOf(giverBattleUnitId), goldOf(giverBattleUnitId), ownerOf(giverBattleUnitId))
    }

    private fun abandonGivingGold() {
        val current = givingGold ?: return
        displayInventoryButtonAgain(current.giverBattleUnitId)
        battlefieldView.resetTiles()
    }

    // The giver being defeated abandons the give. A receiver being defeated moves on to the next one.
    private fun abandonGivingGoldOf(battleUnitId: String) {
        val current = givingGold ?: return
        when {
            battleUnitId == current.giverBattleUnitId -> abandonGivingGold()
            current.receivers.any { receiver -> receiver.battleUnitId == battleUnitId } ->
                continueGivingGold(current, current.receivers.filterNot { receiver -> receiver.battleUnitId == battleUnitId })
        }
    }

    // A reward takes room from a receiver or adds gold to the giver, so both are read again.
    private fun readGivingGoldAgain(battleUnitId: String) {
        val current = givingGold ?: return
        val isInvolved = battleUnitId == current.giverBattleUnitId || current.receivers.any { receiver -> receiver.battleUnitId == battleUnitId }
        if (isInvolved) continueGivingGold(current, goldReceiversOf(current.giverBattleUnitId), goldOf(current.giverBattleUnitId))
    }

    // The chosen receiver stays while it can receive gold, or the next one takes its place. Back to the inventory when none is left.
    private fun continueGivingGold(
        current: GivingGold,
        receivers: List<GoldReceiver>,
        giverGold: Int = current.giverGold,
    ) {
        if (receivers.isEmpty()) {
            finishGivingGold(current.giverBattleUnitId)
            return
        }
        val chosenIndex = receivers.indexOfFirst { receiver -> receiver.battleUnitId == current.receiver.battleUnitId }
        val index = if (chosenIndex >= 0) chosenIndex else current.receiverIndex.coerceAtMost(receivers.lastIndex)
        val changed = current.updated(newReceivers = receivers, newIndex = index, newGiverGold = giverGold)
        givingGold = changed
        displayGivingGold(changed)
    }

    private fun isExploring(): Boolean = partyApi.searchParty()?.isExploring == true

    /**
     * Gives the item to the party member on the tile, while giving, or chooses it to receive the gold, while giving gold.
     * Returns whether the tap was taken, so the battlefield ignores it: every tap is taken meanwhile, also the ones on a tile
     * no receiver stands on.
     */
    fun tileSelected(
        row: Int,
        column: Int,
    ): Boolean {
        val currentGold = givingGold
        if (currentGold != null) {
            val index = currentGold.receivers.indexOfFirst { receiver -> receiver.position == PositionDto(row = row, column = column) }
            if (index >= 0) chooseReceiver(index)
            return true
        }
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
        return playerApi.searchPlayerById(battleUnit.playerId)?.type == PlayerTypeDto.HUMAN && battleUnit.remainingSummonDuration == null
    }

    // In a battle, the inventory can only be used during the turn of its battle unit player. Out of the battles (e.g.:
    // while exploring), it can always be used.
    private fun canUseInventoryOf(battleUnitId: String): Boolean {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return false
        val battle = battleApi.searchBattle()?.takeIf { battle -> !battle.isFinished } ?: return true
        return battle.currentPlayerId == battleUnit.playerId
    }

    // The player can only act once the scene shows the current state of the battle, has chosen what to do with the found
    // item, has given the item or the gold or cancelled giving it, and once the party member switched to is selected.
    private fun whenIdle(action: () -> Unit) {
        if (!timeline.isPlaying && choice == null && giving == null && givingGold == null && switchingTo == null) action()
    }

    // Delegates
    override fun slotSelected(index: Int) {
        val found = foundItem
        if (found != null) {
            askToDiscardFor(found, index)
        } else {
            whenIdle { selectItem(index) }
        }
    }

    override fun inventoryOwnerStepped(step: Int) = whenIdle { switchOwner(step) }

    override fun goldSelected() = whenIdle { selectGold() }

    override fun amountChanged(amount: Int) {
        timeline.play {
            val current = givingGold ?: return@play
            givingGold = current.copy(amount = amount)
            callToActionView.updateGiveGold(amount = amount, maximum = current.maximum)
        }
    }

    override fun receiverStepped(step: Int) {
        val current = givingGold ?: return
        chooseReceiver(current.receiverIndex + step)
    }

    override fun inventoryClosed() =
        whenIdle {
            val battleUnitId = displayedBattleUnitId ?: return@whenIdle
            val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return@whenIdle
            val abilityAvailabilities = battleUnitApi.searchAbilityAvailabilities(battleUnitId)
            timeline.play {
                clearDisplayedInventory()
                // The info is read again, as the items may have changed the battle unit, e.g.: healed it.
                battleUnitInfoView.display(
                    battleUnit = BattleUnitInfoMapper.battleUnitInfo(battleUnit, isHumanPlayers = true),
                    turnActions = BattleUnitInfoMapper.turnActions(battleUnit),
                    remainingTurns = battleUnit.remainingSummonDuration,
                    abilities = BattleUnitInfoMapper.abilities(abilityAvailabilities),
                )
                hudView.displayBattleUnitInfoView()
            }
        }
}
