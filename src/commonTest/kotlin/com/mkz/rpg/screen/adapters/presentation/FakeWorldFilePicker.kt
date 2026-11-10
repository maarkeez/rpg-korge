package com.mkz.rpg.screen.adapters.presentation

class FakeWorldFilePicker : WorldFilePicker {
    var pickedText: String? = null

    var failure: Exception? = null

    override suspend fun pickWorld(): String? {
        failure?.let { throw it }
        return pickedText
    }
}
