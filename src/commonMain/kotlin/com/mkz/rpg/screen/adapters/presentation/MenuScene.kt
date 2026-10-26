package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import korlibs.io.async.launchImmediately
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import kotlin.random.Random

/**
 * The first scene of the game, opening the battle or the map builder. Localization: the texts of the selected language,
 * kept by every scene. Random: the rolls of the CPU battle brain in the battle.
 */
class MenuScene(
    private val localizationApi: LocalizationApi,
    private val random: Random = Random.Default,
) : Scene() {
    // Like the battle scene, the scene coroutines run on the game window event loop, also in the headless tests.
    override val coroutineContext by lazy { super.coroutineContext + views.gameWindow.coroutineDispatcher }

    override suspend fun SContainer.sceneMain() {
        val menuView =
            MenuView(
                texts = Texts(localizationApi),
                onNewGameSelected = { launchImmediately { sceneContainer.changeTo { BattleScene(localizationApi, random) } } },
                onMapBuilderSelected = { launchImmediately { sceneContainer.changeTo { MapBuilderScene(localizationApi) } } },
            )
        menuView.x = (GAME_WINDOW_SIZE.width - menuView.width) / 2
        menuView.y = (GAME_WINDOW_SIZE.height - menuView.height) / 2
        addChild(menuView)
    }
}
