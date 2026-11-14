package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import kotlin.test.Test

class RequestJoinBattleTest {
    private val eventBus = FakeEventBus()
    private val requestJoinBattle = RequestJoinBattle(eventBus)

    @Test
    fun `should request the battle unit to join the battle with its player`() {
        // When
        requestJoinBattle(playerId = "human", battleUnitId = "knight-1")
        // Then
        eventBus.shouldHavePublishedEvents(BattleEvent.RequestJoinBattle(battleUnitId = "knight-1", playerId = "human"))
    }
}
