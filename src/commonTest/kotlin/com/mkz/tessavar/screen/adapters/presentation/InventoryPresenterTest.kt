package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto.Type.TARGETED_TILE
import com.mkz.tessavar.ability.domain.Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_CASTER
import com.mkz.tessavar.ability.domain.AbilityMother.ability
import com.mkz.tessavar.ability.domain.AbilityMother.abilityEffect
import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.effect.domain.EffectMother.healEffect
import com.mkz.tessavar.effect.domain.EffectMother.increaseMaximumHealthEffect
import com.mkz.tessavar.effect.domain.EffectMother.learnAbilityEffect
import com.mkz.tessavar.effect.domain.EffectMother.summonEffect
import com.mkz.tessavar.lootDrop.domain.LootDrop
import com.mkz.tessavar.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.tessavar.lootTable.domain.LootTableMother.lootTable
import com.mkz.tessavar.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.tessavar.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.consumable
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.equippable
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.equippableLootableItem
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.screen.adapters.presentation.FakeHudView.Display
import com.mkz.tessavar.screen.adapters.presentation.GameSceneTimelineMother.gameSceneTimeline
import com.mkz.tessavar.screen.adapters.presentation.GameSceneTimelineMother.playLongStep
import com.mkz.tessavar.screen.adapters.presentation.InventoryView.Slot
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.Dto.TileDto
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionEvent
import com.mkz.tessavar.shared.adapters.presentation.GameButton
import com.mkz.tessavar.shared.adapters.presentation.SubdomainApis
import com.mkz.tessavar.shared.domain.DomainEvent
import com.mkz.tessavar.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InventoryPresenterTest {
    private val apis =
        SubdomainApis(
            terrains = listOf(terrainThatCanBeOccupied(id = "sand").toDto()),
            effects =
                listOf(
                    healEffect(id = "heal", healing = 10).toDto(),
                    increaseMaximumHealthEffect(id = "maximum-health-20", maximumHealth = 20).toDto(),
                    summonEffect(id = "summon-bee", unitId = "bee").toDto(),
                    learnAbilityEffect(id = "learn-teleport", abilityId = "teleport").toDto(),
                    learnAbilityEffect(id = "learn-bee", abilityId = "bee").toDto(),
                ),
            abilities =
                listOf(
                    ability(
                        id = "bee",
                        manaCost = 0,
                        abilityEffects = listOf(abilityEffect(effectId = "summon-bee", target = TARGETED_TILE)),
                        targeting = VACANT_TILE_ADJACENT_TO_CASTER,
                        summonDuration = 5,
                    ).toDto(),
                ),
            lootableItems =
                listOf(
                    consumableLootableItem(id = "health-potion", price = 10, consumable = consumable(effectIds = setOf("heal"))).toDto(),
                    equippableLootableItem(id = "hammer", price = 50, equippable = equippable(effectIds = setOf("maximum-health-20"), equipmentZone = WEAPON)).toDto(),
                    consumableLootableItem(id = "teleport-essence", consumable = consumable(effectIds = setOf("learn-teleport"))).toDto(),
                    consumableLootableItem(id = "bee-essence", consumable = consumable(effectIds = setOf("learn-bee"))).toDto(),
                ),
            units = listOf(unit(id = "knight", baseMaximumHealth = 100, startingAbilities = listOf("bee"), startingGold = 20).toDto(), unit(id = "rat").toDto(), unit(id = "bee").toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            lootTables = listOf(lootTable(id = "rat-loot", entries = listOf(nothingEntry())).toDto()),
            lootDrops = listOf(lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot")).toDto()),
            tiles = List(4) { List(4) { "sand" } },
            deployments =
                listOf(
                    Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1),
                    Deployment(battleUnitId = "rat-1", unitId = "rat", playerId = "cpu", row = 1, column = 3),
                ),
            playerOrder = listOf("human", "cpu"),
            explore = true,
        ).also(SubdomainApis::initialise)
    private val eventBus = apis.eventBus
    private val inventoryView = FakeInventoryView()
    private val battleUnitInfoView = FakeBattleUnitInfoView()
    private val hudView = FakeHudView()
    private val callToActionView = FakeCallToActionView()
    private val timeline = gameSceneTimeline()

    private val inventoryPresenter =
        InventoryPresenter(
            inventoryView = inventoryView,
            giveGoldView = FakeGiveGoldView(),
            battleUnitInfoView = battleUnitInfoView,
            hudView = hudView,
            callToActionView = callToActionView,
            inventoryApi = apis.inventoryApi,
            lootableItemApi = apis.lootableItemApi,
            battleUnitApi = apis.battleUnitApi,
            playerApi = apis.playerApi,
            battleApi = apis.battleApi,
            partyApi = apis.partyApi,
            battlefieldApi = apis.battlefieldApi,
            battlefieldView = FakeBattlefieldView(),
            messages = MessagesMother.messages(),
            timeline = timeline,
            eventBus = eventBus,
        )

    private fun publish(event: DomainEvent) {
        eventBus.publish(event)
        eventBus.dispatch()
    }

    private fun loot(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        apis.lootDropApi.dropItemWithoutRolling(unitId = "rat", lootableItemId = lootableItemId, row = 1, column = 1)
        apis.inventoryApi.storeLootedItem(battleUnitId = battleUnitId, lootableItemId = lootableItemId, row = 1, column = 1)
        eventBus.dispatch()
    }

    private fun select(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) = publish(SelectionEvent.BattleUnitSelected(tile = TileDto(row, column), battleUnitId = battleUnitId, walkingRange = emptySet()))

    private fun openKnightInventory() {
        select("knight-1", row = 1, column = 1)
        battleUnitInfoView.onInventoryButtonSelected.shouldNotBeNull().invoke()
        eventBus.dispatch()
    }

    private fun summonBee(): TileDto {
        val castGroup = apis.battleUnitApi.whereCanCast("knight-1", "bee").first()
        apis.battleUnitApi.castAbility(battleUnitId = "knight-1", abilityId = "bee", castGroup = castGroup)
        eventBus.dispatch()
        val tile = castGroup.positions.first()
        return TileDto(row = tile.row, column = tile.column)
    }

    private fun clickSlot(index: Int) {
        inventoryView.delegate.shouldNotBeNull().slotSelected(index)
        eventBus.dispatch()
    }

    private fun clickItemAction(label: String) {
        callToActionView.displayedItemButtons
            .shouldNotBeNull()
            .actions
            .single { action -> action.label == label }
            .onSelected()
        eventBus.dispatch()
    }

    private fun colorsOfItemButtons() =
        callToActionView.displayedItemButtons
            .shouldNotBeNull()
            .actions
            .map { action -> action.color }

    private fun answerDiscardQuestion(answer: FakeCallToActionView.DisplayedDiscardQuestion.() -> Unit) {
        callToActionView.displayedDiscardQuestion.shouldNotBeNull().answer()
        eventBus.dispatch()
    }

    private fun fillKnightInventoryWithHealthPotions() = repeat(12) { loot("knight-1", "health-potion") }

    private fun findWhileFull(lootableItemId: String) {
        apis.lootDropApi.dropItemWithoutRolling(unitId = "rat", lootableItemId = lootableItemId, row = 1, column = 2)
        apis.inventoryApi.storeLootedItem(battleUnitId = "knight-1", lootableItemId = lootableItemId, row = 1, column = 2)
        eventBus.dispatch()
    }

    @Test
    fun `DisplayInventoryButton should display the inventory button when a human battle unit is selected`() {
        // Given
        // When
        select("knight-1", row = 1, column = 1)
        // Then
        battleUnitInfoView.onInventoryButtonSelected.shouldNotBeNull()
    }

    @Test
    fun `DisplayInventoryButton should not display the inventory button when a cpu battle unit is selected`() {
        // Given
        select("knight-1", row = 1, column = 1)
        // When
        select("rat-1", row = 1, column = 3)
        // Then
        battleUnitInfoView.onInventoryButtonSelected.shouldBeNull()
    }

    @Test
    fun `DisplayInventoryButton should not display the inventory button when a summoned battle unit is selected`() {
        // Given
        val beeTile = summonBee()
        val beeBattleUnitId = apis.battlefieldApi.searchOccupant(row = beeTile.row, column = beeTile.column).shouldNotBeNull()
        select("knight-1", row = 1, column = 1)
        // When
        select(beeBattleUnitId, row = beeTile.row, column = beeTile.column)
        // Then
        battleUnitInfoView.onInventoryButtonSelected.shouldBeNull()
    }

    @Test
    fun `DisplayInventoryButton should not display the inventory button when nothing is selected`() {
        // Given
        select("knight-1", row = 1, column = 1)
        // When
        publish(SelectionEvent.Deselected)
        // Then
        battleUnitInfoView.onInventoryButtonSelected.shouldBeNull()
    }

    @Test
    fun `OpenInventory should display the stored items marking the equipped one when the inventory button is clicked`() {
        // Given
        loot("knight-1", "health-potion")
        loot("knight-1", "hammer")
        apis.inventoryApi.equipItem(battleUnitId = "knight-1", lootableItemId = "hammer")
        eventBus.dispatch()
        // When
        openKnightInventory()
        // Then
        inventoryView.displayedSlots.last().shouldContainExactly(Slot("health-potion", isEquipped = false), Slot("hammer", isEquipped = true))
        inventoryView.displayedGold shouldBe 20
        hudView.displays.last() shouldBe Display.INVENTORY
    }

    @Test
    fun `OpenInventory should display the inventory title, without the arrows, in a battle`() {
        // Given
        // When
        openKnightInventory()
        // Then
        inventoryView.displayedOwner.shouldBeNull()
    }

    @Test
    fun `OpenInventory should ignore the inventory button when the timeline is playing`() {
        // Given
        select("knight-1", row = 1, column = 1)
        timeline.playLongStep()
        // When
        battleUnitInfoView.onInventoryButtonSelected.shouldNotBeNull().invoke()
        // Then
        hudView.displays.shouldBeEmpty()
    }

    @Test
    fun `RefreshInventory should display the stored items again when the battle unit stores an item while its inventory is displayed`() {
        // Given
        openKnightInventory()
        // When
        loot("knight-1", "hammer")
        // Then
        inventoryView.displayedSlots.last().shouldContainExactly(Slot("hammer", isEquipped = false))
    }

    @Test
    fun `SelectItem should frame the item and display its sell price and actions when a consumable is selected`() {
        // Given
        loot("knight-1", "health-potion")
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        inventoryView.selectedSlot shouldBe 0
        val itemButtons = callToActionView.displayedItemButtons.shouldNotBeNull()
        itemButtons.itemName shouldBe "Health Potion"
        itemButtons.itemSellPrice shouldBe 5
        itemButtons.actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Discard" to true, "Consume" to true)
    }

    @Test
    fun `SelectItem should enable consume when the battle unit can learn the ability the consumable teaches`() {
        // Given
        loot("knight-1", "teleport-essence")
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        val itemButtons = callToActionView.displayedItemButtons.shouldNotBeNull()
        itemButtons.actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Discard" to true, "Consume" to true)
    }

    @Test
    fun `SelectItem should disable consume when the battle unit already knows the ability the consumable teaches`() {
        // Given
        loot("knight-1", "bee-essence")
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        val itemButtons = callToActionView.displayedItemButtons.shouldNotBeNull()
        itemButtons.actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Discard" to true, "Consume" to false)
    }

    @Test
    fun `SelectItem should display equip when an equippable item that is not equipped is selected`() {
        // Given
        loot("knight-1", "hammer")
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        val itemButtons = callToActionView.displayedItemButtons.shouldNotBeNull()
        itemButtons.actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Discard" to true, "Equip" to true)
    }

    @Test
    fun `SelectItem should display its sell price when the item is equipped`() {
        // Given
        loot("knight-1", "hammer")
        apis.inventoryApi.equipItem(battleUnitId = "knight-1", lootableItemId = "hammer")
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        callToActionView.displayedItemButtons.shouldNotBeNull().itemSellPrice shouldBe 25
    }

    @Test
    fun `SelectItem should display unequip and disable discard when the equipped item is selected`() {
        // Given
        loot("knight-1", "hammer")
        apis.inventoryApi.equipItem(battleUnitId = "knight-1", lootableItemId = "hammer")
        eventBus.dispatch()
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        val itemButtons = callToActionView.displayedItemButtons.shouldNotBeNull()
        itemButtons.actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Discard" to false, "Unequip" to true)
    }

    @Test
    fun `SelectItem should disable equip when another copy of the item is equipped`() {
        // Given
        loot("knight-1", "hammer")
        loot("knight-1", "hammer")
        apis.inventoryApi.equipItem(battleUnitId = "knight-1", lootableItemId = "hammer")
        eventBus.dispatch()
        openKnightInventory()
        // When
        clickSlot(1)
        // Then
        val itemButtons = callToActionView.displayedItemButtons.shouldNotBeNull()
        itemButtons.actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Discard" to true, "Equip" to false)
    }

    @Test
    fun `SelectItem should display only Consume in blue when a consumable is clicked`() {
        // Given
        loot("knight-1", "health-potion")
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        colorsOfItemButtons().shouldContainExactly(GameButton.GREY, GameButton.GREY, ConfirmButton.COLOR)
        callToActionView.displayedItemButtons!!
            .actions
            .last()
            .pressedColor shouldBe ConfirmButton.PRESSED_COLOR
    }

    @Test
    fun `SelectItem should display only Equip in blue when an item to equip is clicked`() {
        // Given
        loot("knight-1", "hammer")
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        colorsOfItemButtons().shouldContainExactly(GameButton.GREY, GameButton.GREY, ConfirmButton.COLOR)
    }

    @Test
    fun `SelectItem should display only Unequip in blue when an equipped item is clicked`() {
        // Given
        loot("knight-1", "hammer")
        openKnightInventory()
        clickSlot(0)
        clickItemAction("Equip")
        // When
        clickSlot(0)
        // Then
        colorsOfItemButtons().shouldContainExactly(GameButton.GREY, GameButton.GREY, ConfirmButton.COLOR)
    }

    @Test
    fun `Discard should ask first, keeping the item framed and stored, when discard is clicked`() {
        // Given
        loot("knight-1", "health-potion")
        openKnightInventory()
        clickSlot(0)
        // When
        clickItemAction("Discard")
        // Then
        callToActionView.displayedDiscardQuestion.shouldNotBeNull().itemName shouldBe "Health Potion"
        callToActionView.displayedItemButtons.shouldBeNull()
        inventoryView.selectedSlot shouldBe 0
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")?.storedItems shouldBe listOf("health-potion")
    }

    @Test
    fun `DiscardQuestion should discard the item and clear the selection when discard is clicked`() {
        // Given
        loot("knight-1", "health-potion")
        openKnightInventory()
        clickSlot(0)
        clickItemAction("Discard")
        // When
        answerDiscardQuestion { onDiscarded() }
        // Then
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")?.storedItems shouldBe emptyList()
        inventoryView.displayedSlots.last().shouldBeEmpty()
        inventoryView.selectedSlot.shouldBeNull()
        callToActionView.displayedDiscardQuestion.shouldBeNull()
    }

    @Test
    fun `DiscardQuestion should display the item's actions again, discarding nothing, when back is clicked`() {
        // Given
        loot("knight-1", "health-potion")
        openKnightInventory()
        clickSlot(0)
        clickItemAction("Discard")
        // When
        answerDiscardQuestion { onBack() }
        // Then
        callToActionView.displayedDiscardQuestion.shouldBeNull()
        callToActionView.displayedItemButtons.shouldNotBeNull().itemName shouldBe "Health Potion"
        inventoryView.selectedSlot shouldBe 0
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")?.storedItems shouldBe listOf("health-potion")
    }

    @Test
    fun `DiscardQuestion should give way to the actions of another item, discarding nothing, when it is clicked`() {
        // Given
        loot("knight-1", "health-potion")
        loot("knight-1", "hammer")
        openKnightInventory()
        clickSlot(0)
        clickItemAction("Discard")
        // When
        clickSlot(1)
        // Then
        callToActionView.displayedDiscardQuestion.shouldBeNull()
        callToActionView.displayedItemButtons.shouldNotBeNull().itemName shouldBe "Hammer"
        inventoryView.selectedSlot shouldBe 1
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")?.storedItems shouldBe listOf("health-potion", "hammer")
    }

    @Test
    fun `DiscardQuestion should be dropped, discarding nothing, when the inventory is closed`() {
        // Given
        loot("knight-1", "health-potion")
        openKnightInventory()
        clickSlot(0)
        clickItemAction("Discard")
        // When
        inventoryView.delegate.shouldNotBeNull().inventoryClosed()
        eventBus.dispatch()
        // Then
        callToActionView.displayedDiscardQuestion.shouldBeNull()
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")?.storedItems shouldBe listOf("health-potion")
    }

    @Test
    fun `ConsumeItem should consume the item and display the finish turn again when consume is clicked`() {
        // Given
        loot("knight-1", "health-potion")
        openKnightInventory()
        clickSlot(0)
        // When
        clickItemAction("Consume")
        // Then
        inventoryView.displayedSlots.last().shouldBeEmpty()
        inventoryView.selectedSlot.shouldBeNull()
        callToActionView.displayedItemButtons.shouldBeNull()
    }

    @Test
    fun `EquipItem should mark the item as equipped when equip is clicked`() {
        // Given
        loot("knight-1", "hammer")
        openKnightInventory()
        clickSlot(0)
        // When
        clickItemAction("Equip")
        // Then
        inventoryView.displayedSlots.last().shouldContainExactly(Slot("hammer", isEquipped = true))
        apis.battleUnitApi.searchBattleUnitById("knight-1")?.maximumHealth shouldBe 120
    }

    @Test
    fun `CancelItem should unframe the item without acting on it when cancel is clicked`() {
        // Given
        loot("knight-1", "health-potion")
        openKnightInventory()
        clickSlot(0)
        // When
        clickItemAction("Cancel")
        // Then
        inventoryView.selectedSlot.shouldBeNull()
        apis.inventoryApi
            .searchInventoryByBattleUnitId("knight-1")
            ?.storedItems
            ?.shouldContainExactly("health-potion")
    }

    @Test
    fun `CloseInventory should display the battle unit info read again when the inventory is closed`() {
        // Given
        loot("knight-1", "hammer")
        openKnightInventory()
        clickSlot(0)
        clickItemAction("Equip")
        // When
        inventoryView.delegate.shouldNotBeNull().inventoryClosed()
        // Then
        hudView.displays.last() shouldBe Display.BATTLE_UNIT_INFO
        battleUnitInfoView.displayedBattleUnits
            .last()
            .battleUnit.maximumHealth shouldBe 120
    }

    @Test
    fun `InventoryFull should open the inventory and ask what to do with the found item when an item is found while the inventory is full`() {
        // Given
        fillKnightInventoryWithHealthPotions()
        // When
        findWhileFull("hammer")
        // Then
        hudView.displays.last() shouldBe Display.INVENTORY
        inventoryView.displayedSlots.last().size shouldBe 12
        val inventoryFull = callToActionView.displayedInventoryFull.shouldNotBeNull()
        inventoryFull.foundItemName shouldBe "Hammer"
        inventoryFull.foundItemSellPrice shouldBe 25
        inventoryFull.foundItemId shouldBe "hammer"
        timeline.isPlaying.shouldBeTrue()
    }

    @Test
    fun `InventoryFull should ask first, keeping the choice open, when a stored item is tapped`() {
        // Given
        fillKnightInventoryWithHealthPotions()
        findWhileFull("hammer")
        // When
        clickSlot(0)
        // Then
        callToActionView.displayedDiscardQuestion.shouldNotBeNull().itemName shouldBe "Health Potion"
        callToActionView.displayedInventoryFull.shouldBeNull()
        inventoryView.selectedSlot shouldBe 0
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")?.storedItems shouldBe List(12) { "health-potion" }
        timeline.isPlaying.shouldBeTrue()
    }

    @Test
    fun `InventoryFull should display the found item again, with no item framed, when back is clicked`() {
        // Given
        fillKnightInventoryWithHealthPotions()
        findWhileFull("hammer")
        clickSlot(0)
        // When
        answerDiscardQuestion { onBack() }
        // Then
        callToActionView.displayedInventoryFull.shouldNotBeNull().foundItemName shouldBe "Hammer"
        callToActionView.displayedDiscardQuestion.shouldBeNull()
        inventoryView.selectedSlot.shouldBeNull()
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")?.storedItems shouldBe List(12) { "health-potion" }
        timeline.isPlaying.shouldBeTrue()
    }

    @Test
    fun `InventoryFull should move the question when another stored item is tapped`() {
        // Given
        fillKnightInventoryWithHealthPotions()
        findWhileFull("hammer")
        clickSlot(0)
        // When
        clickSlot(3)
        // Then
        callToActionView.displayedDiscardQuestion.shouldNotBeNull()
        inventoryView.selectedSlot shouldBe 3
        timeline.isPlaying.shouldBeTrue()
    }

    @Test
    fun `InventoryFull should ignore an equipped item when it is tapped`() {
        // Given
        loot("knight-1", "hammer")
        openKnightInventory()
        clickSlot(0)
        clickItemAction("Equip")
        repeat(11) { loot("knight-1", "health-potion") }
        findWhileFull("hammer")
        // When
        clickSlot(0)
        // Then
        callToActionView.displayedDiscardQuestion.shouldBeNull()
        callToActionView.displayedInventoryFull.shouldNotBeNull()
        inventoryView.selectedSlot.shouldBeNull()
    }

    @Test
    fun `InventoryFull should discard the tapped item and store the found one when the question is answered`() {
        // Given
        fillKnightInventoryWithHealthPotions()
        findWhileFull("hammer")
        clickSlot(0)
        // When
        answerDiscardQuestion { onDiscarded() }
        // Then
        callToActionView.displayedDiscardQuestion.shouldBeNull()
        inventoryView.selectedSlot.shouldBeNull()
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")?.storedItems shouldBe List(11) { "health-potion" } + "hammer"
        apis.lootDropApi.searchDroppedItems()[LootDrop.Dto.PositionDto(1, 2)].shouldBeNull()
        callToActionView.displayedInventoryFull.shouldBeNull()
        timeline.isPlaying.shouldBeFalse()
    }

    @Test
    fun `InventoryFull should leave the found item on its tile when leave it is clicked`() {
        // Given
        fillKnightInventoryWithHealthPotions()
        findWhileFull("hammer")
        // When
        callToActionView.displayedInventoryFull
            .shouldNotBeNull()
            .onLeft()
        eventBus.dispatch()
        // Then
        apis.lootDropApi.searchDroppedItems()[LootDrop.Dto.PositionDto(1, 2)] shouldBe listOf("hammer")
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")?.storedItems shouldBe List(12) { "health-potion" }
        timeline.isPlaying.shouldBeFalse()
    }

    @Test
    fun `InventoryFull should ignore the other actions until the player chooses what to do with the found item`() {
        // Given
        fillKnightInventoryWithHealthPotions()
        findWhileFull("hammer")
        // When
        inventoryView.delegate.shouldNotBeNull().inventoryClosed()
        // Then
        hudView.displays.last() shouldBe Display.INVENTORY
        callToActionView.displayedInventoryFull.shouldNotBeNull()
    }

    @Test
    fun `SelectItem should not display the item buttons during the turn of another player in a battle`() {
        // Given
        loot("knight-1", "health-potion")
        apis.battleApi.finishPlayerTurn()
        eventBus.dispatch()
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        callToActionView.displayedItemButtons.shouldBeNull()
    }

    @Test
    fun `ConsumeItem should consume the item out of the battles when the battle has finished while exploring`() {
        // Given
        loot("knight-1", "health-potion")
        apis.battleApi.finishPlayerTurn()
        eventBus.dispatch()
        apis.battleApi.startNextRound(joiningPlayerId = null, joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = listOf("rat-1"))
        eventBus.dispatch()
        apis.battleApi
            .searchBattle()!!
            .isFinished
            .shouldBeTrue()
        openKnightInventory()
        clickSlot(0)
        // When
        clickItemAction("Consume")
        // Then
        inventoryView.displayedSlots.last().shouldBeEmpty()
    }
}
