package com.mkz.rpg

import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import com.mkz.rpg.screen.adapters.presentation.MenuScene
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.sceneContainer
import korlibs.korge.view.Stage
import korlibs.korge.view.addUpdater
import korlibs.math.geom.Size
import kotlin.random.Random

val GAME_WINDOW_SIZE = Size(390, 844)

suspend fun main() =
    Korge(windowSize = GAME_WINDOW_SIZE, backgroundColor = Colors["#2b2b2b"]) {
        startGame()
    }

// Shared by `main` and the end-to-end tests, so both boot the game the same way. The tests choose the rolls of the CPU battle brain.
suspend fun Stage.startGame(random: Random = Random.Default) {
    // The resources are downloaded while the menu is displayed, so a new game does not wait as long for them.
    ResourcesBundle.prefetch(views)
    val sceneContainer = sceneContainer()

    // The texts of every scene, in the language picked for the session. The menu waits for the catalogs.
    val eventBus = InMemoryEventBus()
    addUpdater { eventBus.dispatch() }
    val localizationApi = LocalizationApi(eventBus)
    localizationApi.init()

    sceneContainer.changeTo { MenuScene(localizationApi, random) }
}
