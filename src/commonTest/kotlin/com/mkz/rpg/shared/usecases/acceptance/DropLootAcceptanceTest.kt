package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
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

class DropLootAcceptanceTest {
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

    private val playerOneKnightBattleUnitId = "player-1-unit-1"
    private val playerTwoRatBattleUnitId = "player-2-unit-1"

    // The rat of the CPU player starts the battle at row 0 and column 0, with 20 health points.
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
    }

    // The rat is hit twice for 10 damage, which defeats it.
    private fun defeatTheRat() =
        repeat(2) {
            battleUnitApi.applyEffect(
                EffectApplicationDto(
                    source = ApplicationSourceDto.terrain("lava"),
                    target = EffectTargetDto.Unit(playerTwoRatBattleUnitId),
                    effectId = "low-physical-damage",
                ),
            )
        }

    @Test
    fun `should leave a hammer on its tile when a rat is defeated`() =
        scenarioTest(::setUp) {
            // Given
            require(battlefieldApi.searchPosition(playerTwoRatBattleUnitId) == PositionDto(row = 0, column = 0))
            require(battleUnitApi.searchBattleUnitById(playerTwoRatBattleUnitId)!!.remainingHealthPoints == 20)
            defeatTheRat()
            // When
            eventBus.dispatch()
            // Then
            lootDropApi.searchDroppedItems() shouldBe mapOf(LootDrop.Dto.PositionDto(row = 0, column = 0) to listOf("hammer"))
        }

    @Test
    fun `should store the hammer of a defeated rat in the inventory and clear its tile when the knight walks onto it`() =
        scenarioTest(::setUp) {
            // Given
            defeatTheRat()
            eventBus.dispatch()
            require(lootDropApi.searchDroppedItems() == mapOf(LootDrop.Dto.PositionDto(row = 0, column = 0) to listOf("hammer")))
            battleUnitApi.walkBattleUnit(battleUnitId = playerOneKnightBattleUnitId, walkToRow = 0, walkToColumn = 0)
            // When
            eventBus.dispatch()
            // Then
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.storedItems shouldBe listOf("hammer")
            lootDropApi.searchDroppedItems().shouldBeEmpty()
        }
}
