package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.BattleUnitRespawned
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.battlefield.usecases.queries.IsTileVacant
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import com.mkz.tessavar.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.tessavar.unit.domain.UnitMother.unit
import com.mkz.tessavar.unit.usecases.queries.SearchUnitById
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class RespawnBattleUnitTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val unitRepository = InMemoryUnitRepository()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val eventBus = FakeEventBus()
    private val respawnBattleUnit = RespawnBattleUnit(battleUnitRepository, SearchUnitById(unitRepository), IsTileVacant(battlefieldRepository), eventBus)
    private val rat = unit(id = "rat", baseMaximumHealth = 20)
    private val cpu = player(id = "cpu").toDto()

    @BeforeTest
    fun setUp() {
        unitRepository.create(rat)
        battlefieldRepository.create(battlefield(". . ."))
        battleUnitRepository.create(battleUnit(id = "rat-1", unit = rat.toDto(), player = cpu, remainingHealth = 0))
    }

    private fun healthOf(battleUnitId: String) = battleUnitRepository.searchById(battleUnitId)!!.toDto().remainingHealth

    @Test
    fun `should bring the defeated battle unit back on the tile`() {
        // Given
        // When
        respawnBattleUnit(battleUnitId = "rat-1", row = 0, column = 2)
        // Then
        healthOf("rat-1") shouldBe 20
        eventBus.shouldHavePublishedEvents(
            BattleUnitRespawned(playerId = "cpu", battleUnitId = "rat-1", row = 0, column = 2),
            BattlefieldEvent.RequestOccupyTile(occupantId = "rat-1", row = 0, column = 2),
        )
    }

    @Test
    fun `should not respawn the battle unit when the tile is occupied`() {
        // Given
        battlefieldRepository.create(battlefield(". . K", occupants = mapOf('K' to "knight-1")))
        // When
        respawnBattleUnit(battleUnitId = "rat-1", row = 0, column = 2)
        // Then
        healthOf("rat-1") shouldBe 0
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not respawn a battle unit that is not defeated`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "rat-2", unit = rat.toDto(), player = cpu, remainingHealth = 5))
        // When
        respawnBattleUnit(battleUnitId = "rat-2", row = 0, column = 2)
        // Then
        healthOf("rat-2") shouldBe 5
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should ignore a battle unit that does not exist`() {
        // Given
        // When
        respawnBattleUnit(battleUnitId = "ghost", row = 0, column = 2)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
