package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.domain.EffectMother
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.usecases.services.TerrainLoader
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.domain.UnitMother
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TerrainEffectAcceptanceTest {
    private class StubTerrainLoader(
        private val terrains: List<Terrain.Dto>,
    ) : TerrainLoader {
        override fun loadTerrains() = terrains
    }

    private val eventBus = InMemoryEventBus()
    private val terrainEffectId = "terrain-effect"
    private val terrainApi =
        TerrainApi(
            eventBus,
            StubTerrainLoader(
                listOf(
                    Terrain.Dto(id = "sand", canBeOccupied = true, allowedTransitionTo = setOf("forest"), effectId = terrainEffectId),
                    Terrain.Dto(id = "forest", canBeOccupied = true, allowedTransitionTo = setOf("sand")),
                ),
            ),
        )
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, battleUnitApi)
    private val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus)

    private val playerOneId = "player-one"
    private val playerTwoId = "player-two"
    private val sandBattleUnitId = "battle-unit-sand-1"
    private val secondSandBattleUnitId = "battle-unit-sand-2"
    private val forestBattleUnitId = "battle-unit-forest"

    private suspend fun setUp() {
        playerApi.requestPlayerCreation(playerOneId, "Player One", HUMAN)
        playerApi.requestPlayerCreation(playerTwoId, "Player Two", HUMAN)
        terrainApi.init()
        terrainApi.initialiseTerrains()

        val terrainEffect = EffectMother.decreaseHealthEffect(id = terrainEffectId, damage = 7, applicationType = "IMMEDIATELY")
        effectApi.requestEffectCreation(terrainEffect.toDto())

        val tiles =
            List(8) { row ->
                List(8) { column ->
                    if (row in 2..3 && column in 2..3) "sand" else "forest"
                }
            }
        battlefieldApi.requestBattlefieldCreation(8, 8, tiles)

        unitApi.requestUnitCreation(
            UnitMother.unit(id = "unit-sand-1", healthPoints = 20, manaPoints = 10, abilities = emptyList(), movementRange = 3).toDto(),
        )
        unitApi.requestUnitCreation(
            UnitMother.unit(id = "unit-sand-2", healthPoints = 40, manaPoints = 10, abilities = emptyList(), movementRange = 3).toDto(),
        )
        unitApi.requestUnitCreation(
            UnitMother.unit(id = "unit-forest", healthPoints = 30, manaPoints = 10, abilities = emptyList(), movementRange = 3).toDto(),
        )

        battleUnitApi.deployBattleUnit(
            battleUnitId = sandBattleUnitId,
            unitId = "unit-sand-1",
            playerId = playerOneId,
            deployAtRow = 2,
            deployAtColumn = 2,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = secondSandBattleUnitId,
            unitId = "unit-sand-2",
            playerId = playerOneId,
            deployAtRow = 3,
            deployAtColumn = 3,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = forestBattleUnitId,
            unitId = "unit-forest",
            playerId = playerOneId,
            deployAtRow = 0,
            deployAtColumn = 0,
        )

        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, playerOneId, playerTwoId)
        eventBus.dispatch()
    }

    @Test
    fun `should apply the terrain effect to the occupants of the terrain tiles when a new battle round starts`() =
        scenarioTest(::setUp) {
            // Given
            battleApi.searchBattle()!!.currentRound shouldBe 1
            battleUnitApi.searchBattleUnitById(sandBattleUnitId)!!.remainingHealthPoints shouldBe 20
            battleUnitApi.searchBattleUnitById(secondSandBattleUnitId)!!.remainingHealthPoints shouldBe 40
            battleUnitApi.searchBattleUnitById(forestBattleUnitId)!!.remainingHealthPoints shouldBe 30
            // When
            battleApi.finishPlayerTurn()
            eventBus.dispatch()
            battleApi.finishPlayerTurn()
            eventBus.dispatch()
            // Then
            battleApi.searchBattle()!!.currentRound shouldBe 2
            battleUnitApi.searchBattleUnitById(sandBattleUnitId)!!.remainingHealthPoints shouldBe 13
            battleUnitApi.searchBattleUnitById(secondSandBattleUnitId)!!.remainingHealthPoints shouldBe 33
            battleUnitApi.searchBattleUnitById(forestBattleUnitId)!!.remainingHealthPoints shouldBe 30
        }
}
