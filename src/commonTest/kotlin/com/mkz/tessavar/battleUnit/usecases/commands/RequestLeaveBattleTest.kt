package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import kotlin.test.Test

class RequestLeaveBattleTest {
    private val eventBus = FakeEventBus()
    private val requestLeaveBattle = RequestLeaveBattle(eventBus)

    @Test
    fun `should request the battle unit to leave the battle`() {
        // When
        requestLeaveBattle(battleUnitId = "rat-1")
        // Then
        eventBus.shouldHavePublishedEvents(BattleEvent.RequestLeaveBattle(battleUnitId = "rat-1"))
    }
}
