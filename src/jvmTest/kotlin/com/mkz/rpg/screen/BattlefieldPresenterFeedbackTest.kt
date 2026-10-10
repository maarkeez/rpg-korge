package com.mkz.rpg.screen

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.battlesetup.adapters.serialization.BattleScenarioLoader
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.screen.feedback.FeedbackBeat
import com.mkz.rpg.screen.feedback.FeedbackTiming
import com.mkz.rpg.screen.feedback.FxViews
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.adapters.events.RecordingEventBus
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import korlibs.korge.tests.ViewsForTesting
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

class BattlefieldPresenterFeedbackTest : ViewsForTesting() {
    private val eventBus = RecordingEventBus(InMemoryEventBus())
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
    private val performedBeats = mutableListOf<FeedbackBeat>()
    private val playbackChanges = mutableListOf<Boolean>()

    private val playerOneKnightId = "player-1-unit-1"
    private val skullAbilityIndex = 2

    @Nested
    inner class BeatOrder {
        @Test
        fun `should perform the beats in the order of the domain events when the cpu plays its turn with instant timing`() =
            viewsTest {
                // Given
                val script = setupBattleUiScript(feedbackTiming = FeedbackTiming.Instant).script
                performedBeats.clear()
                eventBus.clear()
                // When
                script.finishTurn()
                // Then
                val expectedSequence = eventBus.events.mapNotNull(::beatNameOf)
                assertThat(expectedSequence).contains("Move")
                assertThat(performedBeats.map { it::class.simpleName }).containsExactlyElementsOf(expectedSequence)
            }

        @Test
        fun `should leave no queued playback behind when the cpu plays its turn with instant timing`() =
            viewsTest {
                // Given
                val setup = setupBattleUiScript(feedbackTiming = FeedbackTiming.Instant)
                // When
                setup.script.finishTurn()
                // Then
                assertThat(setup.presenter.isPlayingFeedback).isFalse()
                assertThat(playbackChanges).isEmpty()
            }
    }

    @Nested
    inner class InputGating {
        @Test
        fun `should ignore tile taps when the cpu turn is still playing`() =
            viewsTest {
                // Given
                val setup = setupBattleUiScript(feedbackTiming = FeedbackTiming.Standard)
                setup.script.finishTurn()
                assertThat(setup.presenter.isPlayingFeedback).isTrue()
                val knightPosition = setup.script.positionOf(playerOneKnightId)
                // When
                setup.script.tapTile(row = knightPosition.row, column = knightPosition.column)
                // Then
                assertThat(battleUnitInfoView.visible).isFalse()
            }

        @Test
        fun `should accept tile taps again when the cpu turn has finished playing`() =
            viewsTest {
                // Given
                val setup = setupBattleUiScript(feedbackTiming = FeedbackTiming.Standard)
                setup.script.finishTurn()
                playUntilIdle(setup.presenter)
                val knightPosition = setup.script.positionOf(playerOneKnightId)
                // When
                setup.script.tapTile(row = knightPosition.row, column = knightPosition.column)
                // Then
                assertThat(battleUnitInfoView.visible).isTrue()
            }

        @Test
        fun `should announce the start and the end of playback when the cpu turn plays with real timing`() =
            viewsTest {
                // Given
                val setup = setupBattleUiScript(feedbackTiming = FeedbackTiming.Standard)
                playbackChanges.clear()
                // When
                setup.script.finishTurn()
                playUntilIdle(setup.presenter)
                // Then
                assertThat(playbackChanges).containsExactly(true, false)
            }
    }

    @Nested
    inner class Resync {
        @Test
        fun `should draw exactly the living battle units from the domain when the cpu turn has finished playing`() =
            viewsTest {
                // Given
                val setup = setupBattleUiScript(feedbackTiming = FeedbackTiming.Standard)
                playUntilIdle(setup.presenter)
                // When
                setup.script.finishTurn()
                playUntilIdle(setup.presenter)
                // Then
                val drawnUnitCount = battlefieldView.descendantsWith { it.name == BattlefieldView.BATTLE_UNIT }.size
                val expectedPositions =
                    listOf("player-one", "player-two")
                        .flatMap { battleUnitApi.searchBattleUnitsByPlayerId(it) }
                        .filter { it.remainingHealthPoints > 0 }
                        .map { battlefieldApi.searchPosition(it.id)!! }
                assertThat(drawnUnitCount).isEqualTo(expectedPositions.size)
                assertThat(expectedPositions.filter { battlefieldView.unitViewAt(it.row, it.column) == null }).isEmpty()
            }

        @Test
        fun `should hide the playback indicator and the walkers when the cpu turn has finished playing`() =
            viewsTest {
                // Given
                val setup = setupBattleUiScript(feedbackTiming = FeedbackTiming.Standard)
                // When
                setup.script.finishTurn()
                playUntilIdle(setup.presenter)
                // Then
                assertThat(battlefieldView.activeFxCount).isZero()
                assertThat(battlefieldView.descendantsWith { it.name == FxViews.WALKER_NAME }).isEmpty()
            }
    }

