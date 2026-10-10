package com.mkz.rpg.screen

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.battlesetup.adapters.serialization.BattleScenarioLoader
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.feedback.FeedbackTiming
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import korlibs.korge.view.SolidRect
import korlibs.korge.view.addUpdater
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size
import kotlin.random.Random
import kotlin.time.DurationUnit

class BattleScene(
    val seed: Long? = null,
    val scenarioPath: String? = null,
    val debugEnabled: Boolean = false,
    private val feedbackTiming: FeedbackTiming = FeedbackTiming.Standard,
) : Scene() {
    private val sprites = SpriteRegistry()
    val battlefieldView =
        BattlefieldView(
            sprites,
            viewportSize = Size(BattleLayout.SCREEN_WIDTH, BattleLayout.BATTLEFIELD_HEIGHT),
            idleFrameMs = feedbackTiming.idleFrameMs,
        )
    val battleUnitInfoView = BattleUnitInfoView(sprites)
    val attackPreviewView = AttackPreviewView(sprites)
    private val sheetPanel = SolidRect(Size(BattleLayout.SCREEN_WIDTH, BattleLayout.SHEET_HEIGHT), UiPalette.panel)
    val battleHudView =
        BattleHudView(
            Size(BattleLayout.SCREEN_WIDTH, BattleLayout.SHEET_HEIGHT),
            battleUnitInfoView,
            attackPreviewView,
            sheetPanel,
        )
    val playerCallToActionView = PlayerCallToActionView()
    private var battlefieldPresenter: BattlefieldPresenter? = null

    /** True while feedback animations or queued playback are still running. Tests wait on this before a snapshot. */
    val isFeedbackPlaying: Boolean
        get() = battlefieldPresenter?.isPlayingFeedback == true || battlefieldView.activeFxCount > 0

    override suspend fun SContainer.sceneInit() {
        battlefieldView.loadAssets()
        battleUnitInfoView.loadAssets()
        attackPreviewView.loadAssets()
    }

    override suspend fun SContainer.sceneMain() {
        val scenario = scenarioPath?.let { BattleScenarioLoader().load(it) }
        val effectiveSeed = seed ?: scenario?.toDto()?.seed
        val random = effectiveSeed?.let { Random(it) } ?: Random(System.nanoTime())

        // Event bus
        val eventBus = InMemoryEventBus()
        addUpdater {
            eventBus.dispatch()
        }

        // Backend APIs
        val terrainApi = TerrainApi(eventBus)
        terrainApi.init()
        val unitApi = UnitApi(eventBus)
        val playerApi = PlayerApi(eventBus)
        val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
        val effectApi = EffectApi(eventBus)
        val abilityApi = AbilityApi(effectApi, eventBus)
        val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, random)
        val battleApi = BattleApi(eventBus, battleUnitApi)
        val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, eventBus, random)
        val battleSetupApi = BattleSetupApi(eventBus)

        // Main scene
        val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
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
                battlefieldHudRepository,
                terrainApi.searchTerrainById,
                effectApi.searchEffectById,
                feedbackTiming,
            )
        battlefieldPresenter = presenter
        addUpdater { dt -> presenter.updateFeedback(dt.toDouble(DurationUnit.MILLISECONDS)) }

        // Battlefield fills the screen. The sheet and the action bar overlay it, and the top strip sits above it.
        addChild(battlefieldView)
        battlefieldView.y = BattleLayout.BATTLEFIELD_Y.toDouble()

        sheetPanel.y = BattleLayout.SHEET_Y.toDouble()
        addChild(sheetPanel)
        addChild(battleHudView)
        battleHudView.y = BattleLayout.SHEET_Y.toDouble()

        addChild(playerCallToActionView)
        playerCallToActionView.y = BattleLayout.ACTION_BAR_Y.toDouble()
        FinishTurnPresenter(playerCallToActionView, battleApi, eventBus)

        solidRect(BattleLayout.SCREEN_WIDTH, BattleLayout.BATTLEFIELD_Y, UiPalette.panel)
        val battleInfoView = BattleInfoView()
        battleInfoView.x = BattleLayout.TOP_STRIP_PADDING.toDouble()
        battleInfoView.y = (BattleLayout.SAFE_AREA_TOP + (BattleLayout.TOP_STRIP_HEIGHT - battleInfoView.height) / 2)
        addChild(battleInfoView)
        BattleInfoPresenter(
            battleInfoView,
            battleApi,
            playerApi,
            eventBus,
        )
        presenter.onPlaybackChanged = { playing -> battleInfoView.displayPlayback(playing) }

        // Start game
        battleSetupApi.setupBattle(scenario)

        installDebugSupport(
            battleApi = battleApi,
            playerApi = playerApi,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            effectiveSeed = effectiveSeed,
        )
    }
}
