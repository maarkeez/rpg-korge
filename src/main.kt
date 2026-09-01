import ability.adapters.presentation.AbilityApi
import battle.adapters.presentation.BattleApi
import screen.BattleInfoPresenter
import screen.BattleInfoView
import screen.FinishTurnPresenter
import screen.FinishTurnView
import battlefield.adapters.presentation.BattlefieldApi
import screen.BattlefieldPresenter
import screen.BattlefieldView
import battlesetup.adapters.presentation.BattleSetupApi
import battleunit.adapters.presentation.BattleUnitApi
import cpuBrain.adapters.presentation.CpuBrainApi
import effect.adapters.presentation.EffectApi
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.image.color.*
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.math.geom.*
import player.adapters.presentation.PlayerApi
import screen.AttackPreviewView
import screen.BattleUnitInfoView
import screen.BattleHudView
import shared.domain.EventBus
import unit.adapters.presentation.UnitApi

suspend fun main() = Korge(windowSize = Size(390, 844), backgroundColor = Colors["#2b2b2b"]) {
	val sceneContainer = sceneContainer()

	sceneContainer.changeTo { MyScene() }
}

class MyScene : Scene() {

    val battlefieldView = BattlefieldView()
    val battleUnitInfoView = BattleUnitInfoView()
    val attackPreviewView = AttackPreviewView()
    val battleHudView = BattleHudView(
        Size(390, 300),
        battleUnitInfoView,
        attackPreviewView
    )

    override suspend fun SContainer.sceneInit() {
        battlefieldView.loadAssets()
        battleUnitInfoView.loadAssets()
        attackPreviewView.loadAssets()
    }

	override suspend fun SContainer.sceneMain() {

        // Event bus
        val eventBus = EventBus()
        addUpdater {
            eventBus.dispatch()
        }

        // Backend APIs
        val unitApi = UnitApi(eventBus)
        val playerApi = PlayerApi(eventBus)
        val battlefieldApi = BattlefieldApi(eventBus)
        val effectApi = EffectApi(eventBus)
        val abilityApi = AbilityApi(effectApi, eventBus)
        val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
        val battleApi = BattleApi(eventBus, battleUnitApi)
        val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battleApi, battlefieldApi, eventBus)
        val battleSetupApi = BattleSetupApi(
            playerApi,
            battleApi,
            effectApi,
            abilityApi,
            unitApi,
            battleUnitApi,
            battlefieldApi,
        )

        // Main scene
        val battlefieldPresenter = BattlefieldPresenter(
            battlefieldView,
            battleUnitInfoView,
            attackPreviewView,
            battleHudView,
            battlefieldApi,
            battleUnitApi,
            playerApi,
            unitApi,
            abilityApi,
            battleApi,
            eventBus
        )

        uiVerticalStack(padding = 2.0) {
            uiSpacing(Size(0, 10))
            // Battle info
            val battleInfoView = BattleInfoView(this)
            val battleInfoPresenter = BattleInfoPresenter(
                battleInfoView,
                battleApi,
                playerApi,
                eventBus,
            )
            // Battlefield
            addChild(battlefieldView)

            uiVerticalStack(padding = 5.0) {
                uiSpacing(Size(0, 10))
                addChild(battleHudView)
                uiSpacing(Size(0, 5))
                val finishTurnView = FinishTurnView()
                addChild(finishTurnView)
                val finishTurnPresenter = FinishTurnPresenter(finishTurnView, battleApi, eventBus)
            }
        }

        // Start game
        battleSetupApi.setupBattle()
	}
}
