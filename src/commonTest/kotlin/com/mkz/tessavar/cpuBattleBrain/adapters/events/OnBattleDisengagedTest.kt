package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.JoinDisengagedBattle
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleDisengagedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onBattleDisengaged =
        OnBattleDisengaged(
            JoinDisengagedBattle(queries.searchHumanPlayer, queries.searchOpponent, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus),
            eventBus,
        )
    private val requests =
        mutableListOf<BattleEvent.RequestResolveDisengagedBattle>().also { requested -> eventBus.subscribe<BattleEvent.RequestResolveDisengagedBattle> { requested += it } }

    @Test
    fun `should have the cpu battle units join the disengaged battle when it is disengaged`() {
        // Given
        eventBus.publish(BattleEvent.BattleDisengaged(battleUnitIds = emptySet()))
        // When
        eventBus.dispatch()
        // Then
        requests.shouldContainExactly(BattleEvent.RequestResolveDisengagedBattle(joiningPlayerId = null, joiningBattleUnitIds = emptyList()))
    }
}
