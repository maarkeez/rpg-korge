package com.mkz.rpg.screen.adapters.presentation

// Records the text copied, instead of copying it to the device clipboard.
class FakeClipboard : Clipboard {
    val copiedTexts = mutableListOf<String>()

    override suspend fun copy(text: String) {
        copiedTexts += text
    }
}
