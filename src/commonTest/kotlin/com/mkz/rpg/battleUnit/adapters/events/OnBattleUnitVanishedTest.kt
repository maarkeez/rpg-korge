package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.RequestBattleLeave
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleUnitVanishedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onBattleUnitVanished = OnBattleUnitVanished(RequestBattleLeave(queries.hasAllBattleUnitsDefeated, eventBus), eventBus)
    private val leaveRequests = mutableListOf<BattleEvent.RequestLeaveBattle>().also { requests -> eventBus.subscribe<BattleEvent.RequestLeaveBattle> { requests += it } }
    private val defeatRequests = mutableListOf<BattleEvent.RequestDefeatPlayer>().also { requests -> eventBus.subscribe<BattleEvent.RequestDefeatPlayer> { requests += it } }

    @Test
    fun `should request the summon to leave the battle, and its player to be defeated, when its last battle unit vanishes`() {
        // Given: the vanished summon is no longer stored, and the cpu has no other battle unit.
        eventBus.publish(BattleUnitEvent.BattleUnitVanished(playerId = "cpu", battleUnitId = "bat-1", unitId = "bat", row = 0, column = 1))
        // When
        eventBus.dispatch()
        // Then
        leaveRequests.shouldContainExactly(BattleEvent.RequestLeaveBattle(battleUnitId = "bat-1"))
        defeatRequests.shouldContainExactly(BattleEvent.RequestDefeatPlayer(playerId = "cpu"))
    }
}
