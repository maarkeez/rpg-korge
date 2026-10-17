package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleMother.battle
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LeaveBattleTest {
    private val battleRepository = InMemoryBattleRepository()
    private val leaveBattle = LeaveBattle(battleRepository)

    @Test
    fun `should remove the battle unit from the battle when a battle exists`() {
        // Given
        battleRepository.create(battle(battleUnitIds = setOf("battle-unit-1", "battle-unit-2")))
        // When
        leaveBattle(battleUnitId = "battle-unit-2")
        // Then
        battleRepository.search()?.toDto()?.battleUnitIds shouldBe setOf("battle-unit-1")
    }

    @Test
    fun `should not start a battle when no battle exists`() {
        // When
        leaveBattle(battleUnitId = "battle-unit-2")
        // Then
        battleRepository.search().shouldBeNull()
    }
}
