package com.mkz.rpg.screen

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Stage
import korlibs.korge.view.descendantsWith
import korlibs.math.geom.Size
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.random.Random

class BattleUiScriptTest : ViewsForTesting() {
    private val eventBus = InMemoryEventBus()
    private val random = Random(seed = 42L)
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, random)
    private val battleApi = BattleApi(eventBus, battleUnitApi)
    private val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, eventBus, random)
    private val battleSetupApi = BattleSetupApi(eventBus)

    private val battlefieldView = BattlefieldView()
    private val battleUnitInfoView = BattleUnitInfoView()
    private val attackPreviewView = AttackPreviewView()
    private val battleHudView =
        BattleHudView(
            size = Size(390, 300),
            battleUnitInfoView = battleUnitInfoView,
            attackPreviewView = attackPreviewView,
        )
    private val playerCallToActionView = PlayerCallToActionView()

    private val playerOneId = "player-one"
    private val playerOneKnightId = "player-1-unit-1"
    private val playerTwoRatId = "player-2-unit-1"

    @Nested
    inner class UnitSelection {
        @Test
        fun `should display unit info and movement range when player taps a unit tile`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                // When
                script.selectUnit(playerOneKnightId)
                // Then
                assertThat(battleUnitInfoView.visible).isTrue()
                assertThat(battleHudView.children).contains(battleUnitInfoView)
                assertThat(selectedTileCount()).isGreaterThan(1)
                val position = script.positionOf(playerOneKnightId)
                assertThat(position.row).isEqualTo(6)
                assertThat(position.column).isEqualTo(6)
            }

        @Test
        fun `should move a unit when player taps the destination tile`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.selectUnit(playerOneKnightId)
                // When
                script.tapTile(row = 5, column = 6)
                // Then
                assertThat(script.occupantAt(row = 6, column = 6)).isNull()
                assertThat(script.occupantAt(row = 5, column = 6)).isEqualTo(playerOneKnightId)
            }
    }

    @Nested
    inner class AbilityCast {
        @Test
        fun `should cast an ability when player taps an ability button and a target tile and confirms`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.selectUnit(playerOneKnightId)
                script.selectAbility(TELEPORT_ABILITY_INDEX)
                val target =
                    script
                        .castTargets(battleUnitId = playerOneKnightId, abilityId = "teleport")
                        .first()
                // When
                script.tapTile(row = target.row, column = target.column)
                // Then
                assertThat(script.isConfirmAndCancelDisplayed()).isTrue()
                // When
                script.confirmCast()
                // Then
                val newPosition = script.positionOf(playerOneKnightId)
                assertThat(newPosition.row).isEqualTo(target.row)
                assertThat(newPosition.column).isEqualTo(target.column)
                assertThat(script.occupantAt(row = target.row, column = target.column)).isEqualTo(playerOneKnightId)
            }

        @Test
        fun `should not cast an ability when player cancels the cast preview`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.selectUnit(playerOneKnightId)
                script.selectAbility(TELEPORT_ABILITY_INDEX)
                val target =
                    script
                        .castTargets(battleUnitId = playerOneKnightId, abilityId = "teleport")
                        .first()
                script.tapTile(row = target.row, column = target.column)
                // When
                script.cancelCast()
                // Then
                val position = script.positionOf(playerOneKnightId)
                assertThat(position.row).isEqualTo(6)
                assertThat(position.column).isEqualTo(6)
                assertThat(finishTurnButtonVisible()).isTrue()
            }
    }

    @Nested
    inner class TurnFlow {
        @Test
        fun `should pass turn and let the cpu play when finish button is tapped`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                val ratStart = script.positionOf(playerTwoRatId)
                // When
                script.finishTurn()
                // Then
                assertThat(script.currentPlayerTurn()).isEqualTo(playerOneId)
                val ratEnd = script.positionOf(playerTwoRatId)
                assertThat(ratEnd.row != ratStart.row || ratEnd.column != ratStart.column).isTrue()
            }
    }

    private suspend fun Stage.setupBattleUiScript(): BattleUiScript {
        battlefieldView.loadAssets()
        battleUnitInfoView.loadAssets()
        attackPreviewView.loadAssets()

        BattlefieldPresenter(
            battlefieldView,
            battleUnitInfoView,
            attackPreviewView,
            playerCallToActionView,
            battleHudView,
            battlefieldApi,
            battleUnitApi,
            playerApi,
            unitApi,
            abilityApi,
            battleApi,
            eventBus,
        )
        FinishTurnPresenter(playerCallToActionView, battleApi, eventBus)

        uiVerticalStack(padding = 2.0) {
            uiSpacing(Size(0, 10))
            addChild(battlefieldView)
            uiSpacing(Size(0, 10))
            addChild(battleHudView)
            uiSpacing(Size(0, 5))
            addChild(playerCallToActionView)
        }

        terrainApi.init()
        battleSetupApi.setupBattle()
        eventBus.dispatch()

        return BattleUiScript(
            click = { view -> view.simulateClick() },
            battlefieldView = battlefieldView,
            battleUnitInfoView = battleUnitInfoView,
            playerCallToActionView = playerCallToActionView,
            battlefieldApi = battlefieldApi,
            battleUnitApi = battleUnitApi,
            battleApi = battleApi,
            eventBus = eventBus,
        )
    }

    private fun selectedTileCount(): Int =
        battlefieldView
            .descendantsWith { it.name?.startsWith("row-") ?: false }
            .count { it.findViewByName(BattlefieldView.SELECTION) != null }

    private fun finishTurnButtonVisible(): Boolean =
        playerCallToActionView
            .findViewByName("call-to-action")
            ?.descendantsWith { it is UIButton }
            ?.filterIsInstance<UIButton>()
            ?.any { it.text == "Finish turn" }
            ?: false

    companion object {
        private const val TELEPORT_ABILITY_INDEX = 3
    }
}
