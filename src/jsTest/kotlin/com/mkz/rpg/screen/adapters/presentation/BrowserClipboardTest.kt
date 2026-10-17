package com.mkz.rpg.screen.adapters.presentation

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.render.ClipboardData
import korlibs.render.GameWindow
import korlibs.render.TextClipboardData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class BrowserClipboardTest {
    // A browser window whose clipboard holds the text, or can not be read when it is `null`.
    private class FakeBrowserWindow(
        private val clipboardText: String?,
    ) : GameWindow() {
        val written = mutableListOf<String>()

        override suspend fun clipboardWrite(data: ClipboardData) {
            written += (data as TextClipboardData).text
        }

        override suspend fun clipboardRead(): ClipboardData? = TextClipboardData(clipboardText ?: throw IllegalStateException("NotAllowedError"))
    }

    private var pasteBoxUses = 0

    private fun browserClipboard(
        window: GameWindow,
        isTouchDevice: Boolean,
    ) = BrowserClipboard(window, isTouchDevice = { isTouchDevice }, pasteBox = {
        pasteBoxUses++
        "pasted in the box"
    })

    @Test
    fun `should read the clipboard straight away when the device is a desktop`() =
        runTest {
            // Given
            val clipboard = browserClipboard(FakeBrowserWindow(clipboardText = "copied"), isTouchDevice = false)
            // When
            val pasted = clipboard.paste()
            // Then
            pasted shouldBe "copied"
            pasteBoxUses shouldBe 0
        }

    @Test
    fun `should paste in the box when the device is a touch device`() =
        runTest {
            // Given
            val clipboard = browserClipboard(FakeBrowserWindow(clipboardText = "copied"), isTouchDevice = true)
            // When
            val pasted = clipboard.paste()
            // Then
            pasted shouldBe "pasted in the box"
        }

    @Test
    fun `should paste in the box when the desktop browser does not let the page read the clipboard`() =
        runTest {
            // Given
            val clipboard = browserClipboard(FakeBrowserWindow(clipboardText = null), isTouchDevice = false)
            // When
            val pasted = clipboard.paste()
            // Then
            pasted shouldBe "pasted in the box"
        }

    @Test
    fun `should write the clipboard of the browser when text is copied`() =
        runTest {
            // Given
            val window = FakeBrowserWindow(clipboardText = null)
            val clipboard = browserClipboard(window, isTouchDevice = true)
            // When
            clipboard.copy("exported")
            // Then
            window.written.shouldContainExactly("exported")
            pasteBoxUses shouldBe 0
        }
}
