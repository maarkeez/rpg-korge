package com.mkz.rpg.screen

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability
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
import kotlin.math.abs
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
    inner class MovementRange {
        @Test
        fun `should highlight exactly the tiles returned by whereCanMove when player selects a knight`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                // When
                script.selectUnit(playerOneKnightId)
                // Then
                assertThat(movementTiles(MovementStyle.ALLY)).isEqualTo(script.reachableTiles(playerOneKnightId))
                assertThat(movementTiles(MovementStyle.INSPECT)).isEmpty()
            }

        @Test
        fun `should highlight the movement range with the inspect style when player taps an enemy unit`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                // When
                script.selectUnit(playerTwoRatId)
                // Then
                assertThat(movementTiles(MovementStyle.INSPECT)).isEqualTo(script.reachableTiles(playerTwoRatId))
                assertThat(movementTiles(MovementStyle.ALLY)).isEmpty()
            }

        @Test
        fun `should keep the unit selected and refresh the range with the remaining steps when it moves`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.selectUnit(playerOneKnightId)
                val rangeBeforeMoving = movementTiles(MovementStyle.ALLY)
                // When
                script.tapTile(row = 5, column = 6)
                // Then
                val rangeAfterMoving = movementTiles(MovementStyle.ALLY)
                assertThat(rangeAfterMoving).isEqualTo(script.reachableTiles(playerOneKnightId))
                assertThat(rangeAfterMoving.size).isLessThan(rangeBeforeMoving.size)
                assertThat(battleUnitInfoView.visible).isTrue()
            }

        @Test
        fun `should show no movement range when the selected knight has no steps left`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.selectUnit(playerOneKnightId)
                val farthestTile = script.reachableTiles(playerOneKnightId).maxBy { (row, column) -> abs(row - 6) + abs(column - 6) }
                script.tapTile(row = farthestTile.first, column = farthestTile.second)
                // When
                val tilesLeft = movementTiles(MovementStyle.ALLY)
                // Then
                assertThat(script.battleUnit(playerOneKnightId).remainingTurnActions.remainingSteps).isZero()
                assertThat(tilesLeft).isEmpty()
            }
    }

    private fun movementTiles(style: MovementStyle): Set<Pair<Int, Int>> =
        battlefieldView
            .descendantsWith { it.name?.startsWith("row-") ?: false }
            .filter { tile -> tile.descendantsWith { it is MovementRangeTileView && it.style == style }.isNotEmpty() }
            .map { tile ->
                val (row, column) = Regex("row-(\\d+)-column-(\\d+)").matchEntire(tile.name!!)!!.destructured
                row.toInt() to column.toInt()
            }.toSet()

    @Nested
    inner class EnemyInspection {
        @Test
        fun `should show inspect info without ability bar when player taps an enemy unit`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                // When
                script.selectUnit(playerTwoRatId)
                // Then
                assertThat(battleUnitInfoView.visible).isTrue()
                assertThat(battleUnitInfoView.visibleAbilityButtonCount).isZero()
                assertThat(battleUnitInfoView.readOnlyAbilityIconCount).isGreaterThan(0)
            }

        @Test
        fun `should show the ability bar again when player taps an ally after inspecting an enemy`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.selectUnit(playerTwoRatId)
                // When
                script.selectUnit(playerOneKnightId)
                // Then
                assertThat(battleUnitInfoView.visibleAbilityButtonCount).isGreaterThan(0)
                assertThat(battleUnitInfoView.readOnlyAbilityIconCount).isZero()
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
    inner class AbilityAvailability {
        @Test
        fun `should show the heal slot on cooldown with the turns left when the knight reselected after healing`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.castHealWithKnight()
                script.finishTurn()
                // When
                script.selectUnit(playerOneKnightId)
                // Then
                val healSlot = battleUnitInfoView.abilitySlots[HEAL_ABILITY_INDEX]
                assertThat(healSlot.status).isEqualTo(SearchAbilityAvailability.AbilityAvailability.Status.COOLDOWN)
                val number = healSlot.findViewByName(AbilityButtonView.ABILITY_COOLDOWN_NUMBER) as PixelGlyphs.PixelNumberView
                assertThat(number.value).isEqualTo(1)
            }

        @Test
        fun `should explain the cooldown and not enter the cast range when player taps the heal slot on cooldown`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.castHealWithKnight()
                script.finishTurn()
                script.selectUnit(playerOneKnightId)
                // When
                script.selectAbility(HEAL_ABILITY_INDEX)
                // Then
                assertThat(battleUnitInfoView.abilityLineText).isEqualTo("On cooldown (1)")
                assertThat(battleUnitInfoView.abilitySlots[HEAL_ABILITY_INDEX].findViewByName(AbilityButtonView.ABILITY_SELECTION)).isNull()
                assertThat(script.isConfirmAndCancelDisplayed()).isFalse()
            }

        @Test
        fun `should lock every slot and explain it when player taps an ability after the knight already cast`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.castHealWithKnight()
                script.selectUnit(playerOneKnightId)
                // When
                script.selectAbility(TELEPORT_ABILITY_INDEX)
                // Then
                val statuses = battleUnitInfoView.abilitySlots.map { it.status }
                assertThat(statuses).containsOnly(SearchAbilityAvailability.AbilityAvailability.Status.NO_CASTS_LEFT)
                assertThat(battleUnitInfoView.abilityLineText).isEqualTo("Already acted")
            }

        @Test
        fun `should show the selected ability name cost and summary when player selects a ready ability`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.selectUnit(playerOneKnightId)
                // When
                script.selectAbility(HEAL_ABILITY_INDEX)
                // Then
                assertThat(battleUnitInfoView.abilityLineText).isEqualTo("Heal - 10 MP - +20 HP")
            }

        @Test
        fun `should agree with can cast ability for every slot when the knight is ready and after it healed`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.selectUnit(playerOneKnightId)
                val readyStatuses = battleUnitInfoView.abilitySlots.map { it.status == SearchAbilityAvailability.AbilityAvailability.Status.READY }
                val readyExpected =
                    script
                        .battleUnit(playerOneKnightId)
                        .abilityCooldowns.keys
                        .map { script.canCast(playerOneKnightId, it) }
                script.castHealWithKnight(alreadySelected = true)
                script.finishTurn()
                script.selectUnit(playerOneKnightId)
                // When
                val afterStatuses = battleUnitInfoView.abilitySlots.map { it.status == SearchAbilityAvailability.AbilityAvailability.Status.READY }
                val afterExpected =
                    script
                        .battleUnit(playerOneKnightId)
                        .abilityCooldowns.keys
                        .map { script.canCast(playerOneKnightId, it) }
                // Then
                assertThat(readyStatuses).isEqualTo(readyExpected)
                assertThat(afterStatuses).isEqualTo(afterExpected)
                assertThat(readyStatuses).containsExactly(true, true, true, true, true, true)
                assertThat(afterStatuses).containsExactly(true, true, true, true, true, false)
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

    @Nested
    inner class CastTargetHighlighting {
        @Test
        fun `should highlight exactly the tiles where the ability can be cast when player selects an ability`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.selectUnit(playerOneKnightId)
                // When
                script.selectAbility(TELEPORT_ABILITY_INDEX)
                // Then
                val expected = script.castTargets(battleUnitId = playerOneKnightId, abilityId = "teleport").map { it.row to it.column }.toSet()
                assertThat(script.highlightedCastTiles().keys).isEqualTo(expected)
                assertThat(script.highlightedCastTiles().values).containsOnly(CastTargetKind.TARGET_TILE)
            }

        @Test
        fun `should dim every tile that is not a valid cast tile when player selects an ability`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.selectUnit(playerOneKnightId)
                // When
                script.selectAbility(TELEPORT_ABILITY_INDEX)
                // Then
                val validCount = script.highlightedCastTiles().size
                assertThat(script.dimmedTileCount()).isEqualTo(BATTLEFIELD_TILE_COUNT - validCount)
            }

        @Test
        fun `should explain there is no valid target and keep the ability selected when nothing is in reach`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.selectUnit(playerOneKnightId)
                // When
                script.selectAbility(0)
                // Then
                assertThat(script.castTargets(battleUnitId = playerOneKnightId, abilityId = "poisoned-sword")).isEmpty()
                assertThat(battleUnitInfoView.abilityLineText).isEqualTo("No valid target in reach")
                assertThat(battleUnitInfoView.abilitySlots[0].findViewByName(AbilityButtonView.ABILITY_SELECTION)).isNotNull
            }

        @Test
        fun `should return to the movement range and keep the unit selected when player taps an invalid tile while targeting`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript()
                script.selectUnit(playerOneKnightId)
                script.selectAbility(TELEPORT_ABILITY_INDEX)
                val validTiles = script.highlightedCastTiles().keys
                val invalidTile =
                    (0 until BATTLEFIELD_SIZE)
                        .flatMap { row -> (0 until BATTLEFIELD_SIZE).map { row to it } }
                        .first { it !in validTiles && script.occupantAt(it.first, it.second) == null }
                // When
                script.tapTile(row = invalidTile.first, column = invalidTile.second)
                // Then
                assertThat(script.highlightedCastTiles()).isEmpty()
                assertThat(script.dimmedTileCount()).isZero()
                assertThat(script.reachableTiles(playerOneKnightId)).isNotEmpty
                assertThat(battleUnitInfoView.visibleAbilityButtonCount).isGreaterThan(0)
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
            searchTerrainById = terrainApi.searchTerrainById,
            searchEffectById = effectApi.searchEffectById,
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

    private suspend fun BattleUiScript.castHealWithKnight(alreadySelected: Boolean = false) {
        if (!alreadySelected) selectUnit(playerOneKnightId)
        selectAbility(HEAL_ABILITY_INDEX)
        val target = castTargets(battleUnitId = playerOneKnightId, abilityId = "heal").first()
        tapTile(row = target.row, column = target.column)
        confirmCast()
    }

    private fun BattleUiScript.canCast(
        battleUnitId: String,
        abilityId: String,
    ) = battleUnitApi.canCastAbility(battleUnitId, abilityId)

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
        private const val HEAL_ABILITY_INDEX = 5
        private const val BATTLEFIELD_SIZE = 16
        private const val BATTLEFIELD_TILE_COUNT = BATTLEFIELD_SIZE * BATTLEFIELD_SIZE
    }
}
