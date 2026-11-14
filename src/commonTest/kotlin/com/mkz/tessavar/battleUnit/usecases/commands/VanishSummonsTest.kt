package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.BattleUnitVanished
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class VanishSummonsTest {
    private val queries = InMemoryQueries()
    private val battleUnitRepository = queries.battleUnitRepository
    private val eventBus = FakeEventBus()
    private val vanishSummons = VanishSummons(battleUnitRepository, queries.searchPosition, eventBus)
    private val human = player(id = "human").toDto()
    private val bee = unit(id = "bee").toDto()

    init {
        queries.battlefieldRepository.create(
            battlefield(
                """
                K A B
                """,
                occupants = mapOf('K' to "knight-1", 'A' to "bee-1", 'B' to "bee-2"),
            ),
        )
    }

    @Test
    fun `should vanish and delete every summon left, whatever summon duration it has left`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "bee-1", unit = bee, player = human, remainingSummonDuration = 4))
        battleUnitRepository.create(battleUnit(id = "bee-2", unit = bee, player = human, remainingSummonDuration = 1))
        // When
        vanishSummons()
        // Then
        battleUnitRepository.searchById("bee-1").shouldBeNull()
        battleUnitRepository.searchById("bee-2").shouldBeNull()
        eventBus.shouldHavePublishedEvents(
            BattleUnitVanished(playerId = "human", battleUnitId = "bee-1", unitId = "bee", row = 0, column = 1),
            BattlefieldEvent.RequestRemoveOccupant(occupantId = "bee-1"),
            BattleUnitVanished(playerId = "human", battleUnitId = "bee-2", unitId = "bee", row = 0, column = 2),
            BattlefieldEvent.RequestRemoveOccupant(occupantId = "bee-2"),
        )
    }

    @Test
    fun `should keep the battle units that never vanish and the defeated summons`() {
        // Given
        val knight = battleUnit(id = "knight-1", player = human)
        val defeatedBee = battleUnit(id = "bee-3", unit = bee, player = human, remainingHealth = 0, remainingSummonDuration = 4)
        battleUnitRepository.create(knight)
        battleUnitRepository.create(defeatedBee)
        // When
        vanishSummons()
        // Then
        battleUnitRepository.searchById("knight-1") shouldBe knight
        battleUnitRepository.searchById("bee-3") shouldBe defeatedBee
        eventBus.shouldHavePublishedEvents()
    }
}
