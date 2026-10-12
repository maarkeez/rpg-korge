package com.mkz.rpg.shared.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import korlibs.io.async.AsyncEntryPointResult
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest

// Waits until the views test has finished: on JS, `viewsTest` returns a promise.
expect suspend fun AsyncEntryPointResult.awaitViewsTest()

// Runs the views test on every target. KorGE's `viewsTest` only runs on the JVM by default, and on JS it lets the browser load
// the resources only once per second, so the resources are loaded before the views test starts.
fun ViewsForTesting.sharedViewsTest(block: suspend Stage.() -> Unit): TestResult =
    runTest {
        loadedResourcesLoader()
        viewsTest(cond = { true }, block = block).awaitViewsTest()
    }
