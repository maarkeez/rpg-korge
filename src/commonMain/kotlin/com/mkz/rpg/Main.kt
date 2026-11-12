package com.mkz.rpg

import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import com.mkz.rpg.localization.adapters.resources.ResourcesLocalizationLoader
import com.mkz.rpg.screen.adapters.presentation.MenuScene
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import com.mkz.rpg.storybook.adapters.location.isStorybookRequested
import com.mkz.rpg.storybook.adapters.location.platformStoryLocation
import com.mkz.rpg.storybook.adapters.page.StorybookPage
import com.mkz.rpg.storybook.adapters.page.platformStorybookPage
import com.mkz.rpg.storybook.adapters.presentation.StorybookScene
import com.mkz.rpg.storybook.usecases.services.StoryLocation
import com.mkz.rpg.telemetry.adapters.presentation.TelemetryApi
import com.mkz.rpg.telemetry.adapters.storage.RemoteTelemetryRepository
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.KorgeDisplayMode
import korlibs.korge.scene.sceneContainer
import korlibs.korge.service.storage.IStorage
import korlibs.korge.service.storage.storage
import korlibs.korge.view.Stage
import korlibs.korge.view.addUpdater
import korlibs.math.geom.Size
import korlibs.time.DateTime
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random

val GAME_WINDOW_SIZE = Size(390, 844)

val STORYBOOK_WINDOW_SIZE = Size(1280, 900)
val STORYBOOK_BACKGROUND_COLOR = Colors["#202020"]

suspend fun main() =
    if (isStorybookRequested()) {
        Korge(windowSize = STORYBOOK_WINDOW_SIZE, backgroundColor = STORYBOOK_BACKGROUND_COLOR, displayMode = KorgeDisplayMode.CENTER_NO_CLIP) {
            startStorybook()
        }
    } else {
        Korge(windowSize = GAME_WINDOW_SIZE, backgroundColor = Colors["#2b2b2b"]) {
            startApp()
        }
    }

suspend fun Stage.startApp(
    random: Random = Random.Default,
    storage: IStorage? = null,
) {
    ResourcesBundle.prefetch(views)
    val sceneContainer = sceneContainer()

    val eventBus = InMemoryEventBus()
    addUpdater { eventBus.dispatch() }
    val localizationApi = LocalizationApi(eventBus, ResourcesLocalizationLoader())
    localizationApi.init()
    val telemetryApi = TelemetryApi(storage ?: views.storage, RemoteTelemetryRepository(), CoroutineScope(views.coroutineContext), random) { DateTime.nowUnixMillis() }

    sceneContainer.changeTo { MenuScene(localizationApi, telemetryApi, storage, random) }
}

suspend fun Stage.startStorybook(
    storyLocation: StoryLocation = platformStoryLocation(),
    storybookPage: StorybookPage = platformStorybookPage(),
) {
    val sceneContainer = sceneContainer()

    val eventBus = InMemoryEventBus()
    addUpdater { eventBus.dispatch() }
    val localizationApi = LocalizationApi(eventBus, ResourcesLocalizationLoader())
    localizationApi.init()

    sceneContainer.changeTo { StorybookScene(localizationApi, storyLocation, storybookPage) }
}
