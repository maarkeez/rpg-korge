package com.mkz.rpg.shared.adapters.presentation

import korlibs.io.async.launchImmediately
import korlibs.korge.KorgeConfig
import korlibs.korge.KorgeRunner
import korlibs.korge.internal.KorgeInternal
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage
import korlibs.time.fast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Runs the views test on every target, like KorGE's `viewsTest`, which only runs on the JVM.
 *
 * `viewsTest` lets the browser run only once per second, while the views load their resources from the browser, so the
 * frames are simulated here letting the browser run between them.
 */
@OptIn(KorgeInternal::class)
fun ViewsForTesting.sharedViewsTest(block: suspend Stage.() -> Unit): TestResult =
    runTest(timeout = 60.seconds) {
        viewsLog.init()
        KorgeRunner.prepareViewsBase(views, gameWindow, fixedSizeStep = frameTime.fast, forceRenderEveryFrame = true)
        injector.mapInstance(KorgeConfig(title = "SharedViewsTest", windowSize = windowSize, virtualSize = virtualSize))
        var completed = false
        var failure: Throwable? = null
        dispatcher.dispatch(views.coroutineContext, {
            launchImmediately(views.coroutineContext + dispatcher) {
                try {
                    block(views.stage)
                } catch (error: Throwable) {
                    failure = error
                } finally {
                    completed = true
                }
            }
        })
        while (!completed) {
            time += frameTime
            gameWindow.dispatchRenderEvent()
            dispatcher.executePending(availableTime = 1.seconds)
            withContext(Dispatchers.Default) { delay(1.milliseconds) }
        }
        failure?.let { throw it }
    }
