package com.mkz.rpg.exploration.adapters.events

import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.exploration.usecases.commands.JoinParty
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestJoinPartyTest {
    private val eventBus = InMemoryEventBus()
    private val explorationRepository = InMemoryExplorationRepository()
    private val onRequestJoinParty = OnRequestJoinParty(JoinParty(explorationRepository, eventBus), eventBus)

    @Test
    fun `should add the battle unit to the party when its joining is requested`() {
        // Given
        explorationRepository.save(exploration(battleUnitIds = listOf("knight-1")))
        eventBus.publish(ExplorationEvent.RequestJoinParty(battleUnitId = "viking"))
        // When
        eventBus.dispatch()
        // Then
        explorationRepository.search()!!.toDto().battleUnitIds shouldBe listOf("knight-1", "viking")
    }
}
