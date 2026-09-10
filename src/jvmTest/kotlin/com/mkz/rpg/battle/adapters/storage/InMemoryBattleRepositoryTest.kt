package com.mkz.rpg.battle.adapters.storage

import com.mkz.rpg.battle.domain.BattleMother.battle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InMemoryBattleRepositoryTest {
    private val battleRepository = InMemoryBattleRepository()

    @Nested
    inner class Create {
        @Test
        fun `should store the battle when the battle is created`() {
            // Given
            val createdBattle = battle()
            // When
            battleRepository.create(createdBattle)
            // Then
            val storedBattle = battleRepository.search()
            assertThat(storedBattle).isEqualTo(createdBattle)
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `should replace the stored battle when the battle is updated`() {
            // Given
            val storedBattle = battle()
            battleRepository.create(storedBattle)
            val updatedBattle = storedBattle.finishPlayerTurn()
            // When
            battleRepository.update(updatedBattle)
            // Then
            val searchResult = battleRepository.search()
            assertThat(searchResult).isEqualTo(updatedBattle)
        }
    }

    @Nested
    inner class Search {
        @Test
        fun `should return null when no battle is stored`() {
            // When
            val storedBattle = battleRepository.search()
            // Then
            assertThat(storedBattle).isNull()
        }
    }
}
