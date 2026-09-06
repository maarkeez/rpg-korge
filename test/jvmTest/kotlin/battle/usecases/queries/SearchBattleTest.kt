package battle.usecases.queries

import battle.adapters.storage.*
import battle.domain.*
import org.junit.*

class SearchBattleTest {
    private val battleRepository = InMemoryBattleRepository()
    private val searchBattle = SearchBattle(battleRepository)

    @Test
    fun `should return battle when a battle exists`() {
        // Given
        val battle = BattleMother.battle()
        battleRepository.create(battle)
        // When
        val result = searchBattle()
        // Then
        org.assertj.core.api.Assertions.assertThat(result).isEqualTo(battle.toDto())
    }

    @Test
    fun `should return null when no battle exists`() {
        // Given
        // When
        val result = searchBattle()
        // Then
        org.assertj.core.api.Assertions.assertThat(result).isNull()
    }
}
