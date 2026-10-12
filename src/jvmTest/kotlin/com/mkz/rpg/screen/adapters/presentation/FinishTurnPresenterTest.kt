package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import org.junit.jupiter.api.Test

// Uses the real subdomain APIs, with a human player and a CPU player, and the real view through its page object.
class FinishTurnPresenterTest : ViewsForTesting() {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, battleUnitApi)
    private val timeline = battleSceneTimeline()
    private val playerCallToActionView = PlayerCallToActionView()

    private fun createPresenter() = FinishTurnPresenter(playerCallToActionView, battleApi, playerApi, timeline, eventBus)

    // Starts the battle with the human player turn.
    private fun startBattle() {
        playerApi.requestPlayerCreation("human", "Human", HUMAN)
        playerApi.requestPlayerCreation("cpu", "CPU", CPU)
        battleApi.startFirstRound(listOf("human", "cpu"))
        eventBus.dispatch()
    }

    private fun startTurnOf(playerId: String) {
        eventBus.publish(PlayerTurnStarted(playerId = playerId))
        eventBus.dispatch()
    }

    private fun currentPlayerTurn() = battleApi.searchBattle()?.currentPlayerTurn

    @Test
    fun `Init should display finish turn when is created`() =
        viewsTest {
            // Given
            // When
            createPresenter()
            // Then
            playerCallToActionView.isDisplayingFinishTurn().shouldBeTrue()
        }

    @Test
    fun `Init should disable finish turn when is created and no turn has started yet`() =
        viewsTest {
            // Given
            // When
            createPresenter()
            // Then
            playerCallToActionView.isFinishTurnEnabled().shouldBeFalse()
        }

    @Test
    fun `EnableFinishTurn should enable finish turn when a human player turn starts`() =
        viewsTest {
            // Given
            createPresenter()
            // When
            startBattle()
            // Then
            playerCallToActionView.isFinishTurnEnabled().shouldBeTrue()
        }

    @Test
    fun `EnableFinishTurn should disable finish turn when a cpu player turn starts`() =
        viewsTest {
            // Given
            createPresenter()
            startBattle()
            // When
            startTurnOf("cpu")
            // Then
            playerCallToActionView.isFinishTurnEnabled().shouldBeFalse()
        }

    @Test
    fun `EnableFinishTurn should disable finish turn while the timeline is playing during a human player turn`() =
        viewsTest {
            // Given
            createPresenter()
            startBattle()
            // When
            timeline.playLongStep()
            // Then
            playerCallToActionView.isFinishTurnEnabled().shouldBeFalse()
        }

    @Test
    fun `EnableFinishTurn should enable finish turn once the previous steps have been played when a human player turn starts`() =
        viewsTest {
            // Given
            createPresenter()
            val cpuMove = timeline.playLongStep()
            startBattle()
            playerCallToActionView.isFinishTurnEnabled().shouldBeFalse()
            // When
            cpuMove.complete(Unit)
            // Then
            playerCallToActionView.isFinishTurnEnabled().shouldBeTrue()
        }

    @Test
    fun `FinishTurn should finish the player turn when the player finishes the turn`() =
        viewsTest {
            // Given
            addChild(playerCallToActionView)
            createPresenter()
            startBattle()
            // When
            clickFinishTurn(playerCallToActionView)
            eventBus.dispatch()
            // Then
            currentPlayerTurn() shouldBe "cpu"
        }

    @Test
    fun `FinishTurn should disable finish turn when the player finishes the turn`() =
        viewsTest {
            // Given
            addChild(playerCallToActionView)
            createPresenter()
            startBattle()
            // When
            clickFinishTurn(playerCallToActionView)
            // Then
            playerCallToActionView.isFinishTurnEnabled().shouldBeFalse()
        }

    @Test
    fun `FinishTurn should not finish the player turn when the timeline is playing`() =
        viewsTest {
            // Given
            addChild(playerCallToActionView)
            createPresenter()
            startBattle()
            timeline.playLongStep()
            // When
            clickFinishTurn(playerCallToActionView)
            eventBus.dispatch()
            // Then
            currentPlayerTurn() shouldBe "human"
        }

    @Test
    fun `FinishTurn should not finish the player turn when the cpu player turn is displayed`() =
        viewsTest {
            // Given
            addChild(playerCallToActionView)
            createPresenter()
            startBattle()
            startTurnOf("cpu")
            // When
            clickFinishTurn(playerCallToActionView)
            eventBus.dispatch()
            // Then
            currentPlayerTurn() shouldBe "human"
        }

    @Test
    fun `PlayerWins should hide the call to action when a player wins`() =
        viewsTest {
            // Given
            createPresenter()
            eventBus.publish(PlayerVictory(playerId = "human"))
            // When
            eventBus.dispatch()
            // Then
            playerCallToActionView.isDisplayingFinishTurn().shouldBeFalse()
        }
}
