package com.mkz.tessavar

import com.mkz.tessavar.localization.adapters.presentation.LocalizationApi
import com.mkz.tessavar.localization.adapters.resources.ResourcesLocalizationLoader
import com.mkz.tessavar.screen.adapters.presentation.MenuScene
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.adapters.resources.ResourcesBundle
import com.mkz.tessavar.storybook.adapters.location.isStorybookRequested
import com.mkz.tessavar.storybook.adapters.location.platformStoryLocation
import com.mkz.tessavar.storybook.adapters.page.StorybookPage
import com.mkz.tessavar.storybook.adapters.page.platformStorybookPage
import com.mkz.tessavar.storybook.adapters.presentation.StorybookScene
import com.mkz.tessavar.storybook.usecases.services.StoryLocation
import com.mkz.tessavar.telemetry.adapters.presentation.TelemetryApi
import com.mkz.tessavar.telemetry.adapters.storage.RemoteTelemetryRepository
import com.mkz.tessavar.telemetry.adapters.storage.TELEMETRY_COLLECTOR_URL
import korlibs.image.color.Colors
import korlibs.io.net.http.createHttpClient
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
    val telemetryApi =
        TelemetryApi(
            storage ?: views.storage,
            RemoteTelemetryRepository(TELEMETRY_COLLECTOR_URL, createHttpClient()),
            CoroutineScope(views.coroutineContext),
            random,
        ) { DateTime.nowUnixMillis() }

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
