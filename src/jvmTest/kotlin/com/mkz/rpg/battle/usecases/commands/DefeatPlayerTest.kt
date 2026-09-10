package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother
import com.mkz.rpg.battleUnit.usecases.queries.HasAllBattleUnitsDefeated
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DefeatPlayerTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val hasAllBattleUnitsDefeated: HasAllBattleUnitsDefeated = mock()
    private val defeatPlayer =
        DefeatPlayer(
            hasAllBattleUnitsDefeated = hasAllBattleUnitsDefeated,
            battleRepository = battleRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should defeat player when all its battle units are defeated`() {
        // Given
        val players = listOf("player-1", "player-2")
        battleRepository.create(
            BattleMother
                .battle(players),
        )
        val defeatedPlayer = players[1]
        whenever(hasAllBattleUnitsDefeated(defeatedPlayer)).thenReturn(true)
        // When
        defeatPlayer(playerId = defeatedPlayer)
        // Then
        val storedBattle = battleRepository.search()?.toDto()
        assertThat(storedBattle)
            .isEqualTo(Battle.Dto(currentPlayerTurn = players.first(), currentRound = 1))
        assertThat(eventBus)
            .hasPublishedEvents(BattleEvent.PlayerDefeated(defeatedPlayer))
    }

    @Test
    fun `should not defeat player when its battle units are not all defeated`() {
        // Given
        val players = listOf("player-1", "player-2")
        battleRepository.create(
            BattleMother
                .battle(players),
        )
        val defeatedPlayer = players[1]
        whenever(hasAllBattleUnitsDefeated(defeatedPlayer)).thenReturn(false)
        // When
        defeatPlayer(playerId = defeatedPlayer)
        // Then
        val storedBattle = battleRepository.search()?.toDto()
        assertThat(storedBattle)
            .isEqualTo(Battle.Dto(currentPlayerTurn = players.first(), currentRound = 1))
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
