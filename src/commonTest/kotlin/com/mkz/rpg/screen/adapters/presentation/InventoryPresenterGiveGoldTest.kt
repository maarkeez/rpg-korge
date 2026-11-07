package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.effect.domain.EffectMother.healEffect
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.party.domain.PartyEvent.ExplorationPaused
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.FakeBattleHudView.Display
import com.mkz.rpg.screen.adapters.presentation.FakeBattlefieldView.BattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.GiveGoldView.Member
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InventoryPresenterGiveGoldTest {
    // The human party explores a 4x4 sand battlefield, with no battle: the knight-1 leads at (1,1), followed by the viking-1
    // at (1,2) and the mage-1 at (2,1). The cpu rat-1 wanders at (3,3). Each starts with the gold of its unit, set per test.
    private fun apis(
        knightGold: Int = 120,
        vikingGold: Int = 100,
        mageGold: Int = 99_990,
        party: List<Pair<String, String>> = listOf("knight-1" to "knight", "viking-1" to "viking", "mage-1" to "mage"),
    ): SubdomainApis =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            effects = listOf(healEffect(id = "heal", healing = 10).toDto()),
            lootableItems = listOf(consumableLootableItem(id = "health-potion", price = 10, consumable = consumable(effectIds = setOf("heal"))).toDto()),
            units =
                listOf(
                    unit(id = "knight", startingGold = knightGold).toDto(),
                    unit(id = "viking", startingGold = vikingGold).toDto(),
                    unit(id = "mage", startingGold = mageGold).toDto(),
                    unit(id = "rat").toDto(),
                ),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            lootTables = listOf(lootTable(id = "rat-loot", entries = listOf(nothingEntry())).toDto()),
            lootDrops = listOf(lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot")).toDto()),
            tiles = List(4) { List(4) { "sand" } },
            deployments =
                party.mapIndexed { index, (battleUnitId, unitId) ->
                    Deployment(battleUnitId = battleUnitId, unitId = unitId, playerId = "human", row = 1 + index / 2, column = 1 + index % 2)
                } + Deployment(battleUnitId = "rat-1", unitId = "rat", playerId = "cpu", row = 3, column = 3),
            explore = true,
        ).also(SubdomainApis::initialise)

    private val knightTile = TilePosition(row = 1, column = 1)
    private val vikingTile = TilePosition(row = 1, column = 2)
    private val mageTile = TilePosition(row = 2, column = 1)

    private inner class Scene(
        val apis: SubdomainApis = apis(),
    ) {
        val eventBus = apis.eventBus
        val inventoryView = FakeInventoryView()
        val giveGoldView = FakeGiveGoldView()
        val battleHudView = FakeBattleHudView()
        val battleUnitInfoView = FakeBattleUnitInfoView()
        val playerCallToActionView = FakePlayerCallToActionView()
        val battlefieldView = FakeBattlefieldView()
        val inventoryPresenter =
            InventoryPresenter(
                inventoryView = inventoryView,
                giveGoldView = giveGoldView,
                battleUnitInfoView = battleUnitInfoView,
                battleHudView = battleHudView,
                playerCallToActionView = playerCallToActionView,
                inventoryApi = apis.inventoryApi,
                lootableItemApi = apis.lootableItemApi,
                battleUnitApi = apis.battleUnitApi,
                playerApi = apis.playerApi,
                battleApi = apis.battleApi,
                partyApi = apis.partyApi,
                battlefieldApi = apis.battlefieldApi,
                battlefieldView = battlefieldView,
                texts = TextsMother.texts(),
                timeline = battleSceneTimeline(),
                eventBus = eventBus,
            )

        fun publish(event: DomainEvent) {
            eventBus.publish(event)
            eventBus.dispatch()
        }

        // The player selects the knight and opens its bag.
        fun openKnightBag() {
            publish(BattlefieldHudEvent.SelectedBattleUnit(tile = TileDto(1, 1), battleUnitId = "knight-1", tilesWhereCanBeMoved = emptySet()))
            battleUnitInfoView.onBagSelected.shouldNotBeNull().invoke()
            eventBus.dispatch()
        }

        fun tapGold() {
            inventoryView.delegate.shouldNotBeNull().goldSelected()
            eventBus.dispatch()
        }

        fun openPanel() {
            openKnightBag()
            tapGold()
        }

        fun buttons() = playerCallToActionView.displayedGiveGold.shouldNotBeNull()

        fun give() {
            buttons().onGiven()
            eventBus.dispatch()
        }

        fun gold(battleUnitId: String): Int =
            apis.inventoryApi
                .searchInventoryByBattleUnitId(battleUnitId)
                .shouldNotBeNull()
                .gold

        fun displayedPanel() = giveGoldView.displays.last()
    }

    @Test
    fun `GoldSelected should open the panel with the first member with room, starting at 1, and dim every tile but the receivers'`() {
        // Given
        val scene = Scene()
        scene.openKnightBag()
        // When
        scene.tapGold()
        // Then
        val panel = scene.displayedPanel()
        panel.giver shouldBe Member(name = "Knight", unitId = "knight", gold = 120)
        panel.receiver shouldBe Member(name = "Viking", unitId = "viking", gold = 100)
        panel.amount shouldBe 1
        panel.maximum shouldBe 120
        panel.canStepToPrevious.shouldBeFalse()
        panel.canStepToNext.shouldBeTrue()
        scene.battleHudView.displays.last() shouldBe Display.GIVE_GOLD
        scene.buttons().amount shouldBe 1
        scene.battlefieldView.tilesNotDimmed.last() shouldBe setOf(vikingTile, mageTile)
        scene.battlefieldView.tileSelections.last() shouldBe vikingTile
    }

    @Test
    fun `GoldSelected should leave out the members at the limit`() {
        // Given
        val scene = Scene(apis(mageGold = 99_999))
        // When
        scene.openPanel()
        // Then
        scene.displayedPanel().canStepToNext.shouldBeFalse()
        scene.battlefieldView.tilesNotDimmed.last() shouldBe setOf(vikingTile)
    }

    @Test
    fun `GoldSelected should clear the selected item when one is selected`() {
        // Given
        val scene = Scene()
        scene.openKnightBag()
        scene.apis.lootDropApi.dropItemWithoutRolling(unitId = "rat", lootableItemId = "health-potion", row = 1, column = 1)
        scene.apis.inventoryApi.storeLootedItem(battleUnitId = "knight-1", lootableItemId = "health-potion", row = 1, column = 1)
        scene.eventBus.dispatch()
        scene.inventoryView.delegate
            .shouldNotBeNull()
            .slotSelected(0)
        scene.eventBus.dispatch()
        scene.inventoryView.selectedSlot shouldBe 0
        // When
        scene.tapGold()
        // Then
        scene.inventoryView.selectedSlot.shouldBeNull()
        scene.playerCallToActionView.displayedItemActions.shouldBeNull()
    }

    @Test
    fun `GoldSelected should display why, opening nothing, when the giver carries no gold`() {
        // Given
        val scene = Scene(apis(knightGold = 0))
        scene.openKnightBag()
        // When
        scene.tapGold()
        // Then
        scene.playerCallToActionView.displayedGiveGoldRefusal
            .shouldNotBeNull()
            .message shouldBe "No gold to give"
        scene.giveGoldView.displays.shouldBeEmpty()
        scene.battleHudView.displays.last() shouldBe Display.INVENTORY
    }

    @Test
    fun `GoldSelected should display why when the giver is alone, or every other member is at the limit`() {
        // Given
        val alone = Scene(apis(party = listOf("knight-1" to "knight")))
        alone.openKnightBag()
        // When
        alone.tapGold()
        // Then
        alone.playerCallToActionView.displayedGiveGoldRefusal
            .shouldNotBeNull()
            .message shouldBe "No one to give gold to"
        // Given
        val full = Scene(apis(vikingGold = 99_999, mageGold = 99_999))
        full.openKnightBag()
        // When
        full.tapGold()
        // Then
        full.playerCallToActionView.displayedGiveGoldRefusal
            .shouldNotBeNull()
            .message shouldBe "Everyone's gold is full"
    }

    @Test
    fun `GoldSelected should be ignored while giving an item`() {
        // Given
        val scene = Scene()
        scene.openKnightBag()
        scene.apis.lootDropApi.dropItemWithoutRolling(unitId = "rat", lootableItemId = "health-potion", row = 1, column = 1)
        scene.apis.inventoryApi.storeLootedItem(battleUnitId = "knight-1", lootableItemId = "health-potion", row = 1, column = 1)
        scene.eventBus.dispatch()
        scene.inventoryView.delegate
            .shouldNotBeNull()
            .slotSelected(0)
        scene.eventBus.dispatch()
        scene.playerCallToActionView.displayedItemActions
            .shouldNotBeNull()
            .actions
            .single { action -> action.label == "Give" }
            .onSelected()
        scene.eventBus.dispatch()
        // When
        scene.tapGold()
        // Then
        scene.giveGoldView.displays.shouldBeEmpty()
        scene.playerCallToActionView.displayedGiveItem.shouldNotBeNull()
    }

    @Test
    fun `OK should dismiss the reason`() {
        // Given
        val scene = Scene(apis(knightGold = 0))
        scene.openKnightBag()
        scene.tapGold()
        // When
        scene.playerCallToActionView.displayedGiveGoldRefusal
            .shouldNotBeNull()
            .onDismissed()
        // Then
        scene.playerCallToActionView.displayedGiveGoldRefusal.shouldBeNull()
    }

    @Test
    fun `InventoryClosed should dismiss the reason`() {
        // Given
        val scene = Scene(apis(knightGold = 0))
        scene.openKnightBag()
        scene.tapGold()
        // When
        scene.inventoryView.delegate
            .shouldNotBeNull()
            .inventoryClosed()
        scene.eventBus.dispatch()
        // Then
        scene.playerCallToActionView.displayedGiveGoldRefusal.shouldBeNull()
    }

    @Test
    fun `TileSelected should choose the receiver on the tapped tile, lowering the amount to fit`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        scene.buttons().onAll()
        // When
        val isTaken = scene.inventoryPresenter.tileSelected(row = mageTile.row, column = mageTile.column)
        // Then
        isTaken.shouldBeTrue()
        scene.displayedPanel().receiver.unitId shouldBe "mage"
        scene.displayedPanel().amount shouldBe 9
        scene.displayedPanel().maximum shouldBe 9
        scene.buttons().amount shouldBe 9
        scene.battlefieldView.tileSelections.last() shouldBe mageTile
        scene.battlefieldView.tilesNotDimmed.last() shouldBe setOf(vikingTile, mageTile)
    }

    @Test
    fun `TileSelected should take the taps on any other tile without changing anything`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        val displays = scene.giveGoldView.displays.size
        // When
        val isTakenOnGiver = scene.inventoryPresenter.tileSelected(row = knightTile.row, column = knightTile.column)
        val isTakenOnEmpty = scene.inventoryPresenter.tileSelected(row = 0, column = 0)
        // Then
        isTakenOnGiver.shouldBeTrue()
        isTakenOnEmpty.shouldBeTrue()
        scene.giveGoldView.displays.size shouldBe displays
    }

    @Test
    fun `ReceiverStepped should choose the next receiver, lowering the amount to fit`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        scene.buttons().onAll()
        // When
        scene.giveGoldView.delegate
            .shouldNotBeNull()
            .receiverStepped(1)
        // Then
        scene.displayedPanel().receiver.unitId shouldBe "mage"
        scene.displayedPanel().amount shouldBe 9
        scene.displayedPanel().canStepToPrevious.shouldBeTrue()
        scene.displayedPanel().canStepToNext.shouldBeFalse()
    }

    @Test
    fun `AmountChanged should store the amount and update the buttons, without displaying the panel again`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        val displays = scene.giveGoldView.displays.size
        // When
        scene.giveGoldView.delegate
            .shouldNotBeNull()
            .amountChanged(25)
        // Then
        scene.buttons().amount shouldBe 25
        scene.giveGoldView.displays.size shouldBe displays
    }

    @Test
    fun `All should set the lower of the gold and the receiver's room`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        // When
        scene.buttons().onAll()
        // Then
        scene.displayedPanel().amount shouldBe 120
        scene.buttons().amount shouldBe 120
    }

    @Test
    fun `Give should move the gold, display the bag again with the new gold, and flash the receiver`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        scene.giveGoldView.delegate
            .shouldNotBeNull()
            .amountChanged(25)
        // When
        scene.give()
        // Then
        scene.gold("knight-1") shouldBe 95
        scene.gold("viking-1") shouldBe 125
        scene.inventoryView.displayedGold shouldBe 95
        scene.inventoryView.displayedOwner
            .shouldNotBeNull()
            .canGiveGold
            .shouldBeTrue()
        scene.battleHudView.displays.last() shouldBe Display.INVENTORY
        scene.playerCallToActionView.displayedGiveGold.shouldBeNull()
        scene.battlefieldView.flashedBattleUnits.last() shouldBe BattleUnitAt(row = 1, column = 2, unitId = "viking")
        scene.battlefieldView.tileSelections.last() shouldBe knightTile
        scene.battlefieldView.tilesNotDimmed.last() shouldBe setOf(knightTile)
    }

    @Test
    fun `Give should fade the gold button when no gold is left`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        scene.buttons().onAll()
        // When
        scene.give()
        // Then
        scene.gold("knight-1") shouldBe 0
        scene.inventoryView.displayedOwner
            .shouldNotBeNull()
            .canGiveGold
            .shouldBeFalse()
    }

    @Test
    fun `Give should let the next give start straight away`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        scene.give()
        val displays = scene.giveGoldView.displays.size
        // When
        scene.tapGold()
        // Then
        scene.giveGoldView.displays.size shouldBe displays + 1
        scene.displayedPanel().giver.gold shouldBe 119
        scene.displayedPanel().receiver.gold shouldBe 101
    }

    @Test
    fun `Cancel should display the bag again with nothing moved, the giver selected`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        scene.giveGoldView.delegate
            .shouldNotBeNull()
            .amountChanged(25)
        // When
        scene.buttons().onCancelled()
        // Then
        scene.gold("knight-1") shouldBe 120
        scene.gold("viking-1") shouldBe 100
        scene.battleHudView.displays.last() shouldBe Display.INVENTORY
        scene.playerCallToActionView.displayedGiveGold.shouldBeNull()
        scene.battlefieldView.tileSelections.last() shouldBe knightTile
        scene.battlefieldView.tilesNotDimmed.last() shouldBe setOf(knightTile)
    }

    @Test
    fun `The bag should ignore its slots, arrows and close while the panel is on show`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        val hudDisplays = scene.battleHudView.displays.size
        // When
        scene.inventoryView.delegate
            .shouldNotBeNull()
            .slotSelected(0)
        scene.inventoryView.delegate
            .shouldNotBeNull()
            .inventoryOwnerStepped(1)
        scene.inventoryView.delegate
            .shouldNotBeNull()
            .inventoryClosed()
        scene.eventBus.dispatch()
        // Then
        scene.battleHudView.displays.size shouldBe hudDisplays
        scene.playerCallToActionView.displayedGiveGold.shouldNotBeNull()
    }

    @Test
    fun `Give gold should be abandoned when the exploration pauses, e g a battle starts`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        val resets = scene.battlefieldView.resetTilesCount
        // When
        scene.publish(ExplorationPaused(playerId = "human"))
        // Then
        scene.playerCallToActionView.displayedGiveGold.shouldBeNull()
        scene.battleHudView.displays.last() shouldBe Display.INVENTORY
        scene.battlefieldView.resetTilesCount shouldBe resets + 1
        scene.gold("knight-1") shouldBe 120
    }

    @Test
    fun `Give gold should be abandoned when the giver is defeated`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        // When
        scene.publish(BattleUnitDefeated(playerId = "human", battleUnitId = "knight-1", unitId = "knight", defeatedAtRow = 1, defeatedAtColumn = 1))
        // Then
        scene.playerCallToActionView.displayedGiveGold.shouldBeNull()
        scene.battleHudView.displays.last() shouldBe Display.INVENTORY
    }

    @Test
    fun `Give gold should choose the next receiver when the chosen one is defeated, or go back to the bag when none is left`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        // When
        scene.publish(BattleUnitDefeated(playerId = "human", battleUnitId = "viking-1", unitId = "viking", defeatedAtRow = 1, defeatedAtColumn = 2))
        // Then
        scene.displayedPanel().receiver.unitId shouldBe "mage"
        scene.displayedPanel().canStepToPrevious.shouldBeFalse()
        scene.displayedPanel().canStepToNext.shouldBeFalse()
        scene.battlefieldView.tilesNotDimmed.last() shouldBe setOf(mageTile)
        // When
        scene.publish(BattleUnitDefeated(playerId = "human", battleUnitId = "mage-1", unitId = "mage", defeatedAtRow = 2, defeatedAtColumn = 1))
        // Then
        scene.playerCallToActionView.displayedGiveGold.shouldBeNull()
        scene.battleHudView.displays.last() shouldBe Display.INVENTORY
    }

    @Test
    fun `Give gold should lower the amount when a reward takes room from the receiver`() {
        // Given
        val scene = Scene(apis(vikingGold = 99_000))
        scene.openPanel()
        scene.buttons().onAll()
        scene.displayedPanel().amount shouldBe 120
        // When
        scene.apis.inventoryApi.grantReward(battleUnitId = "viking-1", gold = 950, lootableItemIds = emptyList(), row = 1, column = 2)
        scene.eventBus.dispatch()
        // Then
        scene.displayedPanel().receiver.gold shouldBe 99_950
        scene.displayedPanel().amount shouldBe 49
        scene.displayedPanel().maximum shouldBe 49
        scene.buttons().amount shouldBe 49
    }

    @Test
    fun `Give gold should raise the maximum when a reward adds gold to the giver`() {
        // Given
        val scene = Scene()
        scene.openPanel()
        // When
        scene.apis.inventoryApi.grantReward(battleUnitId = "knight-1", gold = 30, lootableItemIds = emptyList(), row = 1, column = 2)
        scene.eventBus.dispatch()
        // Then
        scene.displayedPanel().giver.gold shouldBe 150
        scene.displayedPanel().maximum shouldBe 150
    }
}
