package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.commands.StartNextRound
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnEngagementUpdatedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val battleRepository = InMemoryBattleRepository()
    private val onEngagementUpdated =
        OnEngagementUpdated(
            eventBus = eventBus,
            startNextRound = StartNextRound(queries.searchBattleUnitById, battleRepository, eventBus),
        )

    @Test
    fun `should start next round with the joining battle units and without the leaving ones when the engagement is updated`() {
        // Given
        listOf("rat-1", "rat-2").forEach { ratId -> queries.battleUnitRepository.create(battleUnit(id = ratId, player = player(id = "cpu").toDto())) }
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = player(id = "human").toDto()))
        battleRepository.create(battle(players = listOf("cpu", "human"), battleUnitIds = setOf("rat-1", "knight-1")))
        eventBus.publish(CpuBattleBrainEvent.EngagementUpdated(joiningBattleUnitIds = listOf("rat-2"), leavingBattleUnitIds = listOf("rat-1")))
        // When
        eventBus.dispatch()
        // Then
        val battle = battleRepository.search()!!.toDto()
        battle.currentRound shouldBe 2
        battle.battleUnitIds shouldBe setOf("rat-2", "knight-1")
    }
}
