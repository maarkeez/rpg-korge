package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother
import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.player.usecases.queries.SearchEnemyPlayer
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DefeatPlayerTest {
    private val battleUnitIds = setOf("battle-unit-1", "battle-unit-2")
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val playerRepository = InMemoryPlayerRepository()
    private val defeatPlayer =
        DefeatPlayer(
            searchEnemyPlayer = SearchEnemyPlayer(playerRepository),
            battleRepository = battleRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should defeat the player when a battle is in progress`() {
        // Given
        val players = listOf("player-1", "player-2")
        battleRepository.create(
            BattleMother
                .battle(players, battleUnitIds),
        )
        val defeatedPlayer = players[1]
        // When
        defeatPlayer(playerId = defeatedPlayer)
        // Then
        val storedBattle = battleRepository.search()?.toDto()
        storedBattle shouldBe Battle.Dto(currentPlayerTurn = players.first(), currentRound = 1, isFinished = false, battleUnitIds = battleUnitIds)
        eventBus.shouldHavePublishedEvents(BattleEvent.PlayerDefeated(defeatedPlayer))
    }

    @Test
    fun `should defeat the player and leave their enemy as the only player while exploring`() {
        // Given
        playerRepository.create(player(id = "human"))
        playerRepository.create(player(id = "cpu"))
        // When
        defeatPlayer(playerId = "cpu")
        // Then
        val storedBattle = battleRepository.search()!!
        storedBattle.isBattleFinished() shouldBe true
        storedBattle.toDto().currentPlayerTurn shouldBe "human"
        eventBus.shouldHavePublishedEvents(BattleEvent.PlayerDefeated("cpu"))
    }

    @Test
    fun `should defeat the player in place of the finished battle while exploring again`() {
        // Given
        playerRepository.create(player(id = "human"))
        playerRepository.create(player(id = "cpu"))
        // The battle finished once the human turn, the last one of the round, finished.
        battleRepository.create(
            BattleMother
                .battle(listOf("cpu", "human"))
                .finishPlayerTurn()
                .finishWithoutWinner()
                .pullEvents()
                .second,
        )
        // When
        defeatPlayer(playerId = "human")
        // Then
        battleRepository.search()!!.toDto().currentPlayerTurn shouldBe "cpu"
        eventBus.shouldHavePublishedEvents(BattleEvent.PlayerDefeated("human"))
    }
}
