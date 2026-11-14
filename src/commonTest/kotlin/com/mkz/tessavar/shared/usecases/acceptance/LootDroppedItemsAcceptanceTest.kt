package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.gameSetup.adapters.presentation.GameSetupApi
import com.mkz.tessavar.inventory.adapters.presentation.InventoryApi
import com.mkz.tessavar.inventory.domain.InventoryEvent.RequestGrantReward
import com.mkz.tessavar.lootDrop.adapters.presentation.LootDropApi
import com.mkz.tessavar.lootDrop.adapters.resources.ResourcesLootDropLoader
import com.mkz.tessavar.lootDrop.domain.LootDrop
import com.mkz.tessavar.lootTable.adapters.presentation.LootTableApi
import com.mkz.tessavar.lootTable.adapters.resources.ResourcesLootTableLoader
import com.mkz.tessavar.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.tessavar.lootableItem.adapters.resources.ResourcesLootableItemLoader
import com.mkz.tessavar.npc.adapters.presentation.NpcApi
import com.mkz.tessavar.npc.adapters.resources.ResourcesNpcLoader
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import kotlin.test.Test

class LootDroppedItemsAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
    private val unitApi = UnitApi(eventBus, ResourcesUnitLoader())
    private val playerApi = PlayerApi(eventBus, ResourcesPlayerLoader())
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
    private val effectApi = EffectApi(eventBus, ResourcesEffectLoader())
    private val abilityApi = AbilityApi(effectApi, eventBus, ResourcesAbilityLoader())
    private val lootableItemApi = LootableItemApi(effectApi, eventBus, ResourcesLootableItemLoader())
    private val lootTableApi = LootTableApi(lootableItemApi, eventBus, ResourcesLootTableLoader())
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, ResourcesBattleUnitLoader())
    private val lootDropApi = LootDropApi(unitApi, lootTableApi, eventBus, ResourcesLootDropLoader(), Random.Default)
    private val battleApi = BattleApi(eventBus)
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val npcApi = NpcApi(battlefieldApi, lootableItemApi, partyApi, eventBus, ResourcesNpcLoader())
    private val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleApi, partyApi, npcApi, eventBus)
    private val gameSetupApi = GameSetupApi(eventBus)

    private val playerOneId = "player-one"
    private val playerOneKnightBattleUnitId = "hero"

    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        lootableItemApi.init()
        lootTableApi.init()
        abilityApi.init()
        unitApi.init()
        lootDropApi.init()
        playerApi.init()
        battlefieldApi.init()
        battleUnitApi.init()
        gameSetupApi.setUpGame()
        eventBus.dispatch()
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, "player-one", "player-two")
        eventBus.dispatch()
    }

    @Test
    fun `should store the dropped item in the inventory and clear its tile when the knight walks across it`() =
        scenarioTest(::setUp) {
            // Given
            require(battleApi.searchBattle()!!.currentPlayerId == playerOneId)
            require(battlefieldApi.searchPosition(playerOneKnightBattleUnitId) == PositionDto(row = 2, column = 1))
            lootDropApi.dropItemWithoutRolling(unitId = "rat", lootableItemId = "health-potion", row = 2, column = 2)
            battleUnitApi.walkBattleUnitInBattle(battleUnitId = playerOneKnightBattleUnitId, walkToRow = 2, walkToColumn = 3)
            // When
            eventBus.dispatch()
            // Then
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.storedItems shouldBe listOf("health-potion")
            lootDropApi.searchDroppedItems().shouldBeEmpty()
        }

    @Test
    fun `should store the dropped item in the inventory and clear its tile when the knight stops on it`() =
        scenarioTest(::setUp) {
            // Given
            require(battleApi.searchBattle()!!.currentPlayerId == playerOneId)
            lootDropApi.dropItemWithoutRolling(unitId = "rat", lootableItemId = "health-potion", row = 2, column = 3)
            battleUnitApi.walkBattleUnitInBattle(battleUnitId = playerOneKnightBattleUnitId, walkToRow = 2, walkToColumn = 3)
            // When
            eventBus.dispatch()
            // Then
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.storedItems shouldBe listOf("health-potion")
            lootDropApi.searchDroppedItems().shouldBeEmpty()
        }

    @Test
    fun `should drop the reward items that do not fit on the tile of the knight when a reward is requested`() =
        scenarioTest(::setUp) {
            // Given
            eventBus.publish(RequestGrantReward(playerOneKnightBattleUnitId, gold = 150, lootableItemIds = List(13) { "health-potion" }, row = 2, column = 1))
            // When
            eventBus.dispatch()
            // Then
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.gold shouldBe 180
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.storedItems shouldBe List(12) { "health-potion" }
            lootDropApi.searchDroppedItems() shouldBe mapOf(LootDrop.Dto.PositionDto(row = 2, column = 1) to listOf("health-potion"))
        }

    @Test
    fun `should store a dropped reward item and clear its tile when the knight walks back onto it with storage space left in its inventory`() =
        scenarioTest(::setUp) {
            // Given
            eventBus.publish(RequestGrantReward(playerOneKnightBattleUnitId, gold = 0, lootableItemIds = List(13) { "health-potion" }, row = 2, column = 1))
            eventBus.dispatch()
            inventoryApi.discardItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "health-potion")
            battleUnitApi.walkBattleUnitInBattle(battleUnitId = playerOneKnightBattleUnitId, walkToRow = 2, walkToColumn = 2)
            eventBus.dispatch()
            battleUnitApi.walkBattleUnitInBattle(battleUnitId = playerOneKnightBattleUnitId, walkToRow = 2, walkToColumn = 1)
            // When
            eventBus.dispatch()
            // Then
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.storedItems shouldBe List(12) { "health-potion" }
            lootDropApi.searchDroppedItems().shouldBeEmpty()
        }

    @Test
    fun `should leave the dropped item on its tile when the knight inventory is full`() =
        scenarioTest(::setUp) {
            // Given
            require(battleApi.searchBattle()!!.currentPlayerId == playerOneId)
            repeat(13) { lootDropApi.dropItemWithoutRolling(unitId = "rat", lootableItemId = "health-potion", row = 2, column = 2) }
            battleUnitApi.walkBattleUnitInBattle(battleUnitId = playerOneKnightBattleUnitId, walkToRow = 2, walkToColumn = 2)
            // When
            eventBus.dispatch()
            // Then
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.storedItems shouldBe List(12) { "health-potion" }
            lootDropApi.searchDroppedItems() shouldBe mapOf(LootDrop.Dto.PositionDto(row = 2, column = 2) to listOf("health-potion"))
        }
}
