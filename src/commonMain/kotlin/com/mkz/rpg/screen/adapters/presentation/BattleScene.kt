package com.mkz.rpg.screen.adapters.presentation

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
import korlibs.korge.scene.Scene
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.SContainer
import korlibs.korge.view.addUpdater
import korlibs.math.geom.Size

class BattleScene : Scene() {
    private val resourcesLoader = BattleSceneResourcesLoader()
    val battlefieldView = BattlefieldView(resourcesLoader)
    val battleUnitInfoView = BattleUnitInfoView(resourcesLoader)
    val attackPreviewView = AttackPreviewView(resourcesLoader)
    val battleHudView =
        BattleHudView(
            Size(390, 300),
            battleUnitInfoView,
            attackPreviewView,
        )
    val playerCallToActionView = PlayerCallToActionView()

    override suspend fun SContainer.sceneInit() {
        resourcesLoader.load()
    }

    override suspend fun SContainer.sceneMain() {
        // Event bus
        val eventBus = InMemoryEventBus()
        addUpdater {
            eventBus.dispatch()
        }

        // Backend APIs
        val terrainApi = TerrainApi(eventBus)
        val unitApi = UnitApi(eventBus)
        val playerApi = PlayerApi(eventBus)
        val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
        val effectApi = EffectApi(eventBus)
        val abilityApi = AbilityApi(effectApi, eventBus)
        val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
        val battleApi = BattleApi(eventBus, battleUnitApi)
        val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus)
        val battleSetupApi = BattleSetupApi(eventBus)

        // Resources: each subdomain loads its data before the battle is set up
        terrainApi.init()
        effectApi.init()
        abilityApi.init()
        unitApi.init()
        playerApi.init()
        battlefieldApi.init()
        battleUnitApi.init()
        battleApi.init()

        // Main scene: the presenters replay the battle events in order on the timeline
        val timeline = BattleSceneTimeline(this@BattleScene)
        val battlefieldPresenter =
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
                timeline,
                eventBus,
            )

        uiVerticalStack(padding = 2.0) {
            uiSpacing(Size(0, 10))
            // Battle info
            val battleInfoView = BattleInfoView()
            addChild(battleInfoView)
            val battleInfoPresenter =
                BattleInfoPresenter(
                    battleInfoView,
                    battleApi,
                    playerApi,
                    timeline,
                    eventBus,
                )
            // Battlefield
            addChild(battlefieldView)

            uiVerticalStack(padding = 5.0) {
                uiSpacing(Size(0, 10))
                addChild(battleHudView)
                uiSpacing(Size(0, 5))
                // val finishTurnView = FinishTurnView()
                // addChild(finishTurnView)
                // val finishTurnPresenter = FinishTurnPresenter(finishTurnView, battleApi, eventBus)
                addChild(playerCallToActionView)
                FinishTurnPresenter(playerCallToActionView, battleApi, timeline, eventBus)
            }
        }

        // Start game
        battleSetupApi.setupBattle()
    }
}
