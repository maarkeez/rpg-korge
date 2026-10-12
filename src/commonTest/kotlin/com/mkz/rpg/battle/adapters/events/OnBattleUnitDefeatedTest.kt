package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent.PlayerDefeated
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.commands.DefeatPlayer
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.HasAllBattleUnitsDefeated
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleUnitDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val onBattleUnitDefeated =
        OnBattleUnitDefeated(
            eventBus = eventBus,
            defeatPlayer = DefeatPlayer(HasAllBattleUnitsDefeated(battleUnitRepository), battleRepository, eventBus),
        )
    private val defeatedPlayers = mutableListOf<PlayerDefeated>().also { defeated -> eventBus.subscribe<PlayerDefeated> { defeated += it } }

    @Test
    fun `should defeat player when a battle unit is defeated`() {
        // Given
        battleRepository.create(battle(players = listOf("player-1", "player-2")))
        battleUnitRepository.create(battleUnit(id = "battle-unit-1", player = player(id = "player-2").toDto(), unit = unit(healthPoints = 0).toDto()))
        eventBus.publish(BattleUnitEvent.BattleUnitDefeated(playerId = "player-2", battleUnitId = "battle-unit-1", defeatedAtRow = 1, defeatedAtColumn = 2))
        // When
        eventBus.dispatch()
        // Then
        defeatedPlayers.shouldContainExactly(PlayerDefeated("player-2"))
    }

    @Test
    fun `should not defeat player when a different event type is dispatched`() {
        // Given
        battleRepository.create(battle(players = listOf("player-1", "player-2")))
        battleUnitRepository.create(battleUnit(id = "battle-unit-1", player = player(id = "player-2").toDto(), unit = unit(healthPoints = 0).toDto()))
        eventBus.publish(BattleUnitEvent.BattleUnitDeployed(battleUnitId = "battle-unit-1", row = 0, column = 0))
        // When
        eventBus.dispatch()
        // Then
        defeatedPlayers.shouldBeEmpty()
    }
}
