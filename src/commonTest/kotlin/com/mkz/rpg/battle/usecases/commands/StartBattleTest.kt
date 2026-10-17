package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class StartBattleTest {
    private val queries = InMemoryQueries()
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val startBattle = StartBattle(queries.searchBattleUnitsByPlayerId, battleRepository, eventBus)

    // The cpu has two rats, and the human two knights, one of them defeated.
    private fun givenBattleUnits() {
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = player(id = "cpu").toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-2", player = player(id = "cpu").toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = player(id = "human").toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "knight-2", player = player(id = "human").toDto(), unit = unit(healthPoints = 0).toDto()))
    }

    @Test
    fun `should start the first round with the engaging player when the cpu engages`() {
        // Given
        givenBattleUnits()
        // When
        startBattle(engagingPlayerId = "cpu", engagedPlayerId = "human", engagingBattleUnitIds = listOf("rat-1"))
        // Then
        val battle = battleRepository.search()?.toDto()
        battle?.currentRound shouldBe 1
        battle?.currentPlayerTurn shouldBe "cpu"
        eventBus.shouldHavePublishedEvents(BattleStarted, PlayerTurnStarted("cpu", setOf("rat-1", "knight-1")))
    }

    @Test
    fun `should put the engaging battle units and every living battle unit of the engaged player in the battle when the cpu engages`() {
        // Given
        givenBattleUnits()
        // When
        startBattle(engagingPlayerId = "cpu", engagedPlayerId = "human", engagingBattleUnitIds = listOf("rat-1"))
        // Then
        battleRepository.search()?.toDto()?.battleUnitIds shouldBe setOf("rat-1", "knight-1")
    }

    @Test
    fun `should not start another battle when a battle is in progress`() {
        // Given
        givenBattleUnits()
        battleRepository.create(battle(players = listOf("human", "cpu")))
        // When
        startBattle(engagingPlayerId = "cpu", engagedPlayerId = "human", engagingBattleUnitIds = listOf("rat-1"))
        // Then
        battleRepository.search()?.toDto()?.currentPlayerTurn shouldBe "human"
        eventBus.shouldNotHavePublishedEvents()
    }
}
