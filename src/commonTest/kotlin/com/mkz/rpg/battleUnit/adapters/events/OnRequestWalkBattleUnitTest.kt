package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.commands.WalkBattleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnRequestWalkBattleUnitTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onRequestWalkBattleUnit =
        OnRequestWalkBattleUnit(WalkBattleUnit(queries.battleUnitRepository, eventBus, queries.searchPosition, queries.searchShortestPath), eventBus)
    private val movedBattleUnitIds = mutableListOf<String>().also { moved -> eventBus.subscribe<BattleUnitEvent.BattleUnitMoved> { moved += it.battleUnitId } }

    @Test
    fun `should walk the battle unit when its walk is requested`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1"))
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        eventBus.publish(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 1))
        // When
        eventBus.dispatch()
        // Then
        movedBattleUnitIds.shouldContainExactly("knight-1")
    }
}
