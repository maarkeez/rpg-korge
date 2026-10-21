package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.RequestBattleJoin
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleUnitDeployedTest {
    private val eventBus = InMemoryEventBus()
    private val onBattleUnitDeployed = OnBattleUnitDeployed(RequestBattleJoin(eventBus), eventBus)
    private val joinRequests = mutableListOf<BattleEvent.RequestJoinBattle>().also { requests -> eventBus.subscribe<BattleEvent.RequestJoinBattle> { requests += it } }

    @Test
    fun `should request the battle unit to join the battle when it is deployed`() {
        // Given
        eventBus.publish(BattleUnitEvent.BattleUnitDeployed(playerId = "cpu", battleUnitId = "bat-1", row = 0, column = 1))
        // When
        eventBus.dispatch()
        // Then
        joinRequests.shouldContainExactly(BattleEvent.RequestJoinBattle(battleUnitId = "bat-1", playerId = "cpu"))
    }
}
