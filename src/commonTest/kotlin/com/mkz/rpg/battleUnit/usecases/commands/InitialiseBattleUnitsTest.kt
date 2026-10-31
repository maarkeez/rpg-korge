package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitError.UnitNotFound
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDeployed
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.battleUnit.usecases.services.FakeBattleUnitLoader
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialiseBattleUnitsTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()

    private fun initialiseBattleUnits(loadedDeployments: List<Deployment>) =
        InitialiseBattleUnits(
            FakeBattleUnitLoader(loadedDeployments),
            queries.battleUnitRepository,
            BattleUnitDeployment(queries.battleUnitRepository, queries.searchUnitById, queries.searchMaximumAbilities, queries.searchPlayerById, queries.canBattlefieldTileBeOccupied),
            eventBus,
        )

    @Test
    fun `should deploy every loaded battle unit when battle units are initialised`() {
        // Given
        queries.unitRepository.create(unit(id = "knight"))
        queries.playerRepository.create(player(id = "player-one"))
        queries.battlefieldRepository.create(
            battlefield(
                """
                . .
                . .
                """,
            ),
        )
        val initialiseBattleUnits =
            initialiseBattleUnits(
                loadedDeployments =
                    listOf(
                        Deployment(battleUnitId = "player-1-unit-1", unitId = "knight", playerId = "player-one", row = 0, column = 0),
                        Deployment(battleUnitId = "player-1-unit-2", unitId = "knight", playerId = "player-one", row = 1, column = 1),
                    ),
            )
        // When
        initialiseBattleUnits()
        // Then
        queries.battleUnitRepository
            .searchById("player-1-unit-1")
            ?.toDto()
            ?.playerId shouldBe "player-one"
        queries.battleUnitRepository
            .searchById("player-1-unit-2")
            ?.toDto()
            ?.playerId shouldBe "player-one"
        eventBus.shouldHavePublishedEvents(
            BattleUnitDeployed(playerId = "player-one", battleUnitId = "player-1-unit-1", row = 0, column = 0),
            BattlefieldEvent.RequestOccupyTile(occupantId = "player-1-unit-1", row = 0, column = 0),
            BattleUnitDeployed(playerId = "player-one", battleUnitId = "player-1-unit-2", row = 1, column = 1),
            BattlefieldEvent.RequestOccupyTile(occupantId = "player-1-unit-2", row = 1, column = 1),
        )
    }

    @Test
    fun `should fail when the unit of a battle unit does not exist`() {
        // Given
        val initialiseBattleUnits =
            initialiseBattleUnits(
                loadedDeployments = listOf(Deployment(battleUnitId = "player-1-unit-1", unitId = "dragon", playerId = "player-one", row = 0, column = 0)),
            )
        // When
        val initialisation = { initialiseBattleUnits() }
        // Then
        shouldThrow<UnitNotFound> { initialisation() }
    }
}
