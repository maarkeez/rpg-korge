package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.ability.domain.Ability
import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto
import com.mkz.tessavar.ability.domain.AbilityMother
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother
import com.mkz.tessavar.battleUnit.usecases.queries.WhereCanCast
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.tessavar.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.effect.domain.EffectMother
import com.mkz.tessavar.gameSetup.adapters.presentation.GameSetupApi
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.player.domain.PlayerMother
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
import com.mkz.tessavar.unit.domain.Unit
import com.mkz.tessavar.unit.domain.UnitMother
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SimpleBattleAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
    private val unitApi = UnitApi(eventBus, ResourcesUnitLoader())
    private val playerApi = PlayerApi(eventBus, ResourcesPlayerLoader())
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
    private val effectApi = EffectApi(eventBus, ResourcesEffectLoader())
    private val abilityApi = AbilityApi(effectApi, eventBus, ResourcesAbilityLoader())
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, ResourcesBattleUnitLoader())
    private val battleApi = BattleApi(eventBus)

    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom.cpuBattleUnitsAlwaysResting())
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, partyApi, eventBus)
    private val gameSetupApi = GameSetupApi(eventBus)

    private val humanPlayerId = PlayerMother.id()
    private val cpuPlayerId = PlayerMother.id()
    private val swordAbilityId = AbilityMother.id()
    private val weakKnightUnitId = UnitMother.id()
    private val knightUnitId = UnitMother.id()
    private val humanBattleUnitId = BattleUnitMother.id()
    private val cpuBattleUnitId = BattleUnitMother.id()

    private suspend fun setUp() {
        playerApi.requestPlayerCreation(humanPlayerId, HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, CPU)
        terrainApi.init()
        terrainApi.initialiseTerrains()
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })

        val lowPhysicalDamage =
            EffectMother
                .damageEffect(
                    id = "low-physical-damage",
                    damage = 10,
                    timingType = "IMMEDIATELY",
                ).toDto()
        effectApi.requestEffectCreation(lowPhysicalDamage)

        val sword =
            Ability.Dto(
                id = swordAbilityId,
                manaCost = 0,
                cooldown = 0,
                abilityEffects = listOf(Ability.Dto.AbilityEffectDto(effectId = lowPhysicalDamage.id, target = EffectTargetDto(type = EffectTargetDto.Type.TARGETED_BATTLE_UNIT))),
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                summonDuration = null,
            )
        abilityApi.requestAbilityCreation(sword)

        val knight =
            Unit.Dto(
                id = knightUnitId,
                baseMaximumHealth = 100,
                baseMaximumMana = 30,
                startingAbilities = listOf(sword.id),
                maximumSteps = 3,
                startingGold = 0,
            )
        val weakKnight =
            Unit.Dto(
                id = weakKnightUnitId,
                baseMaximumHealth = 1,
                baseMaximumMana = 1,
                startingAbilities = listOf(sword.id),
                maximumSteps = 3,
                startingGold = 0,
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
            require(battle.currentPlayerId == cpuPlayerId)
            require(battle.currentRound == 1)
            // When
            eventBus.dispatch()
            // Then
            val storedBattle = battleApi.searchBattle()!!
            storedBattle.currentPlayerId shouldBe humanPlayerId
            storedBattle.currentRound shouldBe 2
        }

    @Test
    fun `should occupy a tile when unit was moved`() =
        scenarioTest(::setUp) {
            // Given
            require(battlefieldApi.searchOccupant(row = 1, column = 1) == humanBattleUnitId)
            battleUnitApi.walkBattleUnitInBattle(battleUnitId = humanBattleUnitId, walkToRow = 2, walkToColumn = 1)
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
            cpuBattleUnit.remainingHealth shouldBe 0
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
            battle.currentPlayerId shouldBe humanPlayerId
        }
}
