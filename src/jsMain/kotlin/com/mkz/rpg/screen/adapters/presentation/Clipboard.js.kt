package com.mkz.rpg.screen.adapters.presentation

import korlibs.render.GameWindow
import korlibs.render.TextClipboardData

actual fun deviceClipboard(gameWindow: GameWindow): Clipboard = BrowserClipboard(gameWindow)

/**
 * The clipboard of the browser. Pasting reads it straight away on desktops. Touch devices (e.g.: Safari on iOS) do not
 * let a page read it without a native paste, which may never come (the read waits forever), so they paste in a
 * [NativePasteBox], also used when a desktop browser does not let the page read the clipboard.
 */
class BrowserClipboard(
    private val gameWindow: GameWindow,
    private val isTouchDevice: () -> Boolean = ::isTouchDevice,
    private val pasteBox: suspend () -> String? = { NativePasteBox().paste() },
) : Clipboard {
    override suspend fun copy(text: String) = gameWindow.clipboardWrite(TextClipboardData(text))

    override suspend fun paste(): String? {
        if (isTouchDevice()) return pasteBox()
        return try {
            (gameWindow.clipboardRead() as? TextClipboardData)?.text
        } catch (_: Throwable) {
            pasteBox()
        }
    }
}

private fun isTouchDevice(): Boolean = js("('ontouchstart' in window) || navigator.maxTouchPoints > 0").unsafeCast<Boolean>()
