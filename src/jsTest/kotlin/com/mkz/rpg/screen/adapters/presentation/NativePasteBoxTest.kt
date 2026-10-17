package com.mkz.rpg.screen.adapters.presentation

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class NativePasteBoxTest {
    @Test
    fun `should display the box with the hint when the player is asked to paste`() =
        runTest {
            // When
            val pasting = async(start = CoroutineStart.UNDISPATCHED) { NativePasteBox().paste() }
            // Then
            isNativePasteBoxDisplayed().shouldBeTrue()
            nativePasteBoxHint() shouldBe NativePasteBox.HINT
            cancelNativePasteBox()
            pasting.join()
        }

    @Test
    fun `should return the text and close the box when the player pastes in it`() =
        runTest {
            // Given
            val pasting = async(start = CoroutineStart.UNDISPATCHED) { NativePasteBox().paste() }
            // When
            pasteInNativePasteBox("tiles = [[\"sand\"]]")
            // Then
            pasting.await() shouldBe "tiles = [[\"sand\"]]"
            isNativePasteBoxDisplayed().shouldBeFalse()
        }

    @Test
    fun `should cancel pasting and close the box when the player cancels`() =
        runTest {
            // Given
            val pasting = async(start = CoroutineStart.UNDISPATCHED) { NativePasteBox().paste() }
            // When
            cancelNativePasteBox()
            // Then
            pasting.join()
            pasting.isCancelled.shouldBeTrue()
            isNativePasteBoxDisplayed().shouldBeFalse()
        }

    @Test
    fun `should close the box when pasting is cancelled by the game`() =
        runTest {
            // Given
            val pasting = async(start = CoroutineStart.UNDISPATCHED) { NativePasteBox().paste() }
            // When
            pasting.cancel()
            pasting.join()
            // Then
            isNativePasteBoxDisplayed().shouldBeFalse()
        }
}
