package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitVanished
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class VanishSummonedBattleUnitsTest {
    private val queries = InMemoryQueries()
    private val battleUnitRepository = queries.battleUnitRepository
    private val eventBus = FakeEventBus()
    private val vanishSummonedBattleUnits = VanishSummonedBattleUnits(battleUnitRepository, queries.searchPosition, eventBus)
    private val human = player(id = "human").toDto()
    private val bee = unit(id = "bee").toDto()

    // The knight-1 and the bees of the human stand on the battlefield. The bee-3 was defeated, and stands no more.
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
    fun `should vanish and delete every summon left, whatever turns it has left`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "bee-1", unit = bee, player = human, remainingTurns = 4))
        battleUnitRepository.create(battleUnit(id = "bee-2", unit = bee, player = human, remainingTurns = 1))
        // When
        vanishSummonedBattleUnits()
        // Then
        battleUnitRepository.searchById("bee-1").shouldBeNull()
        battleUnitRepository.searchById("bee-2").shouldBeNull()
        eventBus.shouldHavePublishedEvents(
            BattleUnitVanished(playerId = "human", battleUnitId = "bee-1", unitId = "bee", row = 0, column = 1),
            BattlefieldEvent.RequestRemoveOccupant(battleUnitId = "bee-1"),
            BattleUnitVanished(playerId = "human", battleUnitId = "bee-2", unitId = "bee", row = 0, column = 2),
            BattlefieldEvent.RequestRemoveOccupant(battleUnitId = "bee-2"),
        )
    }

    @Test
    fun `should keep the battle units that never vanish and the defeated summons`() {
        // Given
        val knight = battleUnit(id = "knight-1", player = human)
        val defeatedBee = battleUnit(id = "bee-3", unit = bee, player = human, remainingHealthPoints = 0, remainingTurns = 4)
        battleUnitRepository.create(knight)
        battleUnitRepository.create(defeatedBee)
        // When
        vanishSummonedBattleUnits()
        // Then
        battleUnitRepository.searchById("knight-1") shouldBe knight
        battleUnitRepository.searchById("bee-3") shouldBe defeatedBee
        eventBus.shouldHavePublishedEvents()
    }
}
