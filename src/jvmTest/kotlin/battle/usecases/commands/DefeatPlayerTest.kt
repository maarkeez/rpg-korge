package battle.usecases.commands

import battle.adapters.storage.InMemoryBattleRepository
import battle.domain.Battle
import battle.domain.BattleEvent
import battle.domain.BattleMother
import battleunit.usecases.queries.HasAllBattleUnitsDefeated
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import shared.domain.FakeEventBus
import shared.domain.assertThat

class DefeatPlayerTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
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
            _root_ide_package_.battle.domain.BattleMother
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
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents(BattleEvent.PlayerDefeated(defeatedPlayer))
    }

    @Test
    fun `should not defeat player when its battle units are not all defeated`() {
        // Given
        val players = listOf("player-1", "player-2")
        battleRepository.create(
            _root_ide_package_.battle.domain.BattleMother
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
