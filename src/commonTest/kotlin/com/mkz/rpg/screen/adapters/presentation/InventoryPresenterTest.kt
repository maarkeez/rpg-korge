package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TILE
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_SELF
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.effect.domain.EffectMother.deployBattleUnitEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseMaximumHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.learnAbilityEffect
import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippableLootableItem
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.screen.adapters.presentation.FakeBattleHudView.Display
import com.mkz.rpg.screen.adapters.presentation.InventoryView.Slot
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InventoryPresenterTest {
    // The battle has started with the human player turn. The human knight-1 (100 health points) stands at (1,1), and the
    // cpu rat-1 at (1,3), on a 4x4 sand battlefield. The health potion (10 G) heals 10 and the hammer (50 G) raises the maximum health by 20.
    // The knight can summon a bee next to it. The teleport essence teaches teleport, and the bee essence teaches bee.
    private val apis =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            effects =
                listOf(
                    increaseHealthEffect(id = "heal", healing = 10).toDto(),
                    increaseMaximumHealthEffect(id = "maximum-health-20", healthPoints = 20).toDto(),
                    deployBattleUnitEffect(id = "summon-bee", unitId = "bee").toDto(),
                    learnAbilityEffect(id = "learn-teleport", abilityId = "teleport").toDto(),
                    learnAbilityEffect(id = "learn-bee", abilityId = "bee").toDto(),
                ),
            abilities =
                listOf(
                    ability(
                        id = "bee",
                        cost = 0,
                        effectSpecs = listOf(effectSpec(effectId = "summon-bee", target = SELECTED_TILE)),
                        targeting = VACANT_TILE_ADJACENT_TO_SELF,
                        turnsDuration = 5,
                    ).toDto(),
                ),
            lootableItems =
                listOf(
                    consumableLootableItem(id = "health-potion", name = "Health Potion", price = 10, consumable = consumable(effectIds = setOf("heal"))).toDto(),
                    equippableLootableItem(id = "hammer", name = "Hammer", price = 50, equippable = equippable(effectIds = setOf("maximum-health-20"), equipmentZone = WEAPON)).toDto(),
                    consumableLootableItem(id = "teleport-essence", consumable = consumable(effectIds = setOf("learn-teleport"))).toDto(),
                    consumableLootableItem(id = "bee-essence", consumable = consumable(effectIds = setOf("learn-bee"))).toDto(),
                ),
            units = listOf(unit(id = "knight", healthPoints = 100, abilities = listOf("bee"), startingGold = 20).toDto(), unit(id = "rat").toDto(), unit(id = "bee").toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            // The rats drop nothing, so the items lying on the battlefield are only the ones the test places, as dropped by a rat.
            lootTables = listOf(lootTable(id = "rat-loot", entries = listOf(nothingEntry())).toDto()),
            lootDrops = listOf(lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot")).toDto()),
            tiles = List(4) { List(4) { "sand" } },
            deployments =
                listOf(
                    Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1),
                    Deployment(battleUnitId = "rat-1", unitId = "rat", playerId = "cpu", row = 1, column = 3),
                ),
            playerOrder = listOf("human", "cpu"),
        ).also(SubdomainApis::initialise)
    private val eventBus = apis.eventBus
    private val inventoryView = FakeInventoryView()
    private val battleUnitInfoView = FakeBattleUnitInfoView()
    private val battleHudView = FakeBattleHudView()
    private val playerCallToActionView = FakePlayerCallToActionView()
    private val timeline = battleSceneTimeline()

    // The presenter is created once the battle has started, so it only reacts to what the test publishes.
    private val inventoryPresenter =
        InventoryPresenter(
            inventoryView = inventoryView,
            battleUnitInfoView = battleUnitInfoView,
            battleHudView = battleHudView,
            playerCallToActionView = playerCallToActionView,
            inventoryApi = apis.inventoryApi,
            lootableItemApi = apis.lootableItemApi,
            battleUnitApi = apis.battleUnitApi,
            playerApi = apis.playerApi,
            unitApi = apis.unitApi,
            battleApi = apis.battleApi,
            timeline = timeline,
            eventBus = eventBus,
        )

    private fun publish(event: DomainEvent) {
        eventBus.publish(event)
        eventBus.dispatch()
    }

    // The battle unit loots the item from its tile.
    private fun loot(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        apis.lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = lootableItemId, row = 1, column = 1)
        apis.inventoryApi.storeLootedItem(battleUnitId = battleUnitId, lootableItemId = lootableItemId, row = 1, column = 1)
        eventBus.dispatch()
    }

    private fun select(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) = publish(BattlefieldHudEvent.SelectedBattleUnit(tile = TileDto(row, column), battleUnitId = battleUnitId, tilesWhereCanBeMoved = emptySet()))

    // The player selects the knight and opens its bag.
    private fun openKnightInventory() {
        select("knight-1", row = 1, column = 1)
        battleUnitInfoView.onBagSelected.shouldNotBeNull().invoke()
        eventBus.dispatch()
    }

    // The knight summons a bee next to it, and returns the tile it stands on.
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
        playerCallToActionView.displayedItemActions
            .shouldNotBeNull()
            .actions
            .single { action -> action.label == label }
            .onSelected()
        eventBus.dispatch()
    }

    private fun fillKnightInventoryWithHealthPotions() = repeat(12) { loot("knight-1", "health-potion") }

    // The knight finds an item lying on (1,2) while its inventory is full.
    private fun findWhileFull(lootableItemId: String) {
        apis.lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = lootableItemId, row = 1, column = 2)
        apis.inventoryApi.storeLootedItem(battleUnitId = "knight-1", lootableItemId = lootableItemId, row = 1, column = 2)
        eventBus.dispatch()
    }

    @Test
    fun `DisplayBag should display the bag when a human battle unit is selected`() {
        // Given
        // When
        select("knight-1", row = 1, column = 1)
        // Then
        battleUnitInfoView.onBagSelected.shouldNotBeNull()
    }

    @Test
    fun `DisplayBag should not display the bag when a cpu battle unit is selected`() {
        // Given
        select("knight-1", row = 1, column = 1)
        // When
        select("rat-1", row = 1, column = 3)
        // Then
        battleUnitInfoView.onBagSelected.shouldBeNull()
    }

    @Test
    fun `DisplayBag should not display the bag when a summoned battle unit is selected`() {
        // Given
        val beeTile = summonBee()
        val beeBattleUnitId = apis.battlefieldApi.searchOccupant(row = beeTile.row, column = beeTile.column).shouldNotBeNull()
        select("knight-1", row = 1, column = 1)
        // When
        select(beeBattleUnitId, row = beeTile.row, column = beeTile.column)
        // Then
        battleUnitInfoView.onBagSelected.shouldBeNull()
    }

    @Test
    fun `DisplayBag should not display the bag when the battlefield hud is idle`() {
        // Given
        select("knight-1", row = 1, column = 1)
        // When
        publish(BattlefieldHudEvent.Idle)
        // Then
        battleUnitInfoView.onBagSelected.shouldBeNull()
    }

    @Test
    fun `OpenInventory should display the stored items marking the equipped one when the bag is clicked`() {
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
        battleHudView.displays.last() shouldBe Display.INVENTORY
    }

    @Test
    fun `OpenInventory should ignore the bag when the timeline is playing`() {
        // Given
        select("knight-1", row = 1, column = 1)
        timeline.playLongStep()
        // When
        battleUnitInfoView.onBagSelected.shouldNotBeNull().invoke()
        // Then
        battleHudView.displays.shouldBeEmpty()
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
    fun `SelectItem should frame the item and display its price and actions when a consumable is selected`() {
        // Given
        loot("knight-1", "health-potion")
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        inventoryView.selectedSlot shouldBe 0
        val itemActions = playerCallToActionView.displayedItemActions.shouldNotBeNull()
        itemActions.itemName shouldBe "Health Potion"
        itemActions.itemPrice shouldBe 10
        itemActions.actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Discard" to true, "Consume" to true)
    }

    @Test
    fun `SelectItem should enable consume when the battle unit can learn the ability the consumable teaches`() {
        // Given
        loot("knight-1", "teleport-essence")
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        val itemActions = playerCallToActionView.displayedItemActions.shouldNotBeNull()
        itemActions.actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Discard" to true, "Consume" to true)
    }

    @Test
    fun `SelectItem should disable consume when the battle unit already knows the ability the consumable teaches`() {
        // Given
        loot("knight-1", "bee-essence")
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        val itemActions = playerCallToActionView.displayedItemActions.shouldNotBeNull()
        itemActions.actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Discard" to true, "Consume" to false)
    }

    @Test
    fun `SelectItem should display equip when an equippable item that is not equipped is selected`() {
        // Given
        loot("knight-1", "hammer")
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        val itemActions = playerCallToActionView.displayedItemActions.shouldNotBeNull()
        itemActions.actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Discard" to true, "Equip" to true)
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
        val itemActions = playerCallToActionView.displayedItemActions.shouldNotBeNull()
        itemActions.actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Discard" to false, "Unequip" to true)
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
        val itemActions = playerCallToActionView.displayedItemActions.shouldNotBeNull()
        itemActions.actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Discard" to true, "Equip" to false)
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
        playerCallToActionView.displayedItemActions.shouldBeNull()
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
        apis.battleUnitApi.searchBattleUnitById("knight-1")?.maximumHealthPoints shouldBe 120
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
        battleHudView.displays.last() shouldBe Display.BATTLE_UNIT_INFO
        battleUnitInfoView.displayedBattleUnits
            .last()
            .battleUnit.maximumHealthPoints shouldBe 120
    }

    @Test
    fun `InventoryFull should open the inventory and ask what to do with the found item when an item is found while the inventory is full`() {
        // Given
        fillKnightInventoryWithHealthPotions()
        // When
        findWhileFull("hammer")
        // Then
        battleHudView.displays.last() shouldBe Display.INVENTORY
        inventoryView.displayedSlots.last().size shouldBe 12
        val inventoryFull = playerCallToActionView.displayedInventoryFull.shouldNotBeNull()
        inventoryFull.foundItemName shouldBe "Hammer"
        inventoryFull.foundItemPrice shouldBe 50
        inventoryFull.foundItemId shouldBe "hammer"
        timeline.isPlaying.shouldBeTrue()
    }

    @Test
    fun `InventoryFull should discard the tapped item and store the found one when a stored item is tapped`() {
        // Given
        fillKnightInventoryWithHealthPotions()
        findWhileFull("hammer")
        // When
        clickSlot(0)
        // Then
        apis.inventoryApi.searchInventoryByBattleUnitId("knight-1")?.storedItems shouldBe List(11) { "health-potion" } + "hammer"
        apis.lootDropApi.searchDroppedItems()[LootDrop.Dto.PositionDto(1, 2)].shouldBeNull()
        playerCallToActionView.displayedInventoryFull.shouldBeNull()
        timeline.isPlaying.shouldBeFalse()
    }

    @Test
    fun `InventoryFull should leave the found item on its tile when leave it is clicked`() {
        // Given
        fillKnightInventoryWithHealthPotions()
        findWhileFull("hammer")
        // When
        playerCallToActionView.displayedInventoryFull
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
        battleHudView.displays.last() shouldBe Display.INVENTORY
        playerCallToActionView.displayedInventoryFull.shouldNotBeNull()
    }

    @Test
    fun `SelectItem should not display the item actions during the turn of another player in a battle`() {
        // Given
        loot("knight-1", "health-potion")
        apis.battleApi.finishPlayerTurn()
        eventBus.dispatch()
        openKnightInventory()
        // When
        clickSlot(0)
        // Then
        playerCallToActionView.displayedItemActions.shouldBeNull()
    }

    @Test
    fun `ConsumeItem should consume the item out of the battles when the battle has finished while exploring`() {
        // Given
        loot("knight-1", "health-potion")
        // The battle finishes during the cpu turn, as the rat leaves it.
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
