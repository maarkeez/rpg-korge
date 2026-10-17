package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.commands.JoinBattle
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnBattleUnitDeployedTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onBattleUnitDeployed = OnBattleUnitDeployed(eventBus, JoinBattle(battleRepository))

    @Test
    fun `should add the battle unit to the battle when it is deployed during the battle`() {
        // Given
        battleRepository.create(battle(battleUnitIds = setOf("battle-unit-1")))
        eventBus.publish(BattleUnitEvent.BattleUnitDeployed(battleUnitId = "battle-unit-2", row = 1, column = 2))
        // When
        eventBus.dispatch()
        // Then
        battleRepository.search()?.toDto()?.battleUnitIds shouldBe setOf("battle-unit-1", "battle-unit-2")
    }
}
