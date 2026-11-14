package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.e2e.openMenu
import com.mkz.rpg.shared.adapters.e2e.waitUntil
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.telemetry.adapters.presentation.TelemetryApi
import com.mkz.rpg.telemetry.adapters.storage.RemoteTelemetryRepository
import io.kotest.matchers.collections.shouldContainExactly
import korlibs.io.net.http.FakeHttpClient
import korlibs.korge.scene.sceneContainer
import korlibs.korge.service.storage.InmemoryStorage
import korlibs.korge.tests.ViewsForTesting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.random.Random
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MenuSceneTest : ViewsForTesting() {
    private val storage = InmemoryStorage()
    private val browser = FakeBrowser()

    private fun telemetryApi() = TelemetryApi(storage, RemoteTelemetryRepository(collectorUrl = null, httpClient = FakeHttpClient()), TestScope(UnconfinedTestDispatcher()), Random(1)) { 1_000.0 }

    @Test
    fun `MenuScene should open the signup form of the mailing list when Wishlist is clicked`() =
        sharedViewsTest {
            // Given
            sceneContainer().changeTo { MenuScene(MessagesMother.localizationApi(), telemetryApi(), storage, browser = browser) }
            val menuView = openMenu()
            // When
            clickWishlist(menuView)
            // Then
            waitUntil { browser.openedUrls.isNotEmpty() }
            browser.openedUrls.shouldContainExactly(SIGNUP_FORM_URL)
        }
}
