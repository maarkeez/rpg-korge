package com.mkz.rpg.inventory.usecases.services

import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.rpg.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import kotlin.test.BeforeTest
import kotlin.test.Test

class PlayerTurnRuleTest {
    private val queries = InMemoryQueries()
    private val playerTurnRule = PlayerTurnRule(queries.searchParty, queries.searchBattle)

    // The human player explores with the knight. The rat is no party member.
    @BeforeTest
    fun setUp() {
        queries.partyRepository.save(party(playerId = "human", battleUnitIds = listOf("knight-1")))
    }

    @Test
    fun `should allow the inventory during the turn of the party player in a battle`() {
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
        queries.battleRepository.create(battle(players = listOf("cpu", "human")).finishWithoutWinner())
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
