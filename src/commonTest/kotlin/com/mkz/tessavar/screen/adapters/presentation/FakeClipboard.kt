package com.mkz.tessavar.screen.adapters.presentation

class FakeClipboard : Clipboard {
    val copiedTexts = mutableListOf<String>()

    override suspend fun copy(text: String) {
        copiedTexts += text
    }
}
