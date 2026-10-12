package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.InitialiseBattleUnits
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.battleUnit.usecases.services.FakeBattleUnitLoader
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlin.test.Test

class OnRequestInitialiseBattleUnitsTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onRequestInitialiseBattleUnits =
        OnRequestInitialiseBattleUnits(
            InitialiseBattleUnits(
                battleUnitLoader = FakeBattleUnitLoader(deployments = listOf(Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 0, column = 0))),
                battleUnitRepository = queries.battleUnitRepository,
                searchUnitById = queries.searchUnitById,
                searchPlayerById = queries.searchPlayerById,
                canBattlefieldTileBeOccupied = queries.canBattlefieldTileBeOccupied,
                eventBus = eventBus,
            ),
            eventBus,
        )

    @Test
    fun `should initialise the battle units when the request is dispatched`() {
        // Given
        queries.unitRepository.create(unit(id = "knight"))
        queries.playerRepository.create(player(id = "human"))
        queries.battlefieldRepository.create(battlefield(". ."))
        eventBus.publish(BattleUnitEvent.RequestInitialiseBattleUnits)
        // When
        eventBus.dispatch()
        // Then
        queries.battleUnitRepository.searchById("knight-1").shouldNotBeNull()
    }
}
