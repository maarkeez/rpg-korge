package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TARGET
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.ADJACENT_ENEMY
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrain
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitManaged
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuTurnFinished
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainMother.cpuBattleBrain
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ManageCpuBattleUnitTest {
    private val queries = InMemoryQueries()
    private val cpuBattleBrainRepository = queries.cpuBattleBrainRepository
    private val eventBus = FakeEventBus()
    private val manageCpuBattleUnit =
        ManageCpuBattleUnit(
            cpuBattleBrainRepository = cpuBattleBrainRepository,
            searchBattleUnitsByPlayerId = queries.searchBattleUnitsByPlayerId,
            whatShouldCast = queries.whatShouldCast,
            whereShouldMove = queries.whereShouldMove,
            eventBus = eventBus,
        )
    private val cpu = player(id = "cpu", type = CPU).toDto()
    private val human = player(id = "human").toDto()

    init {
        queries.playerRepository.create(player(id = "cpu", type = CPU))
        queries.playerRepository.create(player(id = "human"))
        queries.effectRepository.create(decreaseHealthEffect(id = "damage", damage = 10, applicationType = "IMMEDIATELY"))
        queries.abilityRepository.create(ability(id = "sword", cost = 0, cooldown = 0, targeting = ADJACENT_ENEMY, effectSpecs = listOf(effectSpec("damage", SELECTED_TARGET))))
    }

    // Stores the CPU rats (`R` is unit-1 and `S` is unit-2), that know the given abilities, and the human knight `K`, standing on the battlefield map.
    private fun givenBattlefield(
        map: String,
        ratAbilities: List<String> = emptyList(),
    ) {
        val ratUnit = unit(id = "rat", healthPoints = 20, movementRange = 2, abilities = ratAbilities).also(queries.unitRepository::create)
        val knightUnit = unit(id = "knight", healthPoints = 100).also(queries.unitRepository::create)
        queries.battleUnitRepository.create(battleUnit(id = "unit-1", unit = ratUnit.toDto(), player = cpu))
        queries.battleUnitRepository.create(battleUnit(id = "unit-2", unit = ratUnit.toDto(), player = cpu))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = knightUnit.toDto(), player = human))
        val occupants = mapOf('R' to "unit-1", 'S' to "unit-2", 'K' to "knight-1").filterKeys { letter -> letter in map }
        queries.battlefieldRepository.create(battlefield(map, occupants = occupants))
    }

    @Test
    fun `should cast and await the cast when the next battle unit should cast`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain())
        // The rat stands next to the knight.
        givenBattlefield(
            """
            R K
            S .
            """,
            ratAbilities = listOf("sword"),
        )
        // When
        manageCpuBattleUnit("cpu")
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestCastAbility(battleUnitId = "unit-1", abilityId = "sword", castGroup = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1))),
        )
        cpuBattleBrainRepository
            .search()
            ?.toDto()
            ?.isAwaitingAction
            .shouldBeTrue()
    }

    @Test
    fun `should move and await the move when the next battle unit should move and not cast`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain())
        // The rat has no abilities, and walks towards the knight.
        givenBattlefield(
            """
            R . . K
            S . . .
            """,
        )
        // When
        manageCpuBattleUnit("cpu")
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestMoveBattleUnit(battleUnitId = "unit-1", moveToRow = 0, moveToColumn = 2))
        cpuBattleBrainRepository
            .search()
            ?.toDto()
            ?.isAwaitingAction
            .shouldBeTrue()
    }

    @Test
    fun `should manage the battle unit when it has nothing left to do`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain())
        // The rat has no abilities, and there is no enemy to walk to.
        givenBattlefield(
            """
            R .
            S .
            """,
        )
        // When
        manageCpuBattleUnit("cpu")
        // Then
        eventBus.shouldHavePublishedEvents(CpuBattleUnitManaged(playerId = "cpu", battleUnitId = "unit-1"))
        cpuBattleBrainRepository.search()?.nextBattleUnitId() shouldBe "unit-2"
    }

    @Test
    fun `should manage the battle unit without acting when it has been defeated`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain())
        queries.battleUnitRepository.create(battleUnit(id = "unit-2", player = cpu))
        // When
        manageCpuBattleUnit("cpu")
        // Then
        eventBus.shouldHavePublishedEvents(CpuBattleUnitManaged(playerId = "cpu", battleUnitId = "unit-1"))
    }

    @Test
    fun `should finish the turn when every battle unit has been managed`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain(battleUnitIds = listOf("unit-1")).manageBattleUnit("unit-1").pullEvents().second)
        // When
        manageCpuBattleUnit("cpu")
        // Then
        eventBus.shouldHavePublishedEvents(CpuTurnFinished(playerId = "cpu"), BattleEvent.RequestFinishPlayerTurn)
    }

    @Test
    fun `should finish the turn when the action limit has been reached`() {
        // Given
        var limitedCpuBattleBrain = cpuBattleBrain()
        repeat(CpuBattleBrain.MAX_ACTIONS_PER_TURN) {
            limitedCpuBattleBrain =
                limitedCpuBattleBrain
                    .requestCast(battleUnitId = "unit-1", abilityId = "sword")
                    .resolveCast(battleUnitId = "unit-1", abilityId = "sword")
        }
        cpuBattleBrainRepository.save(limitedCpuBattleBrain.pullEvents().second)
        // When
        manageCpuBattleUnit("cpu")
        // Then
        eventBus.shouldHavePublishedEvents(CpuTurnFinished(playerId = "cpu"), BattleEvent.RequestFinishPlayerTurn)
    }

    @Test
    fun `should not act when the cpu battle brain is awaiting an action`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword"))
        // When
        manageCpuBattleUnit("cpu")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not act when the cpu battle brain belongs to another player`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain(playerId = "another-cpu"))
        // When
        manageCpuBattleUnit("cpu")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
