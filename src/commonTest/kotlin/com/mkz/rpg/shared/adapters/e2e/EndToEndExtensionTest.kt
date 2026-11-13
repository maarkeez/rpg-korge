package com.mkz.rpg.shared.adapters.e2e

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

class EndToEndExtensionTest : ViewsForTesting() {
    @Test
    fun `waitUntil should return as soon as the condition holds`() =
        sharedViewsTest {
            // Given
            var checks = 0
            // When
            waitUntil(timeout = 100.milliseconds) { ++checks == 3 }
            // Then
            checks shouldBe 3
        }

    @Test
    fun `waitUntil should fail when the condition does not hold within the timeout`() =
        sharedViewsTest {
            // Given
            val waiting: suspend () -> Unit = { waitUntil(timeout = 100.milliseconds) { false } }
            // When
            val failure = shouldThrow<IllegalStateException> { waiting() }
            // Then
            failure.message shouldBe "waitUntil timed out after 100ms"
        }
}
