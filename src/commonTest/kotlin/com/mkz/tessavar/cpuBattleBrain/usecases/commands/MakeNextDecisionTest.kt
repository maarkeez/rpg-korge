package com.mkz.tessavar.cpuBattleBrain.usecases.commands

import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto.Type.TARGETED_BATTLE_UNIT
import com.mkz.tessavar.ability.domain.Ability.Dto.TargetingDto.ADJACENT_ENEMY
import com.mkz.tessavar.ability.domain.AbilityMother.ability
import com.mkz.tessavar.ability.domain.AbilityMother.abilityEffect
import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrain
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleUnitDone
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuPlayerTurnFinished
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainMother.cpuBattleBrain
import com.mkz.tessavar.effect.domain.EffectMother.damageEffect
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MakeNextDecisionTest {
    private val queries = InMemoryQueries()
    private val cpuBattleBrainRepository = queries.cpuBattleBrainRepository
    private val eventBus = FakeEventBus()
    private val makeNextDecision =
        MakeNextDecision(
            cpuBattleBrainRepository = cpuBattleBrainRepository,
            searchBattleUnitsByPlayerId = queries.searchBattleUnitsByPlayerId,
            whatShouldCast = queries.whatShouldCast,
            whereShouldWalk = queries.whereShouldWalk,
            eventBus = eventBus,
        )
    private val cpu = player(id = "cpu", type = CPU).toDto()
    private val human = player(id = "human").toDto()

    init {
        queries.playerRepository.create(player(id = "cpu", type = CPU))
        queries.playerRepository.create(player(id = "human"))
        queries.effectRepository.create(damageEffect(id = "damage", damage = 10, timingType = "IMMEDIATELY"))
        queries.abilityRepository.create(ability(id = "sword", manaCost = 0, cooldown = 0, targeting = ADJACENT_ENEMY, abilityEffects = listOf(abilityEffect("damage", TARGETED_BATTLE_UNIT))))
    }

    private fun givenBattlefield(
        map: String,
        ratAbilities: List<String> = emptyList(),
    ) {
        val ratUnit = unit(id = "rat", baseMaximumHealth = 20, maximumSteps = 2, startingAbilities = ratAbilities).also(queries.unitRepository::create)
        val knightUnit = unit(id = "knight", baseMaximumHealth = 100).also(queries.unitRepository::create)
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
        givenBattlefield(
            """
            R K
            S .
            """,
            ratAbilities = listOf("sword"),
        )
        // When
        makeNextDecision("cpu")
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestCastAbility(battleUnitId = "unit-1", abilityId = "sword", castGroup = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1))),
        )
        cpuBattleBrainRepository
            .search()
            ?.toDto()
            ?.isAwaitingDecision
            .shouldBeTrue()
    }

    @Test
    fun `should walk and await the walk when the next battle unit should walk and not cast`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain())
        givenBattlefield(
            """
            R . . K
            S . . .
            """,
        )
        // When
        makeNextDecision("cpu")
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnitInBattle(battleUnitId = "unit-1", walkToRow = 0, walkToColumn = 2))
        cpuBattleBrainRepository
            .search()
            ?.toDto()
            ?.isAwaitingDecision
            .shouldBeTrue()
    }

    @Test
    fun `should mark the battle unit done when it has nothing left to do`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain())
        givenBattlefield(
            """
            R .
            S .
            """,
        )
        // When
        makeNextDecision("cpu")
        // Then
        eventBus.shouldHavePublishedEvents(CpuBattleUnitDone(playerId = "cpu", battleUnitId = "unit-1"))
        cpuBattleBrainRepository.search()?.nextBattleUnitId() shouldBe "unit-2"
    }

    @Test
    fun `should mark the battle unit done without acting when it has been defeated`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain())
        queries.battleUnitRepository.create(battleUnit(id = "unit-2", player = cpu))
        // When
        makeNextDecision("cpu")
        // Then
        eventBus.shouldHavePublishedEvents(CpuBattleUnitDone(playerId = "cpu", battleUnitId = "unit-1"))
    }

    @Test
    fun `should finish the player turn when every battle unit is done`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain(battleUnitIds = listOf("unit-1")).markDone("unit-1").pullEvents().second)
        // When
        makeNextDecision("cpu")
        // Then
        eventBus.shouldHavePublishedEvents(CpuPlayerTurnFinished(playerId = "cpu"), BattleEvent.RequestFinishPlayerTurn)
    }

    @Test
    fun `should finish the player turn when the decision limit has been reached`() {
        // Given
        var limitedCpuBattleBrain = cpuBattleBrain()
        repeat(CpuBattleBrain.MAX_DECISIONS_PER_PLAYER_TURN) {
            limitedCpuBattleBrain =
                limitedCpuBattleBrain
                    .requestCast(battleUnitId = "unit-1", abilityId = "sword")
                    .resolveCast(battleUnitId = "unit-1", abilityId = "sword")
        }
        cpuBattleBrainRepository.save(limitedCpuBattleBrain.pullEvents().second)
        // When
        makeNextDecision("cpu")
        // Then
        eventBus.shouldHavePublishedEvents(CpuPlayerTurnFinished(playerId = "cpu"), BattleEvent.RequestFinishPlayerTurn)
    }

    @Test
    fun `should not act when the cpu battle brain is awaiting a decision`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain().requestCast(battleUnitId = "unit-1", abilityId = "sword"))
        // When
        makeNextDecision("cpu")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not act when the cpu battle brain belongs to another player`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain(playerId = "another-cpu"))
        // When
        makeNextDecision("cpu")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
