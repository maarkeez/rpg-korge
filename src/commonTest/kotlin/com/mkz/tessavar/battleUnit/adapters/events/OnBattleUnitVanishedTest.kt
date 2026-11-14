package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.RequestLeaveBattle
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.subscribe
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleUnitVanishedTest {
    private val eventBus = InMemoryEventBus()
    private val onBattleUnitVanished = OnBattleUnitVanished(RequestLeaveBattle(eventBus), eventBus)
    private val leaveRequests = mutableListOf<BattleEvent.RequestLeaveBattle>().also { requests -> eventBus.subscribe<BattleEvent.RequestLeaveBattle> { requests += it } }

    @Test
    fun `should request the summon to leave the battle when it vanishes`() {
        // Given
        eventBus.publish(BattleUnitEvent.BattleUnitVanished(playerId = "cpu", battleUnitId = "bat-1", unitId = "bat", row = 0, column = 1))
        // When
        eventBus.dispatch()
        // Then
        leaveRequests.shouldContainExactly(BattleEvent.RequestLeaveBattle(battleUnitId = "bat-1"))
    }
}
