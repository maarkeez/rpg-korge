package com.mkz.rpg.save.domain

sealed class SaveError(
    message: String,
) : Throwable(message = message) {
    class SlotOutOfRange : SaveError("The slot must be one of the ${Save.SLOTS} save slots")

    class NotPaused : SaveError("The game can only be saved from the pause screen, while exploring")

    class SlotEmpty : SaveError("No game is saved in the slot")

    class IncompatibleSave(
        slot: Int,
        cause: Throwable? = null,
    ) : SaveError(
            "The game saved in slot $slot can not be loaded: it was saved by another version of the game, or names what the game no longer has${cause?.message?.let { reason ->
                " ($reason)"
            }.orEmpty()}",
        )
}
