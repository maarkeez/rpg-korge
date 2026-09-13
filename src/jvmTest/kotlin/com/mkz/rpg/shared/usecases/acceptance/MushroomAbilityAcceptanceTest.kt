package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.math.abs

class MushroomAbilityAcceptanceTest {
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

    @BeforeEach
    fun setup() {
        battleSetupApi.setupBattle()
        eventBus.dispatch()
    }

    @Test
    fun `should apply the venom effect to all adjacent enemy battle units when the mushroom ability is cast`() {
        // Given
        val humanKnightId = "player-1-unit-1"
        val enemyRatIds =
            listOf(
                "player-2-unit-1",
                "player-2-unit-2",
            )
        require(battlefieldApi.searchPosition(humanKnightId) == PositionDto(6, 6))
        require(battlefieldApi.searchPosition("player-2-unit-1") == PositionDto(0, 0))
        require(battlefieldApi.searchPosition("player-2-unit-2") == PositionDto(1, 1))
        require(battleApi.searchBattle()!!.currentPlayerTurn == "player-one")
        approachEnemyBattleUnits(humanKnightId, enemyRatIds)
        // When
        val castGroup = battleUnitApi.whereCanCast(humanKnightId, "mushroom").single()
        battleUnitApi.castAbility(
            battleUnitId = humanKnightId,
            abilityId = "mushroom",
            castGroup = castGroup,
        )
        eventBus.dispatch()
        // Then
        val adjacentEnemyRatIds = adjacentEnemyBattleUnitIds(humanKnightId, enemyRatIds)
        assertThat(adjacentEnemyRatIds).hasSize(2)
        adjacentEnemyRatIds.forEach { ratId ->
            val rat = battleUnitApi.searchBattleUnitById(ratId)!!
            assertThat(rat.ongoingEffects.delayedEffects).contains("venom-damage")
        }
    }

    private fun approachEnemyBattleUnits(
        battleUnitId: String,
        enemyBattleUnitIds: List<String>,
    ) {
        var rounds = 0
        while (adjacentEnemyBattleUnitIds(battleUnitId, enemyBattleUnitIds).size < 2) {
            require(++rounds < 20) { "The battle unit did not get adjacent to two enemy battle units" }
            val position = battlefieldApi.searchPosition(battleUnitId)!!
            val nearestEnemyPosition =
                enemyBattleUnitIds
                    .map { enemyBattleUnitId -> battlefieldApi.searchPosition(enemyBattleUnitId)!! }
                    .minByOrNull { enemyPosition -> manhattanDistance(position, enemyPosition) }!!
            val currentDistanceToNearestEnemy = manhattanDistance(position, nearestEnemyPosition)
            val candidatePositions =
                battleUnitApi
                    .whereCanMove(battleUnitId)
                    .filter { candidate ->
                        candidate != position && manhattanDistance(candidate, nearestEnemyPosition) < currentDistanceToNearestEnemy
                    }
            val closerPosition = candidatePositions.minByOrNull { candidate -> manhattanDistance(candidate, nearestEnemyPosition) }
            if (closerPosition != null) {
                battleUnitApi.moveBattleUnit(
                    battleUnitId = battleUnitId,
                    moveToRow = closerPosition.row,
                    moveToColumn = closerPosition.column,
                )
            }
            battleApi.finishPlayerTurn()
            eventBus.dispatch()
        }
    }

    private fun adjacentEnemyBattleUnitIds(
        battleUnitId: String,
        candidateBattleUnitIds: List<String>,
    ): List<String> {
        val position = battlefieldApi.searchPosition(battleUnitId)!!
        return candidateBattleUnitIds.filter { candidateId ->
            val candidatePosition = battlefieldApi.searchPosition(candidateId)!!
            manhattanDistance(position, candidatePosition) == 1
        }
    }

    private fun manhattanDistance(
        from: PositionDto,
        to: PositionDto,
    ): Int = abs(from.row - to.row) + abs(from.column - to.column)
}
