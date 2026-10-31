package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import com.mkz.rpg.mapBuilder.adapters.presentation.MapBuilderApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.terrain.adapters.resources.ResourcesTerrainLoader
import korlibs.io.async.launchImmediately
import korlibs.korge.scene.Scene
import korlibs.korge.service.storage.IStorage
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.SContainer
import korlibs.korge.view.addUpdater
import korlibs.math.geom.Size

/**
 * Designs a battlefield map, laid out like the battle: the title, the battlefield, the HUD and the calls to action.
 * Storage: where the games are saved, handed back to the menu.
 */
class MapBuilderScene(
    private val localizationApi: LocalizationApi,
    private val storage: IStorage? = null,
) : Scene() {
    private companion object {
        const val MENU_MARGIN = 6.0
    }

    // Like the battle scene, the scene coroutines run on the game window event loop, also in the headless tests.
    override val coroutineContext by lazy { super.coroutineContext + views.gameWindow.coroutineDispatcher }

    private val texts = Texts(localizationApi)
    private val resourcesLoader = BattleSceneResourcesLoader()
    val battlefieldView = BattlefieldView(resourcesLoader)
    val infoView = MapBuilderInfoView(texts)
    val hudView = MapBuilderHudView(texts)
    val callToActionView = MapBuilderCallToActionView(texts)
    val menuView = MapBuilderMenuView()

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
        val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
        val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
        val mapBuilderApi = MapBuilderApi(terrainApi, battlefieldApi, eventBus)
        terrainApi.init()
        terrainApi.initialiseTerrains()

        MapBuilderPresenter(
            battlefieldView = battlefieldView,
            infoView = infoView,
            hudView = hudView,
            callToActionView = callToActionView,
            menuView = menuView,
            mapBuilderApi = mapBuilderApi,
            battlefieldApi = battlefieldApi,
            terrainApi = terrainApi,
            texts = texts,
            clipboard = GameWindowClipboard(views.gameWindow),
            mapFilePicker = GameWindowMapFilePicker(views.gameWindow, texts),
            coroutineScope = this@MapBuilderScene,
            onLeft = { launchImmediately { sceneContainer.changeTo { MenuScene(localizationApi, storage) } } },
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

        // At the top right corner, over the rest of the scene, so the open menu covers it.
        menuView.x = GAME_WINDOW_SIZE.width - MapBuilderMenuView.BUTTON_WIDTH - MENU_MARGIN
        menuView.y = MENU_MARGIN
        addChild(menuView)

        mapBuilderApi.createMap()
    }
}
