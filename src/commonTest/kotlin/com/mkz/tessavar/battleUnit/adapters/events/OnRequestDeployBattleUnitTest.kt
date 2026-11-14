package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.DeployBattleUnit
import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestDeployBattleUnitTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onRequestDeployBattleUnit =
        OnRequestDeployBattleUnit(
            DeployBattleUnit(
                BattleUnitDeployment(queries.battleUnitRepository, queries.searchUnitById, queries.searchMaximumAbilities, queries.searchPlayerById, queries.isTileVacant),
                eventBus,
            ),
            eventBus,
        )

    @Test
    fun `should deploy the battle unit, which never vanishes, when its deployment is requested`() {
        // Given
        queries.unitRepository.create(unit(id = "viking", baseMaximumHealth = 150))
        queries.playerRepository.create(player(id = "human"))
        queries.battlefieldRepository.create(battlefield(". . ."))
        eventBus.publish(BattleUnitEvent.RequestDeployBattleUnit(battleUnitId = "viking", unitId = "viking", playerId = "human", row = 0, column = 2))
        // When
        eventBus.dispatch()
        // Then
        val viking = queries.battleUnitRepository.searchById("viking")!!.toDto()
        viking.playerId shouldBe "human"
        viking.remainingHealth shouldBe 150
        viking.remainingSummonDuration.shouldBeNull()
    }
}
