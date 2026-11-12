package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.telemetry.adapters.presentation.TelemetryApi
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.rpg.worldBuilder.adapters.presentation.WorldBuilderApi
import korlibs.io.async.launchImmediately
import korlibs.korge.scene.Scene
import korlibs.korge.service.storage.IStorage
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.SContainer
import korlibs.korge.view.addUpdater
import korlibs.math.geom.Size

class WorldBuilderScene(
    private val localizationApi: LocalizationApi,
    private val telemetryApi: TelemetryApi,
    private val storage: IStorage? = null,
) : Scene() {
    private companion object {
        const val MENU_MARGIN = 6.0
    }

    override val coroutineContext by lazy { super.coroutineContext + views.gameWindow.coroutineDispatcher }

    private val messages = Messages(localizationApi)
    private val resourcesLoader = GameSceneResourcesLoader()
    val battlefieldView = BattlefieldView(resourcesLoader)
    val infoView = WorldBuilderInfoView(messages)
    val hudView = WorldBuilderHudView(messages)
    val callToActionView = WorldBuilderCallToActionView(messages)
    val menuView = WorldBuilderMenuView()

    override suspend fun SContainer.sceneInit() {
        resourcesLoader.load()
    }

    override suspend fun SContainer.sceneMain() {
        val eventBus = InMemoryEventBus()
        addUpdater {
            eventBus.dispatch()
        }

        val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
        val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
        val worldBuilderApi = WorldBuilderApi(terrainApi, battlefieldApi, eventBus)
        terrainApi.init()
        terrainApi.initialiseTerrains()

        WorldBuilderPresenter(
            battlefieldView = battlefieldView,
            infoView = infoView,
            hudView = hudView,
            callToActionView = callToActionView,
            menuView = menuView,
            worldBuilderApi = worldBuilderApi,
            battlefieldApi = battlefieldApi,
            terrainApi = terrainApi,
            messages = messages,
            clipboard = GameWindowClipboard(views.gameWindow),
            worldFilePicker = GameWindowWorldFilePicker(views.gameWindow, messages),
            coroutineScope = this@WorldBuilderScene,
            onLeft = { launchImmediately { sceneContainer.changeTo { MenuScene(localizationApi, telemetryApi, storage) } } },
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

        menuView.x = GAME_WINDOW_SIZE.width - WorldBuilderMenuView.BUTTON_WIDTH - MENU_MARGIN
        menuView.y = MENU_MARGIN
        addChild(menuView)

        worldBuilderApi.createWorld()
    }
}
