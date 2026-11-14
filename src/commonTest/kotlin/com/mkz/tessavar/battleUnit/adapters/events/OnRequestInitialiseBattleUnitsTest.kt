package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.InitialiseBattleUnits
import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.tessavar.battleUnit.usecases.services.FakeBattleUnitLoader
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
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
                battleUnitDeployment =
                    BattleUnitDeployment(
                        queries.battleUnitRepository,
                        queries.searchUnitById,
                        queries.searchMaximumAbilities,
                        queries.searchPlayerById,
                        queries.isTileVacant,
                    ),
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
