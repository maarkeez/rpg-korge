package com.mkz.rpg

import com.mkz.rpg.screen.adapters.presentation.MenuScene
import com.mkz.rpg.shared.adapters.presentation.WholePixelScale
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.KorgeDisplayMode
import korlibs.korge.scene.sceneContainer
import korlibs.korge.view.Stage
import korlibs.math.geom.Anchor
import korlibs.math.geom.Size
import kotlin.random.Random

val GAME_WINDOW_SIZE = Size(390, 844)

suspend fun main() =
    Korge(
        windowSize = GAME_WINDOW_SIZE,
        // The pixel art is drawn at a whole scale on the stage, so the stage is too on the window.
        displayMode = KorgeDisplayMode(scaleMode = WholePixelScale, scaleAnchor = Anchor.CENTER, clipBorders = true),
        backgroundColor = Colors["#2b2b2b"],
    ) {
        startGame()
    }

// Shared by `main` and the end-to-end tests, so both boot the game the same way. The tests choose the rolls of the CPU battle brain.
suspend fun Stage.startGame(random: Random = Random.Default) {
    // The resources are downloaded while the menu is displayed, so a new game does not wait as long for them.
    ResourcesBundle.prefetch(views)
    val sceneContainer = sceneContainer()

    sceneContainer.changeTo { MenuScene(random) }
}
