package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.HasAllBattleUnitsDefeated
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DefeatPlayerTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val defeatPlayer =
        DefeatPlayer(
            hasAllBattleUnitsDefeated = HasAllBattleUnitsDefeated(battleUnitRepository),
            battleRepository = battleRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should defeat player when all its battle units are defeated`() {
        // Given
        val players = listOf("player-1", "player-2")
        battleRepository.create(
            BattleMother
                .battle(players),
        )
        val defeatedPlayer = players[1]
        battleUnitRepository.create(battleUnit(player = player(id = defeatedPlayer).toDto(), unit = unit(healthPoints = 0).toDto()))
        // When
        defeatPlayer(playerId = defeatedPlayer)
        // Then
        val storedBattle = battleRepository.search()?.toDto()
        storedBattle shouldBe Battle.Dto(currentPlayerTurn = players.first(), currentRound = 1, isFinished = false)
        eventBus.shouldHavePublishedEvents(BattleEvent.PlayerDefeated(defeatedPlayer))
    }

    @Test
    fun `should not defeat player when its battle units are not all defeated`() {
        // Given
        val players = listOf("player-1", "player-2")
        battleRepository.create(
            BattleMother
                .battle(players),
        )
        val defeatedPlayer = players[1]
        battleUnitRepository.create(battleUnit(player = player(id = defeatedPlayer).toDto(), unit = unit(healthPoints = 0).toDto()))
        battleUnitRepository.create(battleUnit(player = player(id = defeatedPlayer).toDto(), unit = unit(healthPoints = 10).toDto()))
        // When
        defeatPlayer(playerId = defeatedPlayer)
        // Then
        val storedBattle = battleRepository.search()?.toDto()
        storedBattle shouldBe Battle.Dto(currentPlayerTurn = players.first(), currentRound = 1, isFinished = false)
        eventBus.publishedEvents.shouldBeEmpty()
    }
}
