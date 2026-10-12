package com.mkz.rpg.shared.adapters.presentation

import korlibs.io.async.AsyncEntryPointResult
import kotlinx.coroutines.await
import kotlin.js.Promise

actual suspend fun AsyncEntryPointResult.awaitViewsTest() {
    unsafeCast<Promise<Unit>>().await()
}
