package com.mkz.tessavar.cpuWanderBrain.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.cpuWanderBrain.adapters.storage.InMemoryCpuWanderBrainRepository
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainMother.cpuWanderBrain
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainMother.withLeaderApart
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.WanderWhileExploring
import com.mkz.tessavar.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnExplorationSecondStartedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries(random = FixedDoublesRandom(0.9))
    private val cpuWanderBrainRepository = InMemoryCpuWanderBrainRepository()
    private val onExplorationSecondStarted =
        OnExplorationSecondStarted(
            WanderWhileExploring(
                cpuWanderBrainRepository,
                WanderingSteps(cpuWanderBrainRepository, queries.whereShouldWander, queries.isOutsideTerritory, queries.searchPosition, eventBus),
                queries.isInSight,
                eventBus,
            ),
            eventBus,
        )
    private val walkRequests =
        mutableListOf<BattleUnitEvent.RequestWalkBattleUnitOutOfBattle>().also { requests ->
            eventBus.subscribe<BattleUnitEvent.RequestWalkBattleUnitOutOfBattle> {
                requests +=
                    it
            }
        }

    @Test
    fun `should wander the cpu battle units when a second starts while exploring`() {
        // Given
        val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        queries.battlefieldRepository.create(battlefield(withLeaderApart("R .\n. ."), occupants = mapOf('R' to "rat-1", 'K' to "knight-1")))
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        cpuWanderBrainRepository.save(cpuWanderBrain(homes = mapOf("rat-1" to (0 to 0))))
        eventBus.publish(PartyEvent.ExplorationSecondStarted(playerId = "human"))
        // When
        eventBus.dispatch()
        // Then
        walkRequests.shouldContainExactly(BattleUnitEvent.RequestWalkBattleUnitOutOfBattle(battleUnitId = "rat-1", walkToRow = 1, walkToColumn = 0))
    }
}
