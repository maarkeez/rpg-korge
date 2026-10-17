package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.usecases.services.FakeBattleLoader
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialiseBattleTest {
    private val queries = InMemoryQueries()
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val initialiseBattle =
        InitialiseBattle(
            FakeBattleLoader(playerOrder = listOf("player-one", "player-two")),
            queries.searchBattleUnitsByPlayerId,
            battleRepository,
            eventBus,
        )

    @Test
    fun `should start the first round with the first loaded player when the battle is initialised`() {
        // When
        initialiseBattle()
        // Then
        val battle = battleRepository.search()?.toDto()
        battle?.currentRound shouldBe 1
        battle?.currentPlayerTurn shouldBe "player-one"
        eventBus.shouldHavePublishedEvents(BattleStarted, PlayerTurnStarted("player-one", emptySet()))
    }

    @Test
    fun `should put the battle units of every player in the battle when the battle is initialised`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = player(id = "player-one").toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = player(id = "player-two").toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-2", player = player(id = "player-two").toDto(), unit = unit(healthPoints = 0).toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "goat-1", player = player(id = "player-three").toDto()))
        // When
        initialiseBattle()
        // Then
        battleRepository.search()?.toDto()?.battleUnitIds shouldBe setOf("knight-1", "rat-1")
        eventBus.shouldHavePublishedEvents(BattleStarted, PlayerTurnStarted("player-one", setOf("knight-1", "rat-1")))
    }
}
