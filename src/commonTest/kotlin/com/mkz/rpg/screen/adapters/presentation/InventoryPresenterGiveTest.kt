package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.effect.domain.EffectMother.healEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseMaximumHealthEffect
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippableLootableItem
import com.mkz.rpg.party.domain.PartyEvent.ExplorationInterrupted
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.FakeBattlefieldView.BattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.GameSceneTimelineMother.gameSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.InventoryView.Slot
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionEvent
import com.mkz.rpg.shared.adapters.presentation.GameButton
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

class InventoryPresenterGiveTest {
    // The human party explores a 4x4 sand battlefield, with no battle: the knight-1 leads at (1,1), followed by the viking-1
    // at (1,2) and the mage-1 at (2,1). The cpu rat-1 wanders at (3,3). The health potion and the hammer (50 G) lie
    // around, dropped by a rat.
    private val apis =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            effects = listOf(healEffect(id = "heal", healing = 10).toDto(), increaseMaximumHealthEffect(id = "maximum-health-20", maximumHealth = 20).toDto()),
            lootableItems =
                listOf(
                    consumableLootableItem(id = "health-potion", price = 10, consumable = consumable(effectIds = setOf("heal"))).toDto(),
                    equippableLootableItem(id = "hammer", price = 50, equippable = equippable(effectIds = setOf("maximum-health-20"), equipmentZone = WEAPON)).toDto(),
                ),
            units = listOf(unit(id = "knight").toDto(), unit(id = "viking").toDto(), unit(id = "mage").toDto(), unit(id = "rat").toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            lootTables = listOf(lootTable(id = "rat-loot", entries = listOf(nothingEntry())).toDto()),
            lootDrops = listOf(lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot")).toDto()),
            tiles = List(4) { List(4) { "sand" } },
            deployments =
                listOf(
                    Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1),
                    Deployment(battleUnitId = "viking-1", unitId = "viking", playerId = "human", row = 1, column = 2),
                    Deployment(battleUnitId = "mage-1", unitId = "mage", playerId = "human", row = 2, column = 1),
                    Deployment(battleUnitId = "rat-1", unitId = "rat", playerId = "cpu", row = 3, column = 3),
                ),
            explore = true,
        ).also(SubdomainApis::initialise)
    private val eventBus = apis.eventBus
    private val inventoryView = FakeInventoryView()
    private val battleUnitInfoView = FakeBattleUnitInfoView()
    private val callToActionView = FakeCallToActionView()
    private val battlefieldView = FakeBattlefieldView()
    private val timeline = gameSceneTimeline()

    private val inventoryPresenter =
        InventoryPresenter(
            inventoryView = inventoryView,
            giveGoldView = FakeGiveGoldView(),
            battleUnitInfoView = battleUnitInfoView,
            hudView = FakeHudView(),
            callToActionView = callToActionView,
            inventoryApi = apis.inventoryApi,
            lootableItemApi = apis.lootableItemApi,
            battleUnitApi = apis.battleUnitApi,
            playerApi = apis.playerApi,
            battleApi = apis.battleApi,
            partyApi = apis.partyApi,
            battlefieldApi = apis.battlefieldApi,
            battlefieldView = battlefieldView,
            messages = MessagesMother.messages(),
            timeline = timeline,
            eventBus = eventBus,
        )

    private val knightTile = TilePosition(row = 1, column = 1)
    private val vikingTile = TilePosition(row = 1, column = 2)
    private val mageTile = TilePosition(row = 2, column = 1)

    // The battle unit loots the item, as if it was lying on its tile.
    private fun loot(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        val position = apis.battlefieldApi.searchPosition(battleUnitId).shouldNotBeNull()
        apis.lootDropApi.dropItemWithoutRolling(unitId = "rat", lootableItemId = lootableItemId, row = position.row, column = position.column)
        apis.inventoryApi.storeLootedItem(battleUnitId = battleUnitId, lootableItemId = lootableItemId, row = position.row, column = position.column)
        eventBus.dispatch()
    }

    private fun fillInventory(battleUnitId: String) = repeat(12) { loot(battleUnitId, "health-potion") }

    private fun publish(event: DomainEvent) {
        eventBus.publish(event)
        eventBus.dispatch()
    }

    // The player selects the knight, opens its inventory and taps its first item.
    private fun selectKnightFirstItem() {
        publish(SelectionEvent.BattleUnitSelected(tile = TileDto(1, 1), battleUnitId = "knight-1", walkingRange = emptySet()))
        battleUnitInfoView.onInventoryButtonSelected.shouldNotBeNull().invoke()
        eventBus.dispatch()
        inventoryView.delegate.shouldNotBeNull().slotSelected(0)
        eventBus.dispatch()
    }

    private fun giveAction() =
        callToActionView.displayedItemButtons
            .shouldNotBeNull()
            .actions
            .single { action -> action.label in setOf("Give", "No one", "No space") }

    private fun clickGive() {
        giveAction().onSelected()
        eventBus.dispatch()
    }

    private fun tapTile(tile: TilePosition): Boolean {
        val isTaken = inventoryPresenter.tileSelected(row = tile.row, column = tile.column)
        eventBus.dispatch()
        return isTaken
    }

    private fun storedItems(battleUnitId: String) =
        apis.inventoryApi
            .searchInventoryByBattleUnitId(battleUnitId)
            .shouldNotBeNull()
            .storedItems

