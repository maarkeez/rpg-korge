package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.usecases.commands.StartBattle
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestStartBattleTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onRequestStartBattle = OnRequestStartBattle(StartBattle(battleRepository, eventBus), eventBus)

    @Test
    fun `should start the battle with the engaging battle units and the engaged ones when it is requested`() {
        // Given
        eventBus.publish(
            BattleEvent.RequestStartBattle(engagingPlayerId = "cpu", engagedPlayerId = "human", engagingBattleUnitIds = listOf("rat-1"), engagedBattleUnitIds = listOf("knight-1")),
        )
        // When
        eventBus.dispatch()
        // Then
        val battle = battleRepository.search()!!.toDto()
        battle.currentPlayerTurn shouldBe "cpu"
        battle.battleUnitIds shouldBe setOf("rat-1", "knight-1")
    }
}
