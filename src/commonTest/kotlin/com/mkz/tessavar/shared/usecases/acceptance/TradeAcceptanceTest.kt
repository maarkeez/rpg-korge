package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.inventory.adapters.presentation.InventoryApi
import com.mkz.tessavar.inventory.domain.Inventory.Dto.TradeRefusalDto
import com.mkz.tessavar.inventory.domain.InventoryError
import com.mkz.tessavar.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.tessavar.lootableItem.adapters.resources.ResourcesLootableItemLoader
import com.mkz.tessavar.npc.adapters.presentation.NpcApi
import com.mkz.tessavar.npc.domain.Npc
import com.mkz.tessavar.npc.usecases.services.FakeNpcLoader
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TradeAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
    private val unitApi = UnitApi(eventBus, ResourcesUnitLoader())
    private val playerApi = PlayerApi(eventBus, ResourcesPlayerLoader())
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
    private val effectApi = EffectApi(eventBus, ResourcesEffectLoader())
    private val abilityApi = AbilityApi(effectApi, eventBus, ResourcesAbilityLoader())
    private val lootableItemApi = LootableItemApi(effectApi, eventBus, ResourcesLootableItemLoader())
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, ResourcesBattleUnitLoader())
    private val battleApi = BattleApi(eventBus)

    private val merchant = Npc.Dto(id = "merchant", row = 0, column = 0, unitId = null, stock = listOf("health-potion", "mana-potion", "hammer"))
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val npcApi = NpcApi(battlefieldApi, lootableItemApi, partyApi, eventBus, FakeNpcLoader(listOf(merchant)))
    private val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleApi, partyApi, npcApi, eventBus)

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
        playerApi.requestPlayerCreation("human", HUMAN)
        battlefieldApi.requestBattlefieldCreation(4, 4, List(4) { List(4) { "sand" } })
        battleUnitApi.deployBattleUnit("knight-1", "knight", "human", deployAtRow = 0, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("viking-1", "viking", "human", deployAtRow = 1, deployAtColumn = 1)
        eventBus.dispatch()
        npcApi.placeNpcs()
        partyApi.startExploration()
        eventBus.dispatch()
        npcApi.talkToAdjacentNpc()
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
            inventoryApi.searchBuyRefusal("knight-1", "hammer") shouldBe TradeRefusalDto.NOT_ENOUGH_GOLD
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
            inventoryApi.searchSellRefusal("viking-1", "hammer") shouldBe TradeRefusalDto.EQUIPPED
            // When
            inventoryApi.unequipItem("viking-1", "hammer")
            eventBus.dispatch()
            // Then
            inventoryApi.searchSellRefusal("viking-1", "hammer").shouldBeNull()
        }

    @Test
    fun `should not let the party trade once the talk with the merchant is finished`() =
        scenarioTest(::setUp) {
            // When
            partyApi.finishTalk()
            eventBus.dispatch()
            // Then
            inventoryApi.searchBuyRefusal("viking-1", "health-potion") shouldBe TradeRefusalDto.NOT_TRADING
            shouldThrow<InventoryError.NotTradingWithAMerchant> { inventoryApi.buyItem("viking-1", "health-potion") }
        }
}
