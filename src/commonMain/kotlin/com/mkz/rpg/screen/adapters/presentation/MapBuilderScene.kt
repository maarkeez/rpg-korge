package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.mapBuilder.adapters.presentation.MapBuilderApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import korlibs.io.async.launchImmediately
import korlibs.korge.scene.Scene
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.SContainer
import korlibs.korge.view.addUpdater
import korlibs.math.geom.Size

/** Designs a battlefield map, laid out like the battle: the title, the battlefield, the HUD and the calls to action. */
class MapBuilderScene : Scene() {
    // Like the battle scene, the scene coroutines run on the game window event loop, also in the headless tests.
    override val coroutineContext by lazy { super.coroutineContext + views.gameWindow.coroutineDispatcher }

    private val resourcesLoader = BattleSceneResourcesLoader()
    val battlefieldView = BattlefieldView(resourcesLoader)
    val infoView = MapBuilderInfoView()
    val hudView = MapBuilderHudView()
    val callToActionView = MapBuilderCallToActionView()

    override suspend fun SContainer.sceneInit() {
        resourcesLoader.load()
    }

    override suspend fun SContainer.sceneMain() {
        // Event bus
        val eventBus = InMemoryEventBus()
        addUpdater {
            eventBus.dispatch()
        }

        // Backend APIs: the map builder uses the terrains and the battlefield rules, but loads no battlefield
        val terrainApi = TerrainApi(eventBus)
        val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
        val mapBuilderApi = MapBuilderApi(terrainApi, battlefieldApi, eventBus)
        terrainApi.init()
        terrainApi.initialiseTerrains()

        MapBuilderPresenter(
            battlefieldView = battlefieldView,
            infoView = infoView,
            hudView = hudView,
            callToActionView = callToActionView,
            mapBuilderApi = mapBuilderApi,
            battlefieldApi = battlefieldApi,
            terrainApi = terrainApi,
            clipboard = GameWindowClipboard(views.gameWindow),
            coroutineScope = this@MapBuilderScene,
            onLeft = { launchImmediately { sceneContainer.changeTo { MenuScene() } } },
            eventBus = eventBus,
        )

        uiVerticalStack(padding = 2.0) {
            uiSpacing(Size(0, 10))
            addChild(infoView)
            addChild(battlefieldView)
            uiVerticalStack(padding = 5.0) {
                uiSpacing(Size(0, 10))
                addChild(hudView)
                uiSpacing(Size(0, 5))
                addChild(callToActionView)
            }
        }

        mapBuilderApi.createMap()
    }
}
