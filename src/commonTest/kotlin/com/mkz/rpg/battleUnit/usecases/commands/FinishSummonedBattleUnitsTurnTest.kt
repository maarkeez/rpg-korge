package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitVanished
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FinishSummonedBattleUnitsTurnTest {
    private val queries = InMemoryQueries()
    private val battleUnitRepository = queries.battleUnitRepository
    private val eventBus = FakeEventBus()
    private val finishSummonedBattleUnitsTurn = FinishSummonedBattleUnitsTurn(battleUnitRepository, queries.searchPosition, eventBus)
    private val human = player(id = "human").toDto()
    private val cpu = player(id = "cpu").toDto()
    private val bee = unit(id = "bee").toDto()

    // The knight-1 and the bees of the human, and the bat of the cpu, stand on the battlefield.
    private fun givenBattlefield() {
        queries.battlefieldRepository.create(
            battlefield(
                """
                K A B
                C D .
                """,
                occupants = mapOf('K' to "knight-1", 'A' to "bee-1", 'B' to "bee-2", 'C' to "bat-1", 'D' to "bee-3"),
            ),
        )
    }

    @Test
    fun `should leave the summons of the player in the battle with one turn fewer`() {
        // Given
        givenBattlefield()
        battleUnitRepository.create(battleUnit(id = "bee-1", unit = bee, player = human, remainingTurns = 3))
        // When
        finishSummonedBattleUnitsTurn(playerId = "human", battleUnitIds = setOf("bee-1"))
        // Then
        battleUnitRepository.searchById("bee-1")!!.toDto().remainingTurns shouldBe 2
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should vanish and delete the summons of the player that finish their last turn`() {
        // Given
        givenBattlefield()
        battleUnitRepository.create(battleUnit(id = "bee-1", unit = bee, player = human, remainingTurns = 3))
        battleUnitRepository.create(battleUnit(id = "bee-2", unit = bee, player = human, remainingTurns = 1))
        // When
        finishSummonedBattleUnitsTurn(playerId = "human", battleUnitIds = setOf("bee-1", "bee-2"))
        // Then
        battleUnitRepository.searchById("bee-1")!!.toDto().remainingTurns shouldBe 2
        battleUnitRepository.searchById("bee-2").shouldBeNull()
        eventBus.shouldHavePublishedEvents(
            BattleUnitVanished(playerId = "human", battleUnitId = "bee-2", unitId = "bee", row = 0, column = 2),
            BattlefieldEvent.RequestRemoveOccupant(battleUnitId = "bee-2"),
        )
    }

    @Test
    fun `should not change the battle units that never vanish`() {
        // Given
        givenBattlefield()
        val knight = battleUnit(id = "knight-1", player = human)
        battleUnitRepository.create(knight)
        // When
        finishSummonedBattleUnitsTurn(playerId = "human", battleUnitIds = setOf("knight-1"))
        // Then
        battleUnitRepository.searchById("knight-1") shouldBe knight
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not count the turn of the summons of the other players`() {
        // Given
        givenBattlefield()
        battleUnitRepository.create(battleUnit(id = "bat-1", player = cpu, remainingTurns = 1))
        // When
        finishSummonedBattleUnitsTurn(playerId = "human", battleUnitIds = setOf("bat-1"))
        // Then
        battleUnitRepository.searchById("bat-1")!!.toDto().remainingTurns shouldBe 1
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not count the turn of the summons out of the battle`() {
        // Given
        givenBattlefield()
        battleUnitRepository.create(battleUnit(id = "bee-3", unit = bee, player = human, remainingTurns = 1))
        // When
        finishSummonedBattleUnitsTurn(playerId = "human", battleUnitIds = emptySet())
        // Then
        battleUnitRepository.searchById("bee-3")!!.toDto().remainingTurns shouldBe 1
        eventBus.shouldNotHavePublishedEvents()
    }
}
