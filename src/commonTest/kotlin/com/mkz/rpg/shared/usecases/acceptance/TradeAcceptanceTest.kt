package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.inventory.domain.InventoryError
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.usecases.services.FakeNpcLoader
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

// Uses the units and lootable items defined in the resources: the knight starts with 20 G and the viking with 100 G, the
// potions are worth 10 G, and the hammer 50 G.
class TradeAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val lootableItemApi = LootableItemApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, playerApi, battleUnitApi)

    // West of the knight, selling the potions and the hammer.
    private val merchant = Npc.Dto(id = "merchant", name = "Merchant", line = "How can I help you?", row = 0, column = 0, stock = listOf("health-potion", "mana-potion", "hammer"))
    private val npcApi = NpcApi(battlefieldApi, unitApi, lootableItemApi, eventBus, FakeNpcLoader(listOf(merchant)))
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, npcApi, eventBus)
    private val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleUnitApi, playerApi, battleApi, partyApi, npcApi, eventBus)

    // The knight leads from (0,1), next to the merchant, and the viking follows it from (1,1).
    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        abilityApi.init()
        unitApi.init()
        lootableItemApi.init()
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        lootableItemApi.initialiseLootableItems()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        playerApi.requestPlayerCreation("human", "Human", HUMAN)
        battlefieldApi.requestBattlefieldCreation(4, 4, List(4) { List(4) { "sand" } })
        battleUnitApi.deployBattleUnit("knight-1", "knight", "human", deployAtRow = 0, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("viking-1", "viking", "human", deployAtRow = 1, deployAtColumn = 1)
        eventBus.dispatch()
        npcApi.initialiseNpcs()
        partyApi.startExploration()
        eventBus.dispatch()
        partyApi.talkToAdjacentNpc()
        eventBus.dispatch()
    }

    private fun gold(battleUnitId: String) = inventoryApi.searchInventoryByBattleUnitId(battleUnitId)!!.gold

    private fun storedItems(battleUnitId: String) = inventoryApi.searchInventoryByBattleUnitId(battleUnitId)!!.storedItems

    private fun buy(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        inventoryApi.buyItem(battleUnitId, lootableItemId)
        eventBus.dispatch()
    }

    private fun sell(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        inventoryApi.sellItem(battleUnitId, lootableItemId)
        eventBus.dispatch()
    }

    @Test
    fun `should give each party member its own starting gold once deployed`() =
        scenarioTest(::setUp) {
            // Then
            gold("knight-1") shouldBe 20
            gold("viking-1") shouldBe 100
        }

    @Test
    fun `should let each party member buy with its own gold`() =
        scenarioTest(::setUp) {
            // Then
            inventoryApi.canBuyItem("knight-1", "hammer").shouldBeFalse()
            shouldThrow<InventoryError.NotEnoughGold> { inventoryApi.buyItem("knight-1", "hammer") }
            // When
            buy("knight-1", "health-potion")
            buy("viking-1", "hammer")
            // Then
            gold("knight-1") shouldBe 10
            storedItems("knight-1").shouldContainExactly("health-potion")
            gold("viking-1") shouldBe 50
            storedItems("viking-1").shouldContainExactly("hammer")
        }

    @Test
    fun `should pay half the price, rounded down, for a sold item`() =
        scenarioTest(::setUp) {
            // Given
            buy("viking-1", "hammer")
            buy("viking-1", "mana-potion")
            // When
            sell("viking-1", "hammer")
            sell("viking-1", "mana-potion")
            // Then
            gold("viking-1") shouldBe 100 - 50 - 10 + 25 + 5
            storedItems("viking-1").shouldContainExactly()
        }

    @Test
    fun `should not let an equipped hammer be sold until it is unequipped`() =
        scenarioTest(::setUp) {
            // Given
            buy("viking-1", "hammer")
            inventoryApi.equipItem("viking-1", "hammer")
            eventBus.dispatch()
            // Then
            inventoryApi.canSellItem("viking-1", "hammer").shouldBeFalse()
            // When
            inventoryApi.unequipItem("viking-1", "hammer")
            eventBus.dispatch()
            // Then
            inventoryApi.canSellItem("viking-1", "hammer").shouldBeTrue()
        }

    @Test
    fun `should not let the party trade once the talk with the merchant is finished`() =
        scenarioTest(::setUp) {
            // When
            partyApi.finishTalk()
            eventBus.dispatch()
            // Then
            inventoryApi.canBuyItem("viking-1", "health-potion").shouldBeFalse()
            shouldThrow<InventoryError.NotTradingWithAMerchant> { inventoryApi.buyItem("viking-1", "health-potion") }
        }
}
