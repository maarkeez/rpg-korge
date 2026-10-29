package com.mkz.rpg

import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import com.mkz.rpg.screen.adapters.presentation.MenuScene
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import com.mkz.rpg.storybook.adapters.location.isStorybookRequested
import com.mkz.rpg.storybook.adapters.location.platformStoryLocation
import com.mkz.rpg.storybook.adapters.page.StorybookPage
import com.mkz.rpg.storybook.adapters.page.platformStorybookPage
import com.mkz.rpg.storybook.adapters.presentation.StorybookScene
import com.mkz.rpg.storybook.usecases.services.StoryLocation
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.KorgeDisplayMode
import korlibs.korge.scene.sceneContainer
import korlibs.korge.view.Stage
import korlibs.korge.view.addUpdater
import korlibs.math.geom.Size
import kotlin.random.Random

val GAME_WINDOW_SIZE = Size(390, 844)

// Landscape, so the sidebar and the story info fit on both sides of a phone as large as the game window.
val STORYBOOK_WINDOW_SIZE = Size(1280, 900)
val STORYBOOK_BACKGROUND_COLOR = Colors["#202020"]

// The same bundle starts the game, or the Storybook when it is asked for: see docs/plans/storybook.md.
suspend fun main() =
    if (isStorybookRequested()) {
        // Drawn on the whole screen, around the Storybook fitted in it, so it can be zoomed in to fill a portrait phone.
        Korge(windowSize = STORYBOOK_WINDOW_SIZE, backgroundColor = STORYBOOK_BACKGROUND_COLOR, displayMode = KorgeDisplayMode.CENTER_NO_CLIP) {
            startStorybook()
        }
    } else {
        Korge(windowSize = GAME_WINDOW_SIZE, backgroundColor = Colors["#2b2b2b"]) {
            startGame()
        }
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

// Shared by `main` and the end-to-end tests, so both boot the Storybook the same way. The tests choose the story asked for,
// and see when it has rendered.
suspend fun Stage.startStorybook(
    storyLocation: StoryLocation = platformStoryLocation(),
    storybookPage: StorybookPage = platformStorybookPage(),
) {
    val sceneContainer = sceneContainer()

    // The texts of the views on show, in English until the language global selects another one.
    val eventBus = InMemoryEventBus()
    addUpdater { eventBus.dispatch() }
    val localizationApi = LocalizationApi(eventBus)
    localizationApi.init()

    sceneContainer.changeTo { StorybookScene(localizationApi, storyLocation, storybookPage) }
}
