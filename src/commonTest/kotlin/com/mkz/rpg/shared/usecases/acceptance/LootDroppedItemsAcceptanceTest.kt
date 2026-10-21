package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.lootDrop.adapters.presentation.LootDropApi
import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LootDroppedItemsAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val lootableItemApi = LootableItemApi(effectApi, eventBus)
    private val lootTableApi = LootTableApi(lootableItemApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val lootDropApi = LootDropApi(unitApi, lootTableApi, eventBus)
    private val battleApi = BattleApi(eventBus, playerApi)
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val npcApi = NpcApi(battlefieldApi, unitApi, lootableItemApi, partyApi, eventBus)
    private val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleUnitApi, playerApi, battleApi, partyApi, npcApi, eventBus)
    private val battleSetupApi = BattleSetupApi(eventBus)

    private val playerOneId = "player-one"
    private val playerOneKnightBattleUnitId = "player-1-unit-1"

    // The knight of the human player starts the battle at row 6 and column 6, with the sand tiles at its east free.
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
        battleSetupApi.setupBattle()
        eventBus.dispatch()
        // The setup starts exploring: the battle starts as if the players engaged each other.
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, "player-one", "player-two")
        eventBus.dispatch()
    }

    @Test
    fun `should store the dropped item in the inventory and clear its tile when the knight walks across it`() =
        scenarioTest(::setUp) {
            // Given
            require(battleApi.searchBattle()!!.currentPlayerTurn == playerOneId)
            require(battlefieldApi.searchPosition(playerOneKnightBattleUnitId) == PositionDto(row = 6, column = 6))
            lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "health-potion", row = 6, column = 7)
            battleUnitApi.moveBattleUnit(battleUnitId = playerOneKnightBattleUnitId, moveToRow = 6, moveToColumn = 8)
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
            require(battleApi.searchBattle()!!.currentPlayerTurn == playerOneId)
            lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "health-potion", row = 6, column = 8)
            battleUnitApi.moveBattleUnit(battleUnitId = playerOneKnightBattleUnitId, moveToRow = 6, moveToColumn = 8)
            // When
            eventBus.dispatch()
            // Then
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.storedItems shouldBe listOf("health-potion")
            lootDropApi.searchDroppedItems().shouldBeEmpty()
        }

    @Test
    fun `should leave the dropped item on its tile when the knight inventory is full`() =
        scenarioTest(::setUp) {
            // Given
            require(battleApi.searchBattle()!!.currentPlayerTurn == playerOneId)
            repeat(13) { lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "health-potion", row = 6, column = 7) }
            battleUnitApi.moveBattleUnit(battleUnitId = playerOneKnightBattleUnitId, moveToRow = 6, moveToColumn = 7)
            // When
            eventBus.dispatch()
            // Then
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.storedItems shouldBe List(12) { "health-potion" }
            lootDropApi.searchDroppedItems() shouldBe mapOf(LootDrop.Dto.PositionDto(row = 6, column = 7) to listOf("health-potion"))
        }
}
