package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.lootDrop.adapters.presentation.LootDropApi
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ConsumeHealthPotionAcceptanceTest {
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
    private val npcApi = NpcApi(battlefieldApi, lootableItemApi, partyApi, eventBus)
    private val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleApi, partyApi, npcApi, eventBus)
    private val battleSetupApi = BattleSetupApi(eventBus)

    private val playerOneId = "player-one"
    private val playerOneKnightBattleUnitId = "hero"

    // The knight of the human player starts the battle with 100 health points, and is hit twice for 10 damage.
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
        repeat(2) {
            battleUnitApi.applyEffect(
                EffectApplicationDto(
                    source = ApplicationSourceDto.terrain("lava"),
                    target = EffectTargetDto.battleUnit(playerOneKnightBattleUnitId),
                    effectId = "low-physical-damage",
                ),
            )
        }
        eventBus.dispatch()
    }

    @Test
    fun `should heal the knight when the knight consumes a looted health potion`() =
        scenarioTest(::setUp) {
            // Given
            require(battleApi.searchBattle()!!.currentPlayerTurn == playerOneId)
            require(battleUnitApi.searchBattleUnitById(playerOneKnightBattleUnitId)!!.remainingHealthPoints == 80)
            lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "health-potion", row = 6, column = 7)
            inventoryApi.storeLootedItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "health-potion", row = 6, column = 7)
            // When
            inventoryApi.consumeItem(battleUnitId = playerOneKnightBattleUnitId, lootableItemId = "health-potion")
            eventBus.dispatch()
            // Then
            battleUnitApi.searchBattleUnitById(playerOneKnightBattleUnitId)!!.remainingHealthPoints shouldBe 90
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.storedItems.shouldBeEmpty()
        }
}
