package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ConsumeManaPotionAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val lootableItemApi = LootableItemApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, battleUnitApi)
    private val inventoryApi = InventoryApi(lootableItemApi, battleUnitApi, playerApi, battleApi, eventBus)
    private val battleSetupApi = BattleSetupApi(eventBus)

    private val playerOneId = "player-one"
    private val playerOneKnightBattleUnitId = "player-1-unit-1"

    // The knight of the human player starts the battle with 30 mana points, and spends 10 casting heal on itself.
    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        lootableItemApi.init()
        abilityApi.init()
        unitApi.init()
        playerApi.init()
        battlefieldApi.init()
        battleUnitApi.init()
        battleApi.init()
        battleSetupApi.setupBattle()
        eventBus.dispatch()
        // The setup starts exploring: the battle starts as if the CPU engaged.
        battleApi.initialiseBattle()
        eventBus.dispatch()
        val castGroup = battleUnitApi.whereCanCast(playerOneKnightBattleUnitId, "heal").single()
        battleUnitApi.castAbility(battleUnitId = playerOneKnightBattleUnitId, abilityId = "heal", castGroup = castGroup)
        eventBus.dispatch()
    }

    @Test
    fun `should restore the knight mana when the knight consumes a looted mana potion`() =
        scenarioTest(::setUp) {
            // Given
            require(battleApi.searchBattle()!!.currentPlayerTurn == playerOneId)
            require(battleUnitApi.searchBattleUnitById(playerOneKnightBattleUnitId)!!.remainingManaPoints == 20)
            battlefieldApi.placeDroppedItem(lootableItemId = "mana-potion", row = 6, column = 7)
            inventoryApi.storeLootedItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "mana-potion", row = 6, column = 7)
            // When
            inventoryApi.consumeItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "mana-potion")
            eventBus.dispatch()
            // Then
            battleUnitApi.searchBattleUnitById(playerOneKnightBattleUnitId)!!.remainingManaPoints shouldBe 30
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.storedItems.shouldBeEmpty()
        }
}
