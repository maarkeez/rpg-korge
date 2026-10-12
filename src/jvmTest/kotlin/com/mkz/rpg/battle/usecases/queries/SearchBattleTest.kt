package com.mkz.rpg.battle.usecases.queries

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleMother.battle
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchBattleTest {
    private val battleRepository = InMemoryBattleRepository()
    private val searchBattle = SearchBattle(battleRepository)

    @Test
    fun `should return battle when a battle exists`() {
        // Given
        val battle = battle()
        battleRepository.create(battle)
        // When
        val result = searchBattle()
        // Then
        result shouldBe battle.toDto()
    }

    @Test
    fun `should return null when no battle exists`() {
        // Given
        // When
        val result = searchBattle()
        // Then
        result.shouldBeNull()
    }
}
