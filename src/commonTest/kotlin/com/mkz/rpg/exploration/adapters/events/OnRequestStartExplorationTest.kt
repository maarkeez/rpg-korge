package com.mkz.rpg.exploration.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.exploration.usecases.commands.StartExploration
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestStartExplorationTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val explorationRepository = InMemoryExplorationRepository()
    private val onRequestStartExploration =
        OnRequestStartExploration(StartExploration(queries.searchHumanPlayer, queries.searchBattleUnitsByPlayerId, explorationRepository, eventBus), eventBus)

    @Test
    fun `should start exploring when it is requested`() {
        // Given
        val human = player(id = "human", type = PlayerTypeDto.HUMAN)
        queries.playerRepository.create(human)
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = human.toDto()))
        eventBus.publish(ExplorationEvent.RequestStartExploration)
        // When
        eventBus.dispatch()
        // Then
        explorationRepository.search()?.toDto()?.leaderBattleUnitId shouldBe "knight-1"
    }
}