    @Test
    fun `SelectItem should offer give between discard and consume while exploring`() {
        // Given
        loot("knight-1", "health-potion")
        // When
        selectKnightFirstItem()
        // Then
        callToActionView.displayedItemButtons
            .shouldNotBeNull()
            .actions
            .map { action -> action.label to action.isEnabled }
            .shouldContainExactly("Cancel" to true, "Discard" to true, "Give" to true, "Consume" to true)
        callToActionView.displayedItemButtons!!
            .actions
            .map { action -> action.color }
            .shouldContainExactly(GameButton.GREY, GameButton.GREY, GameButton.GREY, ConfirmButton.COLOR)
    }

    @Test
    fun `SelectItem should disable give for the only stored copy of an equipped item`() {
        // Given
        loot("knight-1", "hammer")
        apis.inventoryApi.equipItem(battleUnitId = "knight-1", lootableItemId = "hammer")
        eventBus.dispatch()
        // When
        selectKnightFirstItem()
        // Then
        giveAction().isEnabled.shouldBeFalse()
    }

    @Test
    fun `SelectItem should disable give, reading why, when every other inventory is full`() {
        // Given
        loot("knight-1", "hammer")
        fillInventory("viking-1")
        fillInventory("mage-1")
        // When
        selectKnightFirstItem()
        // Then
        giveAction().label shouldBe "No space"
        giveAction().isEnabled.shouldBeFalse()
    }

    @Test
    fun `Give should dim every tile but the ones of the members that can receive the item, and ask to tap one`() {
        // Given
        loot("knight-1", "hammer")
        fillInventory("mage-1")
        selectKnightFirstItem()
        // When
        clickGive()
        // Then
        battlefieldView.tilesNotDimmed.last() shouldBe setOf(vikingTile)
        callToActionView.displayedGiveItem.shouldNotBeNull().itemName shouldBe "Hammer"
        callToActionView.displayedItemButtons.shouldBeNull()
    }

    @Test
    fun `TileSelected should give the item to the member on the tapped tile, keeping the giver's inventory open`() {
        // Given
        loot("knight-1", "hammer")
        loot("knight-1", "health-potion")
        selectKnightFirstItem()
        clickGive()
        // When
        val isTaken = tapTile(vikingTile)
        // Then
        isTaken.shouldBeTrue()
        storedItems("knight-1").shouldContainExactly("health-potion")
        storedItems("viking-1").shouldContainExactly("hammer")
        inventoryView.displayedSlots.last().shouldContainExactly(Slot("health-potion", isEquipped = false))
        inventoryView.selectedSlot.shouldBeNull()
        callToActionView.displayedGiveItem.shouldBeNull()
        battlefieldView.flashedBattleUnits.last() shouldBe BattleUnitAt(row = 1, column = 2, unitId = "viking")
        // The tiles look as when the giver was selected.
        battlefieldView.tileSelections.last() shouldBe knightTile
        battlefieldView.tilesNotDimmed.last() shouldBe setOf(knightTile)
    }

    @Test
    fun `TileSelected should take the taps on any other tile without giving the item`() {
        // Given
        loot("knight-1", "hammer")
        fillInventory("mage-1")
        selectKnightFirstItem()
        clickGive()
        // When
        val takenTaps = listOf(knightTile, mageTile, TilePosition(row = 0, column = 0), TilePosition(row = 3, column = 3)).map(::tapTile)
        // Then
        takenTaps.shouldContainExactly(true, true, true, true)
        storedItems("knight-1").shouldContainExactly("hammer")
        callToActionView.displayedGiveItem.shouldNotBeNull()
    }

    @Test
    fun `TileSelected should not take the taps when not giving`() {
        // Given
        loot("knight-1", "hammer")
        selectKnightFirstItem()
        // Then
        tapTile(vikingTile).shouldBeFalse()
    }

    @Test
    fun `Give should ignore the inventory's slots and close until the item is given or cancelled`() {
        // Given
        loot("knight-1", "hammer")
        loot("knight-1", "health-potion")
        selectKnightFirstItem()
        clickGive()
        // When
        inventoryView.delegate.shouldNotBeNull().slotSelected(1)
        inventoryView.delegate.shouldNotBeNull().inventoryClosed()
        eventBus.dispatch()
        // Then
        inventoryView.selectedSlot shouldBe 0
        callToActionView.displayedGiveItem.shouldNotBeNull()
    }

    @Test
    fun `Cancel should display the item's actions again, with the giver selected`() {
        // Given
        loot("knight-1", "hammer")
        selectKnightFirstItem()
        clickGive()
        // When
        callToActionView.displayedGiveItem.shouldNotBeNull().onCancelled()
        eventBus.dispatch()
        // Then
        callToActionView.displayedItemButtons.shouldNotBeNull().itemName shouldBe "Hammer"
        battlefieldView.tilesNotDimmed.last() shouldBe setOf(knightTile)
        tapTile(vikingTile).shouldBeFalse()
        storedItems("viking-1").shouldBeEmpty()
    }

    @Test
    fun `Give should be abandoned when the exploration is interrupted, e g a battle starts`() {
        // Given
        loot("knight-1", "hammer")
        selectKnightFirstItem()
        clickGive()
        // When
        publish(ExplorationInterrupted(playerId = "human"))
        // Then
        callToActionView.displayedGiveItem.shouldBeNull()
        tapTile(vikingTile).shouldBeFalse()
    }
}
