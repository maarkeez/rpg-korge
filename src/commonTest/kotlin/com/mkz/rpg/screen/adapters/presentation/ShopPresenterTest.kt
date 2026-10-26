package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseMaximumHealthEffect
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.WEAPON
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippableLootableItem
import com.mkz.rpg.npc.domain.NpcMother.npc
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.screen.adapters.presentation.ShopView.Mode
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ShopPresenterTest {
    // The knight (20 G) leads from (1,1), followed by the viking (100 G) at (2,1). The merchant stands north of the knight,
    // at (0,1), selling health potions (10 G) and hammers (50 G). There is no battle.
    private val merchant = npc(id = "merchant", name = "Merchant", line = "How can I help you?", row = 0, column = 1, stock = listOf("health-potion", "hammer")).toDto()
    private val apis =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            effects = listOf(increaseHealthEffect(id = "cure").toDto(), increaseMaximumHealthEffect(id = "sturdy").toDto()),
            lootableItems =
                listOf(
                    consumableLootableItem(id = "health-potion", name = "Health Potion", price = 10, consumable = consumable(effectIds = setOf("cure"))).toDto(),
                    equippableLootableItem(id = "hammer", name = "Hammer", price = 50, equippable = equippable(effectIds = setOf("sturdy"), equipmentZone = WEAPON)).toDto(),
                ),
            units = listOf(unit(id = "knight", name = "Knight", startingGold = 20).toDto(), unit(id = "viking", name = "Viking", startingGold = 100).toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            tiles = List(3) { List(3) { "sand" } },
            deployments =
                listOf(
                    Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1),
                    Deployment(battleUnitId = "viking-1", unitId = "viking", playerId = "human", row = 2, column = 1),
                ),
            npcs = listOf(merchant),
        ).also(SubdomainApis::initialise)
    private val eventBus = apis.eventBus
    private val shopView = ShopView(BattleSceneResourcesLoader(), TextsMother.texts())
    private val battlefieldView = FakeBattlefieldView()
    private val timeline = battleSceneTimeline()
    private val presenter =
        ShopPresenter(
            shopView,
            battlefieldView,
            apis.partyApi,
            apis.inventoryApi,
            apis.lootableItemApi,
            apis.battleUnitApi,
            apis.unitApi,
            apis.battlefieldApi,
            TextsMother.texts(),
            timeline,
            eventBus,
        )

    private val knightTile = PositionDto(row = 1, column = 1)
    private val vikingTile = PositionDto(row = 2, column = 1)
    private val merchantTile = PositionDto(row = 0, column = 1)

    // The party talks to the merchant, and its shop opens to buy, or to sell.
    private fun openTheShop(mode: Mode = Mode.BUY) {
        apis.partyApi.startExploration()
        eventBus.dispatch()
        apis.npcApi.talkToAdjacentNpc()
        eventBus.dispatch()
        timeline.play { presenter.open(merchant, mode) }
    }

    // The viking buys a hammer, and the shop opens again to sell, with the viking as buyer.
    private fun reopenToSellTheVikingsHammer(beforeReopening: () -> Unit = {}) {
        openTheShop()
        presenter.shopBuyerStepped(1)
        presenter.shopSlotSelected(1)
        trade()
        presenter.close()
        beforeReopening()
        timeline.play { presenter.open(merchant, Mode.SELL) }
        presenter.shopBuyerStepped(1)
    }

    private fun trade() {
        presenter.shopActionSelected()
        eventBus.dispatch()
    }

    @Test
    fun `Init should hide the shop when is created`() {
        // Then
        shopView.visible.shouldBeFalse()
        presenter.isOpen.shouldBeFalse()
    }

    @Test
    fun `Open should display the merchant's items to buy, with the leader as buyer, and dim every tile but theirs`() {
        // When
        openTheShop()
        // Then
        presenter.isOpen.shouldBeTrue()
        shopView.visible.shouldBeTrue()
        shopView.buyerText() shouldBe "Knight · 20 G · Bag 0/12"
        shopView.arrowsEnabled() shouldBe (false to true)
        shopView.footerText() shouldBe "Tap an item to buy"
        shopView.action().shouldBeNull()
        battlefieldView.tilesNotDimmed.last() shouldBe setOf(knightTile, merchantTile)
        battlefieldView.scrolledToTiles.last() shouldBe knightTile
    }

    @Test
    fun `ShopSlotSelected should display the item price, and Buy disabled reading why when the buyer can not afford it`() {
        // Given
        openTheShop()
        // When
        presenter.shopSlotSelected(1)
        // Then
        shopView.selectedSlotIndexes().shouldContainExactly(1)
        shopView.footerText() shouldBe "Hammer"
        shopView.footerPrice() shouldBe "50 G"
        shopView.action() shouldBe ("Not enough gold" to false)
    }

    @Test
    fun `Open should dim the items the buyer can not afford`() {
        // When
        openTheShop()
        // Then
        shopView.dimmedSlotIndexes().shouldContainExactly(1)
        // When
        presenter.shopBuyerStepped(1)
        // Then
        shopView.dimmedSlotIndexes().shouldBeEmpty()
    }

    @Test
    fun `ShopSlotSelected should clear the selection and display the hint again when an empty slot is selected`() {
        // Given
        openTheShop()
        presenter.shopSlotSelected(0)
        // When
        presenter.shopSlotSelected(5)
        // Then
        shopView.selectedSlotIndexes().shouldBeEmpty()
        shopView.footerText() shouldBe "Tap an item to buy"
        shopView.action().shouldBeNull()
    }

    @Test
    fun `ShopSlotSelected should clear the selection when the selected item is selected again`() {
        // Given
        openTheShop()
        presenter.shopSlotSelected(0)
        // When
        presenter.shopSlotSelected(0)
        // Then
        shopView.selectedSlotIndexes().shouldBeEmpty()
        shopView.footerText() shouldBe "Tap an item to buy"
        // When
        presenter.shopSlotSelected(0)
        // Then
        shopView.selectedSlotIndexes().shouldContainExactly(0)
    }

    @Test
    fun `ShopBuyerStepped should switch the buyer, clear the selection, dim every tile but the new buyer's and the merchant's, and scroll to it`() {
        // Given
        openTheShop()
        presenter.shopSlotSelected(0)
        // When
        presenter.shopBuyerStepped(1)
        // Then
        shopView.buyerText() shouldBe "Viking · 100 G · Bag 0/12"
        shopView.arrowsEnabled() shouldBe (true to false)
        shopView.selectedSlotIndexes().shouldBeEmpty()
        shopView.action().shouldBeNull()
        battlefieldView.tilesNotDimmed.last() shouldBe setOf(vikingTile, merchantTile)
        battlefieldView.scrolledToTiles.last() shouldBe vikingTile
    }

    @Test
    fun `ShopBuyerStepped should stop at the ends of the party`() {
        // Given
        openTheShop()
        // When
        presenter.shopBuyerStepped(-1)
        // Then
        shopView.buyerText() shouldBe "Knight · 20 G · Bag 0/12"
        // When
        presenter.shopBuyerStepped(1)
        presenter.shopBuyerStepped(1)
        // Then
        shopView.buyerText() shouldBe "Viking · 100 G · Bag 0/12"
    }

    @Test
    fun `ShopActionSelected should buy the item, keeping it selected with Buy checked again`() {
        // Given
        openTheShop()
        presenter.shopBuyerStepped(1)
        presenter.shopSlotSelected(1)
        // When
        trade()
        // Then
        shopView.buyerText() shouldBe "Viking · 50 G · Bag 1/12"
        shopView.selectedSlotIndexes().shouldContainExactly(1)
        shopView.action() shouldBe ("Buy" to true)
        // When
        trade()
        // Then
        shopView.buyerText() shouldBe "Viking · 0 G · Bag 2/12"
        shopView.action() shouldBe ("Not enough gold" to false)
        shopView.dimmedSlotIndexes().shouldContainExactly(0, 1)
    }

    @Test
    fun `ShopActionSelected should do nothing when the buyer can not afford the item`() {
        // Given
        openTheShop()
        presenter.shopSlotSelected(1)
        // When
        trade()
        // Then
        shopView.buyerText() shouldBe "Knight · 20 G · Bag 0/12"
    }

    @Test
    fun `Open should display the buyer's items to sell`() {
        // When
        reopenToSellTheVikingsHammer()
        // Then
        shopView.selectedSlotIndexes().shouldBeEmpty()
        shopView.footerText() shouldBe "Tap an item to sell"
        // When
        presenter.shopSlotSelected(0)
        // Then
        shopView.footerText() shouldBe "Hammer"
    }

    @Test
    fun `Open should display there is nothing to sell when the buyer stores no item`() {
        // When
        openTheShop(Mode.SELL)
        // Then
        shopView.footerText() shouldBe "Nothing to sell"
    }

    @Test
    fun `ShopActionSelected should sell the item for half the price and clear the selection`() {
        // Given
        reopenToSellTheVikingsHammer()
        presenter.shopSlotSelected(0)
        // Then
        shopView.footerPrice() shouldBe "25 G"
        shopView.action() shouldBe ("Sell" to true)
        // When
        trade()
        // Then
        shopView.buyerText() shouldBe "Viking · 75 G · Bag 0/12"
        shopView.selectedSlotIndexes().shouldBeEmpty()
        shopView.footerText() shouldBe "Nothing to sell"
    }

    @Test
    fun `Sell should be disabled reading Equipped for the only copy of an equipped item, framed in gold and dimmed`() {
        // Given
        reopenToSellTheVikingsHammer {
            apis.inventoryApi.equipItem("viking-1", "hammer")
            eventBus.dispatch()
        }
        // When
        presenter.shopSlotSelected(0)
        // Then
        shopView.equippedSlotIndexes().shouldContainExactly(0)
        shopView.dimmedSlotIndexes().shouldContainExactly(0)
        shopView.action() shouldBe ("Equipped" to false)
    }

    @Test
    fun `ShopSlotSelected should do nothing while the scene plays the previous steps`() {
        // Given
        openTheShop()
        timeline.playLongStep()
        // When
        presenter.shopSlotSelected(0)
        // Then
        shopView.selectedSlotIndexes().shouldBeEmpty()
    }

    @Test
    fun `ShopBackSelected should call the back listener, still talking to the merchant`() {
        // Given
        var backs = 0
        presenter.setOnBack { backs++ }
        openTheShop()
        // When
        presenter.shopBackSelected()
        eventBus.dispatch()
        // Then
        backs shouldBe 1
        apis.partyApi.searchParty()!!.talkingNpcId shouldBe "merchant"
    }

    @Test
    fun `Close should hide the shop and restore the tiles`() {
        // Given
        openTheShop()
        presenter.shopSlotSelected(0)
        val resetTilesCount = battlefieldView.resetTilesCount
        // When
        presenter.close()
        // Then
        presenter.isOpen.shouldBeFalse()
        shopView.visible.shouldBeFalse()
        battlefieldView.resetTilesCount shouldBe resetTilesCount + 1
    }
}
