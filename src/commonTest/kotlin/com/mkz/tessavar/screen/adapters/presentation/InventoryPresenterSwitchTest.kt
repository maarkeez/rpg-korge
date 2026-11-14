package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.tessavar.effect.domain.EffectMother.healEffect
import com.mkz.tessavar.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.tessavar.lootTable.domain.LootTableMother.lootTable
import com.mkz.tessavar.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.consumable
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.screen.adapters.presentation.FakeHudView.Display
import com.mkz.tessavar.screen.adapters.presentation.GameSceneTimelineMother.gameSceneTimeline
import com.mkz.tessavar.screen.adapters.presentation.GameSceneTimelineMother.playLongStep
import com.mkz.tessavar.screen.adapters.presentation.InventoryView.Owner
import com.mkz.tessavar.screen.adapters.presentation.InventoryView.Slot
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.Dto.TileDto
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionEvent
import com.mkz.tessavar.shared.adapters.presentation.SubdomainApis
import com.mkz.tessavar.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InventoryPresenterSwitchTest {
    private val apis =
        SubdomainApis(
            terrains = listOf(terrainThatCanBeOccupied(id = "sand").toDto()),
            effects = listOf(healEffect(id = "heal", healing = 10).toDto()),
            lootableItems = listOf(consumableLootableItem(id = "health-potion", price = 10, consumable = consumable(effectIds = setOf("heal"))).toDto()),
            units =
                listOf(
                    unit(id = "knight", startingGold = 20).toDto(),
                    unit(id = "viking", startingGold = 30).toDto(),
                    unit(id = "mage").toDto(),
                    unit(id = "rat").toDto(),
                ),
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
    private val hudView = FakeHudView()
    private val callToActionView = FakeCallToActionView()
    private val timeline = gameSceneTimeline()

    private val selectedBattleUnitIds = mutableListOf<String>()

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
        ).apply {
            setBattleUnitSelector { battleUnitId ->
                selectedBattleUnitIds += battleUnitId
                val position = apis.battlefieldApi.searchPosition(battleUnitId).shouldNotBeNull()
                eventBus.publish(SelectionEvent.BattleUnitSelected(tile = TileDto(position.row, position.column), battleUnitId = battleUnitId, walkingRange = emptySet()))
            }
        }

    private fun loot(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        val position = apis.battlefieldApi.searchPosition(battleUnitId).shouldNotBeNull()
        apis.lootDropApi.dropItemWithoutRolling(unitId = "rat", lootableItemId = lootableItemId, row = position.row, column = position.column)
        apis.inventoryApi.storeLootedItem(battleUnitId = battleUnitId, lootableItemId = lootableItemId, row = position.row, column = position.column)
        eventBus.dispatch()
    }

    private fun openInventoryOf(battleUnitId: String) {
        val position = apis.battlefieldApi.searchPosition(battleUnitId).shouldNotBeNull()
        eventBus.publish(SelectionEvent.BattleUnitSelected(tile = TileDto(position.row, position.column), battleUnitId = battleUnitId, walkingRange = emptySet()))
        eventBus.dispatch()
        battleUnitInfoView.onInventoryButtonSelected.shouldNotBeNull().invoke()
        eventBus.dispatch()
    }

    private fun step(step: Int) {
        inventoryView.delegate.shouldNotBeNull().inventoryOwnerStepped(step)
        eventBus.dispatch()
    }

    @Test
    fun `OpenInventory should open the inventory straight away when no ability is displayed`() {
        // Given
        // When
        openInventoryOf("knight-1")
        // Then
        selectedBattleUnitIds.shouldBeEmpty()
        hudView.displays.last() shouldBe Display.INVENTORY
    }

    @Test
    fun `OpenInventory should name the party member and its position in the party while exploring`() {
        // Given
        // When
        openInventoryOf("knight-1")
        // Then
        inventoryView.displayedOwner shouldBe Owner(name = "Knight", partyIndex = 0, partySize = 3, canGiveGold = true)
    }

    @Test
    fun `InventoryOwnerStepped should select the next party member on the battlefield and display its inventory`() {
        // Given
        loot("viking-1", "health-potion")
        openInventoryOf("knight-1")
        hudView.displays.clear()
        // When
        step(1)
        // Then
        selectedBattleUnitIds.shouldContainExactly("viking-1")
        inventoryView.displayedSlots.last().shouldContainExactly(Slot("health-potion", isEquipped = false))
        inventoryView.displayedGold shouldBe 30
        inventoryView.displayedOwner shouldBe Owner(name = "Viking", partyIndex = 1, partySize = 3, canGiveGold = true)
        hudView.displays.last() shouldBe Display.INVENTORY
    }

    @Test
    fun `InventoryOwnerStepped should select the previous party member`() {
        // Given
        openInventoryOf("mage-1")
        // When
        step(-1)
        // Then
        selectedBattleUnitIds.shouldContainExactly("viking-1")
        inventoryView.displayedOwner shouldBe Owner(name = "Viking", partyIndex = 1, partySize = 3, canGiveGold = true)
    }

    @Test
    fun `InventoryOwnerStepped should stop at the ends of the party`() {
        // Given
        openInventoryOf("knight-1")
        // When
        step(-1)
        // Then
        selectedBattleUnitIds.shouldBeEmpty()
        inventoryView.displayedOwner shouldBe Owner(name = "Knight", partyIndex = 0, partySize = 3, canGiveGold = true)
    }

    @Test
    fun `InventoryOwnerStepped should do nothing while the scene plays the previous steps`() {
        // Given
        openInventoryOf("knight-1")
        timeline.playLongStep()
        // When
        step(1)
        // Then
        selectedBattleUnitIds.shouldBeEmpty()
    }

    @Test
    fun `InventoryOwnerStepped should do nothing while giving an item`() {
        // Given
        loot("knight-1", "health-potion")
        openInventoryOf("knight-1")
        inventoryView.delegate.shouldNotBeNull().slotSelected(0)
        eventBus.dispatch()
        callToActionView.displayedItemButtons
            .shouldNotBeNull()
            .actions
            .single { action -> action.label == "Give" }
            .onSelected()
        eventBus.dispatch()
        // When
        step(1)
        // Then
        selectedBattleUnitIds.shouldBeEmpty()
    }

    @Test
    fun `BattleUnitSelected should display the info of a party member selected on its tile once the inventory has switched`() {
        // Given
        openInventoryOf("knight-1")
        step(1)
        hudView.displays.clear()
        val displayCount = inventoryView.displayedSlots.size
        // When
        val mageTile = apis.battlefieldApi.searchPosition("mage-1").shouldNotBeNull()
        eventBus.publish(SelectionEvent.BattleUnitSelected(tile = TileDto(mageTile.row, mageTile.column), battleUnitId = "mage-1", walkingRange = emptySet()))
        eventBus.dispatch()
        // Then
        hudView.displays.shouldBeEmpty()
        inventoryView.displayedSlots.size shouldBe displayCount
    }
}
