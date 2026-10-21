package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleMother.battle
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class JoinBattleTest {
    private val battleRepository = InMemoryBattleRepository()
    private val joinBattle = JoinBattle(battleRepository)

    @Test
    fun `should add the battle unit to the battle when a battle exists`() {
        // Given
        battleRepository.create(battle(battleUnitIds = setOf("battle-unit-1")))
        // When
        joinBattle(battleUnitId = "battle-unit-2", playerId = "player-2")
        // Then
        battleRepository.search()?.toDto()?.battleUnitIds shouldBe setOf("battle-unit-1", "battle-unit-2")
    }

    @Test
    fun `should not start a battle when no battle exists`() {
        // When
        joinBattle(battleUnitId = "battle-unit-2", playerId = "player-2")
        // Then
        battleRepository.search().shouldBeNull()
    }
}
