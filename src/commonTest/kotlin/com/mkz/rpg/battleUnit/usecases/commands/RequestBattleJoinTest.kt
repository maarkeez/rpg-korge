package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import kotlin.test.Test

class RequestBattleJoinTest {
    private val eventBus = FakeEventBus()
    private val requestBattleJoin = RequestBattleJoin(eventBus)

    @Test
    fun `should request the battle unit to join the battle with its player`() {
        // When
        requestBattleJoin(playerId = "human", battleUnitId = "knight-1")
        // Then
        eventBus.shouldHavePublishedEvents(BattleEvent.RequestJoinBattle(battleUnitId = "knight-1", playerId = "human"))
    }
}
