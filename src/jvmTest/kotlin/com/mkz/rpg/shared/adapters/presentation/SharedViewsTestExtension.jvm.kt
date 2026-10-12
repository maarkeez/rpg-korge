package com.mkz.rpg.shared.adapters.presentation

import korlibs.io.async.AsyncEntryPointResult

// On the JVM, `viewsTest` blocks until the views test has finished.
actual suspend fun AsyncEntryPointResult.awaitViewsTest() {}
