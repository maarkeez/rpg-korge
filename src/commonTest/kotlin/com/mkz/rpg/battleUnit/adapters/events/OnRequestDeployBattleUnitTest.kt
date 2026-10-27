package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.DeployBattleUnit
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestDeployBattleUnitTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onRequestDeployBattleUnit =
        OnRequestDeployBattleUnit(
            DeployBattleUnit(BattleUnitDeployment(queries.battleUnitRepository, queries.searchUnitById, queries.searchPlayerById, queries.canBattlefieldTileBeOccupied), eventBus),
            eventBus,
        )

    @Test
    fun `should deploy the battle unit, which never vanishes, when its deployment is requested`() {
        // Given
        queries.unitRepository.create(unit(id = "viking", healthPoints = 150))
        queries.playerRepository.create(player(id = "human"))
        queries.battlefieldRepository.create(battlefield(". . ."))
        eventBus.publish(BattleUnitEvent.RequestDeployBattleUnit(battleUnitId = "viking", unitId = "viking", playerId = "human", row = 0, column = 2))
        // When
        eventBus.dispatch()
        // Then
        val viking = queries.battleUnitRepository.searchById("viking")!!.toDto()
        viking.playerId shouldBe "human"
        viking.remainingHealthPoints shouldBe 150
        viking.remainingTurns.shouldBeNull()
    }
}
