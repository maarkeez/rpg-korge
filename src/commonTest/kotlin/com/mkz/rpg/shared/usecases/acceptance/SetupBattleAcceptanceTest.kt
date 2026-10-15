package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto.CONSUMABLE
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SetupBattleAcceptanceTest {
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
    private val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus)
    private val battleSetupApi = BattleSetupApi(eventBus)

    private val playerOneId = "player-one"
    private val playerOneKnightBattleUnitId = "player-1-unit-1"

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
    }

    @Test
    fun `should occupy adjacent tile when battle unit was moved`() =
        scenarioTest(::setUp) {
            // Given
            val currentPosition = battlefieldApi.searchPosition(playerOneKnightBattleUnitId)!!
            require(currentPosition.row == 6 && currentPosition.column == 6)
            require(battleApi.searchBattle()!!.currentPlayerTurn == playerOneId)
            battleUnitApi.moveBattleUnit(
                battleUnitId = playerOneKnightBattleUnitId,
                moveToRow = 5,
                moveToColumn = 6,
            )
            // When
            eventBus.dispatch()
            // Then
            val previousTileOccupantId = battlefieldApi.searchOccupant(row = 6, column = 6)
            previousTileOccupantId.shouldBeNull()
            val newTileOccupantId = battlefieldApi.searchOccupant(row = 5, column = 6)
            newTileOccupantId shouldBe playerOneKnightBattleUnitId
        }

    @Test
    fun `should store the health potion when the battle is set up`() =
        scenarioTest(::setUp) {
            // Given
            val healthPotionId = "health-potion"
            // When
            val healthPotion = lootableItemApi.searchLootableItemById(healthPotionId)
            // Then
            healthPotion.shouldNotBeNull()
            healthPotion.name shouldBe "Health Potion"
            healthPotion.lootType.type shouldBe CONSUMABLE
            healthPotion.lootType.consumable?.effectIds shouldBe setOf("health-potion-heal")
        }
}
