package com.mkz.rpg.screen.adapters.presentation

import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.clipboard.ClipboardEvent
import kotlin.coroutines.resume

/**
 * A box over the game where the player pastes with the browser's own paste (e.g.: holding the box and choosing Paste on
 * iOS), which needs no permission to read the clipboard.
 */
class NativePasteBox {
    companion object {
        const val OVERLAY_ID = "native-paste-box"
        const val HINT = "Tap the box and choose Paste"
        const val CANCEL = "Cancel"

        private const val OVERLAY_STYLE =
            "position:fixed;inset:0;z-index:1000;display:flex;flex-direction:column;align-items:center;justify-content:center;" +
                "gap:12px;background:rgba(0,0,0,0.7);font-family:sans-serif;color:white"

        // Text of 16px at least, so iOS does not zoom in on the box.
        private const val TEXT_AREA_STYLE = "width:80%;max-width:360px;height:120px;font-size:16px;border-radius:6px"
        private const val CANCEL_STYLE = "font-size:16px;padding:8px 24px;border-radius:6px"
    }

    /** The text pasted. Cancelled when the player cancels pasting. */
    suspend fun paste(): String? =
        suspendCancellableCoroutine { continuation ->
            val overlay = document.createElement("div") as HTMLDivElement
            overlay.id = OVERLAY_ID
            overlay.setAttribute("style", OVERLAY_STYLE)
            val hint = document.createElement("div") as HTMLDivElement
            hint.textContent = HINT
            val textArea = document.createElement("textarea") as HTMLTextAreaElement
            textArea.setAttribute("style", TEXT_AREA_STYLE)
            val cancel = document.createElement("button") as HTMLButtonElement
            cancel.textContent = CANCEL
            cancel.setAttribute("style", CANCEL_STYLE)
            overlay.append(hint, textArea, cancel)

            fun close() = overlay.remove()
            textArea.addEventListener("paste", { event ->
                event.preventDefault()
                close()
                if (continuation.isActive) continuation.resume(event.unsafeCast<ClipboardEvent>().clipboardData?.getData("text/plain"))
            })
            cancel.addEventListener("click", {
                close()
                continuation.cancel()
            })
            continuation.invokeOnCancellation { close() }

            document.body?.appendChild(overlay)
            textArea.focus()
        }
}