    @Nested
    inner class Walks {
        @Test
        fun `should hop one adjacent tile at a time when a unit moves several tiles`() =
            viewsTest {
                // Given
                val setup = setupBattleUiScript(feedbackTiming = FeedbackTiming.Standard)
                setup.script.selectUnit(playerOneKnightId)
                playUntilIdle(setup.presenter)
                performedBeats.clear()
                // When
                setup.script.tapTile(row = 6, column = 3)
                playUntilIdle(setup.presenter)
                // Then
                val hops = performedBeats.filterIsInstance<FeedbackBeat.Move>()
                assertThat(hops).hasSize(3)
                assertThat(hops.map { it.hopIndex }).containsExactly(0, 1, 2)
                hops.forEach { hop -> assertThat(abs(hop.fromRow - hop.toRow) + abs(hop.fromColumn - hop.toColumn)).isEqualTo(1) }
                assertThat(setup.script.occupantAt(row = 6, column = 3)).isEqualTo(playerOneKnightId)
            }
    }

    @Nested
    inner class Impacts {
        @Test
        fun `should flash the target and pop its damage when the player casts a damaging ability`() =
            viewsTest {
                // Given
                val setup = setupBattleUiScript(feedbackTiming = FeedbackTiming.Standard, scenarioPath = SHOWCASE_SCENARIO)
                setup.script.selectUnit(playerOneKnightId)
                setup.script.selectAbility(skullAbilityIndex)
                val target = setup.script.castTargets(battleUnitId = playerOneKnightId, abilityId = "skull").first()
                setup.script.tapTile(row = target.row, column = target.column)
                performedBeats.clear()
                // When
                setup.script.confirmCast()
                // Then
                val hit = performedBeats.filterIsInstance<FeedbackBeat.Hit>().first()
                assertThat(hit.amount).isPositive()
                assertThat(battlefieldView.descendantsWith { it.name == FxViews.FLASH_NAME }).isNotEmpty
                assertThat(battlefieldView.descendantsWith { it.name == FxViews.NUMBER_NAME }).isNotEmpty
                playUntilIdle(setup.presenter)
                assertThat(battlefieldView.activeFxCount).isZero()
            }
    }

    private fun playUntilIdle(presenter: BattlefieldPresenter) {
        var elapsedMs = 0
        while ((presenter.isPlayingFeedback || battlefieldView.activeFxCount > 0) && elapsedMs < MAX_PLAYBACK_MS) {
            presenter.updateFeedback(deltaMs = FRAME_MS)
            elapsedMs += FRAME_MS.toInt()
        }
        assertThat(presenter.isPlayingFeedback).isFalse()
    }

    private fun beatNameOf(event: DomainEvent): String? =
        when (event) {
            is BattleUnitEvent.BattleUnitMoved -> "Move"
            is BattleUnitEvent.BattleUnitDamaged -> "Hit"
            is BattleUnitEvent.BattleUnitHealed -> "Heal"
            is BattleUnitEvent.BattleUnitDeployed -> "Deployed"
            is BattlefieldEvent.OccupantRemoved -> "OccupantRemoved"
            is BattleUnitEvent.EffectReceived -> "StatusApplied"
            is com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted -> "TurnStarted"
            else -> null
        }

    private data class Setup(
        val script: BattleUiScript,
        val presenter: BattlefieldPresenter,
    )

    private suspend fun Stage.setupBattleUiScript(
        feedbackTiming: FeedbackTiming,
        scenarioPath: String? = null,
    ): Setup {
        battlefieldView.loadAssets()
        battleUnitInfoView.loadAssets()
        attackPreviewView.loadAssets()

        val presenter =
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
                feedbackTiming = feedbackTiming,
                onBeatPerformed = { beat -> performedBeats += beat },
            )
        presenter.onPlaybackChanged = { playing -> playbackChanges += playing }
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
        battleSetupApi.setupBattle(scenarioPath?.let { BattleScenarioLoader().load(it) })
        eventBus.dispatch()
        playUntilIdle(presenter)

        val script =
            BattleUiScript(
                click = { view -> view.simulateClick() },
                battlefieldView = battlefieldView,
                battleUnitInfoView = battleUnitInfoView,
                playerCallToActionView = playerCallToActionView,
                battlefieldApi = battlefieldApi,
                battleUnitApi = battleUnitApi,
                battleApi = battleApi,
                eventBus = eventBus,
            )
        return Setup(script, presenter)
    }

    private companion object {
        const val FRAME_MS = 16.0
        const val MAX_PLAYBACK_MS = 20_000
    }
}
