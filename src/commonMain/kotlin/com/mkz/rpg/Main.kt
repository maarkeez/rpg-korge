package com.mkz.rpg

import com.mkz.rpg.screen.adapters.presentation.BattleScene
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.sceneContainer
import korlibs.korge.view.Stage
import korlibs.math.geom.Size

val GAME_WINDOW_SIZE = Size(390, 844)

suspend fun main() =
    Korge(windowSize = GAME_WINDOW_SIZE, backgroundColor = Colors["#2b2b2b"]) {
        startGame()
    }

// Shared by `main` and the end-to-end tests, so both boot the game the same way.
suspend fun Stage.startGame() {
    val sceneContainer = sceneContainer()

    sceneContainer.changeTo { BattleScene() }
}
