package com.mkz.tessavar.worldBuilder.domain

sealed class WorldBuilderError(
    message: String,
) : Throwable(message = message) {
    class WorldNotCreated : WorldBuilderError("The world has not been created")

    class TileNotFound : WorldBuilderError("The tile is out of the world")
}
