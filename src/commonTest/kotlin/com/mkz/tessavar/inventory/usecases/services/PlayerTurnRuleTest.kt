package com.mkz.tessavar.inventory.usecases.services

import com.mkz.tessavar.battle.domain.BattleMother.battle
import com.mkz.tessavar.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.tessavar.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import kotlin.test.BeforeTest
import kotlin.test.Test

class PlayerTurnRuleTest {
    private val queries = InMemoryQueries()
    private val playerTurnRule = PlayerTurnRule(queries.searchParty, queries.searchBattle)

    @BeforeTest
    fun setUp() {
        queries.partyRepository.save(party(playerId = "human", battleUnitIds = listOf("knight-1")))
    }

    @Test
    fun `should allow the inventory during the player turn of the human player in a battle`() {
        // Given
        queries.battleRepository.create(battle(players = listOf("human", "cpu")))
        // Then
        shouldNotThrowAny { playerTurnRule.requirePlayerTurn("knight-1") }
    }

    @Test
    fun `should fail during the turn of another player in a battle`() {
        // Given
        queries.battleRepository.create(battle(players = listOf("cpu", "human")))
        // Then
        shouldThrow<NotTheBattleUnitPlayerTurn> { playerTurnRule.requirePlayerTurn("knight-1") }
    }

    @Test
    fun `should allow the inventory while exploring before any battle`() {
        // Then
        shouldNotThrowAny { playerTurnRule.requirePlayerTurn("knight-1") }
    }

    @Test
    fun `should allow the inventory while exploring once the battle has finished`() {
        // Given
        queries.battleRepository.create(battle(players = listOf("cpu", "human")).finish())
        // Then
        shouldNotThrowAny { playerTurnRule.requirePlayerTurn("knight-1") }
    }

    @Test
    fun `should fail when the battle unit is no party member`() {
        // Then
        shouldThrow<NotAPartyMember> { playerTurnRule.requirePlayerTurn("rat-1") }
    }

    @Test
    fun `should fail when there is no party`() {
        // Given
        val queries = InMemoryQueries()
        // Then
        shouldThrow<NotAPartyMember> { PlayerTurnRule(queries.searchParty, queries.searchBattle).requirePlayerTurn("knight-1") }
    }
}
