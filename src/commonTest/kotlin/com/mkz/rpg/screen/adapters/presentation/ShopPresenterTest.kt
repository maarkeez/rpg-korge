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
import io.kotest.matchers.nulls.shouldNotBeNull
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
    private val shopView = ShopView(BattleSceneResourcesLoader())
    private val battlefieldView = FakeBattlefieldView()
    private val playerCallToActionView = FakePlayerCallToActionView()
    private val timeline = battleSceneTimeline()
    private val presenter =
        ShopPresenter(
            shopView,
            battlefieldView,
            playerCallToActionView,
            apis.partyApi,
            apis.inventoryApi,
            apis.lootableItemApi,
            apis.battleUnitApi,
            apis.unitApi,
            apis.battlefieldApi,
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
        presenter.shopBuyerSwiped(1)
        presenter.shopSlotSelected(1)
        trade("Buy")
        presenter.close()
        beforeReopening()
        timeline.play { presenter.open(merchant, Mode.SELL) }
        presenter.shopBuyerSwiped(1)
    }

    private fun displayedItem() = playerCallToActionView.displayedItemActions.shouldNotBeNull()

    private fun displayedAction(label: String) = displayedItem().actions.single { action -> action.label == label }

    private fun trade(label: String) {
        displayedAction(label).onSelected()
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
        shopView.titleText() shouldBe "Buy from Merchant"
        shopView.mode() shouldBe Mode.BUY
        shopView.buyerText() shouldBe "Knight · 20 G · Bag 0/12"
        shopView.dotColors().shouldContainExactly(ShopView.BUYER_DOT_COLOR, ShopView.PARTY_MEMBER_DOT_COLOR)
        battlefieldView.tilesNotDimmed.last() shouldBe setOf(knightTile, merchantTile)
        battlefieldView.scrolledToTiles.last() shouldBe knightTile
        playerCallToActionView.visible.shouldBeFalse()
    }

    @Test
    fun `ShopSlotSelected should display the item price with Cancel, and Buy disabled when the buyer can not afford it`() {
        // Given
        openTheShop()
        // When
        presenter.shopSlotSelected(1)
        // Then
        shopView.selectedSlotIndexes().shouldContainExactly(1)
        playerCallToActionView.visible.shouldBeTrue()
        displayedItem().itemName shouldBe "Hammer"
        displayedItem().itemPrice shouldBe 50
        displayedItem().actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Buy" to false)
    }

    @Test
    fun `ShopSlotSelected should clear the selection and hide the call to action when an empty slot is selected`() {
        // Given
        openTheShop()
        presenter.shopSlotSelected(0)
        // When
        presenter.shopSlotSelected(5)
        // Then
        shopView.selectedSlotIndexes().shouldBeEmpty()
        playerCallToActionView.visible.shouldBeFalse()
    }

    @Test
    fun `Cancel should clear the selection and hide the call to action`() {
        // Given
        openTheShop()
        presenter.shopSlotSelected(0)
        // When
        trade("Cancel")
        // Then
        shopView.selectedSlotIndexes().shouldBeEmpty()
        playerCallToActionView.visible.shouldBeFalse()
    }

    @Test
    fun `ShopBuyerSwiped should switch the buyer, clear the selection, dim every tile but the new buyer's and the merchant's, and scroll to it`() {
        // Given
        openTheShop()
        presenter.shopSlotSelected(0)
        // When
        presenter.shopBuyerSwiped(1)
        // Then
        shopView.buyerText() shouldBe "Viking · 100 G · Bag 0/12"
        shopView.dotColors().shouldContainExactly(ShopView.PARTY_MEMBER_DOT_COLOR, ShopView.BUYER_DOT_COLOR)
        shopView.selectedSlotIndexes().shouldBeEmpty()
        playerCallToActionView.visible.shouldBeFalse()
        battlefieldView.tilesNotDimmed.last() shouldBe setOf(vikingTile, merchantTile)
        battlefieldView.scrolledToTiles.last() shouldBe vikingTile
    }

    @Test
    fun `ShopBuyerSwiped should stop at the ends of the party`() {
        // Given
        openTheShop()
        // When
        presenter.shopBuyerSwiped(-1)
        // Then
        shopView.buyerText() shouldBe "Knight · 20 G · Bag 0/12"
        // When
        presenter.shopBuyerSwiped(1)
        presenter.shopBuyerSwiped(1)
        // Then
        shopView.buyerText() shouldBe "Viking · 100 G · Bag 0/12"
    }

    @Test
    fun `ShopBuyerSelected should switch to the party member of the tapped dot`() {
        // Given
        openTheShop()
        // When
        presenter.shopBuyerSelected(1)
        // Then
        shopView.buyerText() shouldBe "Viking · 100 G · Bag 0/12"
    }

    @Test
    fun `Buy should pay and store the item, keeping it selected with Buy checked again`() {
        // Given
        openTheShop()
        presenter.shopBuyerSwiped(1)
        presenter.shopSlotSelected(1)
        // When
        trade("Buy")
        // Then
        shopView.buyerText() shouldBe "Viking · 50 G · Bag 1/12"
        shopView.selectedSlotIndexes().shouldContainExactly(1)
        displayedAction("Buy").isEnabled.shouldBeTrue()
        // When
        trade("Buy")
        // Then
        shopView.buyerText() shouldBe "Viking · 0 G · Bag 2/12"
        displayedAction("Buy").isEnabled.shouldBeFalse()
    }

    @Test
    fun `Open should display the buyer's items to sell`() {
        // When
        reopenToSellTheVikingsHammer()
        // Then
        shopView.titleText() shouldBe "Sell to Merchant"
        shopView.mode() shouldBe Mode.SELL
        shopView.selectedSlotIndexes().shouldBeEmpty()
        // When
        presenter.shopSlotSelected(0)
        // Then
        displayedItem().itemName shouldBe "Hammer"
    }

    @Test
    fun `Sell should receive half the price and clear the selection`() {
        // Given
        reopenToSellTheVikingsHammer()
        presenter.shopSlotSelected(0)
        // Then
        displayedItem().itemPrice shouldBe 25
        displayedItem().actions.map { action -> action.label to action.isEnabled }.shouldContainExactly("Cancel" to true, "Sell" to true)
        // When
        trade("Sell")
        // Then
        shopView.buyerText() shouldBe "Viking · 75 G · Bag 0/12"
        shopView.selectedSlotIndexes().shouldBeEmpty()
        playerCallToActionView.visible.shouldBeFalse()
    }

    @Test
    fun `Sell should be disabled for the only copy of an equipped item, framed in gold`() {
        // Given
        reopenToSellTheVikingsHammer {
            apis.inventoryApi.equipItem("viking-1", "hammer")
            eventBus.dispatch()
        }
        // When
        presenter.shopSlotSelected(0)
        // Then
        shopView.equippedSlotIndexes().shouldContainExactly(0)
        displayedAction("Sell").isEnabled.shouldBeFalse()
    }

    @Test
    fun `ShopSlotSelected should do nothing while the scene plays the previous steps`() {
        // Given
        openTheShop()
        timeline.playLongStep()
        // When
        presenter.shopSlotSelected(0)
        // Then
        playerCallToActionView.displayedItemActions.shouldBeNull()
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
    fun `Close should hide the shop and the call to action, restore the tiles, and display the finish turn again`() {
        // Given
        openTheShop()
        presenter.shopSlotSelected(0)
        val resetTilesCount = battlefieldView.resetTilesCount
        // When
        presenter.close()
        // Then
        presenter.isOpen.shouldBeFalse()
        shopView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeFalse()
        battlefieldView.resetTilesCount shouldBe resetTilesCount + 1
        playerCallToActionView.displayedItemActions.shouldBeNull()
    }
}
