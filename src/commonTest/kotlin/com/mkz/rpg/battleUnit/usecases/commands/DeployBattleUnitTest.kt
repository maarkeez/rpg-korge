package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitError
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class DeployBattleUnitTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val deployBattleUnit =
        DeployBattleUnit(
            battleUnitRepository = queries.battleUnitRepository,
            eventBus = eventBus,
            searchUnitById = queries.searchUnitById,
            searchPlayerById = queries.searchPlayerById,
            canBattlefieldTileBeOccupied = queries.canBattlefieldTileBeOccupied,
        )

    @Test
    fun `should deploy battle unit when the battlefield tile can be occupied`() {
        // Given
        val unit = unit().also(queries.unitRepository::create).toDto()
        val player = player().also(queries.playerRepository::create).toDto()
        queries.battlefieldRepository.create(battlefield(". ."))
        // When
        deployBattleUnit(
            battleUnitId = "battle-unit-1",
            unitId = unit.id,
            playerId = player.id,
            deployAtRow = 0,
            deployAtColumn = 0,
        )
        // Then
        val storedBattleUnit = queries.battleUnitRepository.searchById("battle-unit-1")?.toDto()
        storedBattleUnit.shouldNotBeNull()
        storedBattleUnit.playerId shouldBe player.id
        storedBattleUnit.unitId shouldBe unit.id
        storedBattleUnit.remainingHealthPoints shouldBe unit.healthPoints
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.BattleUnitDeployed(playerId = player.id, battleUnitId = "battle-unit-1", row = 0, column = 0),
        )
    }

    @Test
    fun `should throw unit not found when the unit does not exist`() {
        // Given
        // When
        val error =
            shouldThrowAny {
                deployBattleUnit("battle-unit-1", "unknown-unit", "player-1", 0, 0)
            }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.UnitNotFound>()
    }

    @Test
    fun `should throw player not found when the player does not exist`() {
        // Given
        val unit = unit().also(queries.unitRepository::create).toDto()
        // When
        val error =
            shouldThrowAny {
                deployBattleUnit("battle-unit-1", unit.id, "unknown-player", 0, 0)
            }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.PlayerNotFound>()
    }

    @Test
    fun `should throw tile error when the battlefield tile can not be occupied`() {
        // Given
        val unit = unit().also(queries.unitRepository::create).toDto()
        val player = player().also(queries.playerRepository::create).toDto()
        queries.battlefieldRepository.create(battlefield("# ."))
        // When
        val error =
            shouldThrowAny {
                deployBattleUnit("battle-unit-1", unit.id, player.id, 0, 0)
            }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.BattlefieldTileCanNotBeOccupied>()
    }
}
