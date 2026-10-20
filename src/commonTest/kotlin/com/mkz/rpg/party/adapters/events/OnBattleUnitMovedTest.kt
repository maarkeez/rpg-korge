package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.commands.MarkBattleUnitMoved
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnBattleUnitMovedTest {
    private val eventBus = InMemoryEventBus()
    private val partyRepository = InMemoryPartyRepository()
    private val onBattleUnitMoved = OnBattleUnitMoved(MarkBattleUnitMoved(partyRepository), eventBus)

    @Test
    fun `should remember the battle unit moved during the current second when it moves`() {
        // Given
        partyRepository.save(party())
        eventBus.publish(BattleUnitEvent.BattleUnitMoved(battleUnitId = "knight-1", fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 1, path = emptyList()))
        // When
        eventBus.dispatch()
        // Then
        partyRepository.search()!!.standingBattleUnitIds(setOf("knight-1", "knight-2")) shouldBe setOf("knight-2")
    }
}
