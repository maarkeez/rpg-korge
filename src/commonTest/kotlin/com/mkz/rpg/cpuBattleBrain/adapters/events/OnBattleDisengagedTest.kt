package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.ReengageEnemies
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleDisengagedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onBattleDisengaged =
        OnBattleDisengaged(
            ReengageEnemies(queries.searchHumanPlayer, queries.searchEnemyPlayer, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus),
            eventBus,
        )
    private val requests =
        mutableListOf<BattleEvent.RequestFinishDisengagedBattle>().also { requested -> eventBus.subscribe<BattleEvent.RequestFinishDisengagedBattle> { requested += it } }

    @Test
    fun `should re-engage the enemies when the battle is disengaged`() {
        // Given
        eventBus.publish(BattleEvent.BattleDisengaged(battleUnitIds = emptySet()))
        // When
        eventBus.dispatch()
        // Then
        requests.shouldContainExactly(BattleEvent.RequestFinishDisengagedBattle(joiningPlayerId = null, joiningBattleUnitIds = emptyList()))
    }
}
