package com.mkz.rpg.screen.adapters.presentation

// Records the text copied, and pastes the text the test puts, instead of using the device clipboard.
class FakeClipboard : Clipboard {
    val copiedTexts = mutableListOf<String>()

    // The text pasted, `null` when the clipboard is empty or can not be read.
    var textToPaste: String? = null

    override suspend fun copy(text: String) {
        copiedTexts += text
    }

    override suspend fun paste(): String? = textToPaste
}
