package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.GAME_WINDOW_SIZE
import korlibs.io.async.launchImmediately
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer

/** The first scene of the game, opening the battle or the map builder. */
class MenuScene : Scene() {
    // Like the battle scene, the scene coroutines run on the game window event loop, also in the headless tests.
    override val coroutineContext by lazy { super.coroutineContext + views.gameWindow.coroutineDispatcher }

    override suspend fun SContainer.sceneMain() {
        val menuView =
            MenuView(
                onNewGameSelected = { launchImmediately { sceneContainer.changeTo { BattleScene() } } },
                onMapBuilderSelected = { launchImmediately { sceneContainer.changeTo { MapBuilderScene() } } },
            )
        menuView.x = (GAME_WINDOW_SIZE.width - menuView.width) / 2
        menuView.y = (GAME_WINDOW_SIZE.height - menuView.height) / 2
        addChild(menuView)
    }
}
