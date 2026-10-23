package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LeaveBattleTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val leaveBattle = LeaveBattle(battleRepository, eventBus)

    @Test
    fun `should remove the battle unit from the battle when a battle exists`() {
        // Given
        battleRepository.create(battle(players = listOf("cpu", "human"), battleUnits = mapOf("rat-1" to "cpu", "rat-2" to "cpu", "knight-1" to "human")))
        // When
        leaveBattle(battleUnitId = "rat-2")
        // Then
        battleRepository.search()?.toDto()?.battleUnitIds shouldBe setOf("rat-1", "knight-1")
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should disengage the battle when the last battle unit of a player leaves it`() {
        // Given
        battleRepository.create(battle(players = listOf("cpu", "human"), battleUnits = mapOf("rat-1" to "cpu", "knight-1" to "human")))
        // When
        leaveBattle(battleUnitId = "rat-1")
        // Then
        battleRepository.search()?.toDto()?.battleUnitIds shouldBe setOf("knight-1")
        eventBus.shouldHavePublishedEvents(BattleEvent.BattleDisengaged(battleUnitIds = setOf("knight-1")))
    }

    @Test
    fun `should not start a battle when no battle exists`() {
        // When
        leaveBattle(battleUnitId = "battle-unit-2")
        // Then
        battleRepository.search().shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }
}
