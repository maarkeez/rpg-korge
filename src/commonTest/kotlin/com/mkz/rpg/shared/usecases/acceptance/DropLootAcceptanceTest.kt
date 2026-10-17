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
import com.mkz.rpg.lootDrop.adapters.presentation.LootDropApi
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
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
    private val lootDropApi = LootDropApi(unitApi, lootTableApi, battleUnitApi, eventBus)
    private val battleApi = BattleApi(eventBus, playerApi, battleUnitApi)
    private val battleSetupApi = BattleSetupApi(eventBus)

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

    @Test
    fun `should leave a hammer on its tile when a rat is defeated`() =
        scenarioTest(::setUp) {
            // Given
            require(battlefieldApi.searchPosition(playerTwoRatBattleUnitId) == PositionDto(row = 0, column = 0))
            require(battleUnitApi.searchBattleUnitById(playerTwoRatBattleUnitId)!!.remainingHealthPoints == 20)
            repeat(2) {
                battleUnitApi.applyEffect(
                    EffectApplicationDto(
                        source = ApplicationSourceDto.terrain("lava"),
                        target = EffectTargetDto.Unit(playerTwoRatBattleUnitId),
                        effectId = "low-physical-damage",
                    ),
                )
            }
            // When
            eventBus.dispatch()
            // Then
            battlefieldApi.searchDroppedItems() shouldBe mapOf(PositionDto(row = 0, column = 0) to listOf("hammer"))
        }
}
