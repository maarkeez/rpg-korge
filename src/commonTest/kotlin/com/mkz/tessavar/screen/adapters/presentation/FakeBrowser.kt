package com.mkz.tessavar.screen.adapters.presentation

class FakeBrowser : Browser {
    val openedUrls = mutableListOf<String>()

    override suspend fun open(url: String) {
        openedUrls += url
    }
}
