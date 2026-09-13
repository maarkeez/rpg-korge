package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.AbilityMother
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnitMother
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.PlayerMother
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.domain.UnitMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SimpleBattleAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, battleUnitApi)
    private val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battleApi, battlefieldApi, eventBus)
    private val battleSetupApi =
        BattleSetupApi(
            playerApi,
            battleApi,
            effectApi,
            abilityApi,
            unitApi,
            battleUnitApi,
            battlefieldApi,
        )

    private val humanPlayerId = PlayerMother.id()
    private val cpuPlayerId = PlayerMother.id()
    private val swordAbilityId = AbilityMother.id()
    private val weakKnightUnitId = UnitMother.id()
    private val knightUnitId = UnitMother.id()
    private val humanBattleUnitId = BattleUnitMother.id()
    private val cpuBattleUnitId = BattleUnitMother.id()

    @BeforeEach
    fun setup() {
        playerApi.requestPlayerCreation(humanPlayerId, "Human", HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, "CPU", CPU)
        battlefieldApi.initializeBattlefield(8, 8, List(8) { List(8) { "tile-id-$it" } })

        val lowPhysicalDamage =
            Effect.Dto(
                id = "low-physical-damage",
                type = Effect.Dto.TypeDto.DECREASE_HEALTH,
                power = 10,
                probability = 100,
                modifiers = emptyList(),
                application =
                    ApplicationDto(
                        "IMMEDIATELY",
                        onTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            )
        effectApi.requestEffectCreation(lowPhysicalDamage)

        val sword =
            Ability.Dto(
                id = swordAbilityId,
                name = "Sword",
                cost = 0,
                cooldown = 0,
                effects = listOf(lowPhysicalDamage.id),
                targetPattern = Ability.Dto.TargetPatternDto.ADJACENT_ENEMY,
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
        battleApi.startFirstRound(listOf(humanPlayerId, cpuPlayerId))
        eventBus.dispatch()
    }

    @Test
    fun `should occupy tile when unit was deployed`() {
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
        assertThat(occupantBattleUnitId).isEqualTo(battleUnitId)
    }

    @Test
    fun `should play cpu when is cpu player turn`() {
        // Given
        battleApi.finishPlayerTurn()
        val battle = battleApi.searchBattle()!!
        require(battle.currentPlayerTurn == cpuPlayerId)
        require(battle.currentRound == 1)
        // When
        eventBus.dispatch()
        // Then
        val storedBattle = battleApi.searchBattle()!!
        assertThat(storedBattle.currentPlayerTurn).isEqualTo(humanPlayerId)
        assertThat(storedBattle.currentRound).isEqualTo(2)
    }

    @Test
    fun `should occupy a tile when unit was moved`() {
        // Given
        require(battlefieldApi.searchOccupant(row = 1, column = 1) == humanBattleUnitId)
        battleUnitApi.moveBattleUnit(battleUnitId = humanBattleUnitId, moveToRow = 2, moveToColumn = 1)
        // When
        eventBus.dispatch()
        // Then
        val previousTileOccupantId = battlefieldApi.searchOccupant(row = 1, column = 1)
        assertThat(previousTileOccupantId).isNull()
        val newTileOccupantId = battlefieldApi.searchOccupant(row = 2, column = 1)
        assertThat(newTileOccupantId).isEqualTo(humanBattleUnitId)
    }

    @Test
    fun `should remove enemy battle unit from battlefield when is defeated`() {
        // Given
        require(battlefieldApi.searchOccupant(row = 1, column = 1) == humanBattleUnitId)
        require(battlefieldApi.searchOccupant(row = 1, column = 2) == cpuBattleUnitId)
        battleUnitApi.castAbility(
            battleUnitId = humanBattleUnitId,
            abilityId = swordAbilityId,
            row = 1,
            column = 2,
        )
        // When
        eventBus.dispatch()
        // Then
        val cpuBattleUnit = battleUnitApi.searchBattleUnitById(cpuBattleUnitId)!!
        assertThat(cpuBattleUnit.remainingHealthPoints).isZero
        val cpuBattleUnitPosition = battlefieldApi.searchPosition(cpuBattleUnitId)
        assertThat(cpuBattleUnitPosition).isNull()
    }

    @Test
    fun `should win game when enemy is defeated`() {
        // Given
        require(battlefieldApi.searchOccupant(row = 1, column = 1) == humanBattleUnitId)
        require(battlefieldApi.searchOccupant(row = 1, column = 2) == cpuBattleUnitId)
        battleUnitApi.castAbility(
            battleUnitId = humanBattleUnitId,
            abilityId = swordAbilityId,
            row = 1,
            column = 2,
        )
        // When
        eventBus.dispatch()
        // Then
        val battle = battleApi.searchBattle()!!
        assertThat(battle.isFinished).isTrue()
        assertThat(battle.currentPlayerTurn).isEqualTo(humanPlayerId)
    }
}
