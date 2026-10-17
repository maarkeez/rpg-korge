package com.mkz.rpg.screen.adapters.presentation

// Picks the file the test puts, instead of opening the device file picker.
class FakeMapFilePicker : MapFilePicker {
    // The text of the file picked, `null` when the player picks none.
    var pickedText: String? = null

    // Thrown instead of picking, as when the file can not be read.
    var failure: Exception? = null

    override suspend fun pickMap(): String? {
        failure?.let { throw it }
        return pickedText
    }
}
