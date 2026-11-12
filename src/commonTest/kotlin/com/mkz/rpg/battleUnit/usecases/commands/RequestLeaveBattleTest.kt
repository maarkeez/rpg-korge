package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
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
