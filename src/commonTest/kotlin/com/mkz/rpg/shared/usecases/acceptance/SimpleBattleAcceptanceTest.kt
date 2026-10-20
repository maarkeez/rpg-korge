package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.domain.AbilityMother
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnitMother
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.domain.EffectMother
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.PlayerMother
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.domain.UnitMother
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SimpleBattleAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, playerApi, battleUnitApi)

    // The CPU battle units out of the battle rest when a round finishes, rather than wandering.
    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom(0.0))
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, eventBus)
    private val battleSetupApi = BattleSetupApi(eventBus)

    private val humanPlayerId = PlayerMother.id()
    private val cpuPlayerId = PlayerMother.id()
    private val swordAbilityId = AbilityMother.id()
    private val weakKnightUnitId = UnitMother.id()
    private val knightUnitId = UnitMother.id()
    private val humanBattleUnitId = BattleUnitMother.id()
    private val cpuBattleUnitId = BattleUnitMother.id()

    private suspend fun setUp() {
        playerApi.requestPlayerCreation(humanPlayerId, "Human", HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, "CPU", CPU)
        terrainApi.init()
        terrainApi.initialiseTerrains()
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })

        val lowPhysicalDamage =
            EffectMother
                .decreaseHealthEffect(
                    id = "low-physical-damage",
                    damage = 10,
                    applicationType = "IMMEDIATELY",
                ).toDto()
        effectApi.requestEffectCreation(lowPhysicalDamage)

        val sword =
            Ability.Dto(
                id = swordAbilityId,
                name = "Sword",
                cost = 0,
                cooldown = 0,
                effectSpecs = listOf(Ability.Dto.EffectSpecDto(effectId = lowPhysicalDamage.id, target = TargetExpressionDto(type = TargetExpressionDto.Type.SELECTED_TARGET))),
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
            )
        abilityApi.requestAbilityCreation(sword)

        val knight =
            Unit.Dto(
                id = knightUnitId,
                name = "Knight",
                healthPoints = 100,
                manaPoints = 30,
                abilities = listOf(sword.id),
                movementRange = 3,
            )
        val weakKnight =
            Unit.Dto(
                id = weakKnightUnitId,
                name = "Weak Knight",
                healthPoints = 1,
                manaPoints = 1,
                abilities = listOf(sword.id),
                movementRange = 3,
            )
        unitApi.requestUnitCreation(knight)
        unitApi.requestUnitCreation(weakKnight)
        battleUnitApi.deployBattleUnit(
            battleUnitId = humanBattleUnitId,
            unitId = knightUnitId,
            playerId = humanPlayerId,
            deployAtRow = 1,
            deployAtColumn = 1,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = cpuBattleUnitId,
            unitId = weakKnightUnitId,
            playerId = cpuPlayerId,
            deployAtRow = 1,
            deployAtColumn = 2,
        )
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, humanPlayerId, cpuPlayerId)
        eventBus.dispatch()
    }

    @Test
    fun `should occupy tile when unit was deployed`() =
        scenarioTest(::setUp) {
            // Given
            val battleUnitId = BattleUnitMother.id()
            battleUnitApi.deployBattleUnit(
                battleUnitId = battleUnitId,
                unitId = knightUnitId,
                playerId = humanPlayerId,
                deployAtRow = 2,
                deployAtColumn = 2,
            )
            // When
            eventBus.dispatch()
            // Then
            val occupantBattleUnitId = battlefieldApi.searchOccupant(row = 2, column = 2)
            occupantBattleUnitId shouldBe battleUnitId
        }

    @Test
    fun `should play cpu when is cpu player turn`() =
        scenarioTest(::setUp) {
            // Given
            battleApi.finishPlayerTurn()
            val battle = battleApi.searchBattle()!!
            require(battle.currentPlayerTurn == cpuPlayerId)
            require(battle.currentRound == 1)
            // When
            eventBus.dispatch()
            // Then
            val storedBattle = battleApi.searchBattle()!!
            storedBattle.currentPlayerTurn shouldBe humanPlayerId
            storedBattle.currentRound shouldBe 2
        }

    @Test
    fun `should occupy a tile when unit was moved`() =
        scenarioTest(::setUp) {
            // Given
            require(battlefieldApi.searchOccupant(row = 1, column = 1) == humanBattleUnitId)
            battleUnitApi.moveBattleUnit(battleUnitId = humanBattleUnitId, moveToRow = 2, moveToColumn = 1)
            // When
            eventBus.dispatch()
            // Then
            val previousTileOccupantId = battlefieldApi.searchOccupant(row = 1, column = 1)
            previousTileOccupantId.shouldBeNull()
            val newTileOccupantId = battlefieldApi.searchOccupant(row = 2, column = 1)
            newTileOccupantId shouldBe humanBattleUnitId
        }

    @Test
    fun `should remove enemy battle unit from battlefield when is defeated`() =
        scenarioTest(::setUp) {
            // Given
            require(battlefieldApi.searchOccupant(row = 1, column = 1) == humanBattleUnitId)
            require(battlefieldApi.searchOccupant(row = 1, column = 2) == cpuBattleUnitId)
            battleUnitApi.castAbility(
                battleUnitId = humanBattleUnitId,
                abilityId = swordAbilityId,
                castGroup =
                    WhereCanCast.CastGroup(
                        positions =
                            listOf(
                                WhereCanCast.PositionDto(row = 1, column = 2),
                            ),
                    ),
            )
            // When
            eventBus.dispatch()
            // Then
            val cpuBattleUnit = battleUnitApi.searchBattleUnitById(cpuBattleUnitId)!!
            cpuBattleUnit.remainingHealthPoints shouldBe 0
            val cpuBattleUnitPosition = battlefieldApi.searchPosition(cpuBattleUnitId)
            cpuBattleUnitPosition.shouldBeNull()
        }

    @Test
    fun `should win game when enemy is defeated`() =
        scenarioTest(::setUp) {
            // Given
            require(battlefieldApi.searchOccupant(row = 1, column = 1) == humanBattleUnitId)
            require(battlefieldApi.searchOccupant(row = 1, column = 2) == cpuBattleUnitId)
            battleUnitApi.castAbility(
                battleUnitId = humanBattleUnitId,
                abilityId = swordAbilityId,
                castGroup =
                    WhereCanCast.CastGroup(
                        positions =
                            listOf(
                                WhereCanCast.PositionDto(row = 1, column = 2),
                            ),
                    ),
            )
            // When
            eventBus.dispatch()
            // Then
            val battle = battleApi.searchBattle()!!
            battle.isFinished.shouldBeTrue()
            battle.currentPlayerTurn shouldBe humanPlayerId
        }
}
