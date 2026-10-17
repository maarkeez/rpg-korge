package com.mkz.rpg.screen.adapters.presentation

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event

// Page object of [NativePasteBox]: plays the player's part on the box over the game.

fun isNativePasteBoxDisplayed(): Boolean = document.getElementById(NativePasteBox.OVERLAY_ID) != null

fun nativePasteBoxHint(): String? = document.getElementById(NativePasteBox.OVERLAY_ID)?.firstElementChild?.textContent

/** Pastes the text in the box, as the browser does when the player chooses Paste. */
fun pasteInNativePasteBox(text: String) {
    val textArea = document.querySelector("#${NativePasteBox.OVERLAY_ID} textarea") as HTMLTextAreaElement
    val clipboardData: dynamic = js("new DataTransfer()")
    clipboardData.setData("text/plain", text)
    val init: dynamic = js("({ bubbles: true, cancelable: true })")
    init.clipboardData = clipboardData
    val pasteEvent: Event = js("new ClipboardEvent('paste', init)").unsafeCast<Event>()
    textArea.dispatchEvent(pasteEvent)
}

fun cancelNativePasteBox() = (document.querySelector("#${NativePasteBox.OVERLAY_ID} button") as HTMLButtonElement).click()
