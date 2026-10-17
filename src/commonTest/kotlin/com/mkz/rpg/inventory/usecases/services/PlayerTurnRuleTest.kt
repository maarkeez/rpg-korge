package com.mkz.rpg.inventory.usecases.services

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.inventory.domain.InventoryError.InventoryBattleUnitDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryError.NotTheBattleUnitPlayerTurn
import com.mkz.rpg.player.domain.PlayerMother.player
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import kotlin.test.BeforeTest
import kotlin.test.Test

class PlayerTurnRuleTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val battleRepository = InMemoryBattleRepository()
    private val playerTurnRule = PlayerTurnRule(SearchBattleUnitById(battleUnitRepository), SearchBattle(battleRepository))

    // The human player owns the knight, and the CPU player the rat.
    @BeforeTest
    fun setUp() {
        battleUnitRepository.create(battleUnit(id = "knight-1", player = player(id = "human").toDto()))
        battleUnitRepository.create(battleUnit(id = "rat-1", player = player(id = "cpu").toDto()))
    }

    @Test
    fun `should allow the inventory during the turn of its battle unit player in a battle`() {
        // Given
        battleRepository.create(battle(players = listOf("human", "cpu")))
        // Then
        shouldNotThrowAny { playerTurnRule.requirePlayerTurn("knight-1") }
    }

    @Test
    fun `should fail during the turn of another player in a battle`() {
        // Given
        battleRepository.create(battle(players = listOf("human", "cpu")))
        // Then
        shouldThrow<NotTheBattleUnitPlayerTurn> { playerTurnRule.requirePlayerTurn("rat-1") }
    }

    @Test
    fun `should allow the inventory of any battle unit while exploring before any battle`() {
        // Then
        shouldNotThrowAny { playerTurnRule.requirePlayerTurn("knight-1") }
        shouldNotThrowAny { playerTurnRule.requirePlayerTurn("rat-1") }
    }

    @Test
    fun `should allow the inventory of any battle unit while exploring once the battle has finished`() {
        // Given
        battleRepository.create(battle(players = listOf("human", "cpu")).finishWithoutWinner())
        // Then
        shouldNotThrowAny { playerTurnRule.requirePlayerTurn("rat-1") }
    }

    @Test
    fun `should fail when the battle unit does not exist`() {
        // Then
        shouldThrow<InventoryBattleUnitDoesNotExist> { playerTurnRule.requirePlayerTurn("bat-1") }
    }
}
