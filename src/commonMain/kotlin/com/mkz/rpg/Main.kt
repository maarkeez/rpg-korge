package com.mkz.rpg

import com.mkz.rpg.screen.adapters.presentation.MenuScene
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.sceneContainer
import korlibs.korge.view.Stage
import korlibs.math.geom.Size
import kotlin.random.Random

val GAME_WINDOW_SIZE = Size(390, 844)

suspend fun main() =
    Korge(windowSize = GAME_WINDOW_SIZE, backgroundColor = Colors["#2b2b2b"]) {
        startGame()
    }

// Shared by `main` and the end-to-end tests, so both boot the game the same way. The tests choose the rolls of the CPU brain.
suspend fun Stage.startGame(random: Random = Random.Default) {
    val sceneContainer = sceneContainer()

    sceneContainer.changeTo { MenuScene(random) }
}
