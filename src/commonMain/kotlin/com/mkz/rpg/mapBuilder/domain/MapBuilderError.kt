package com.mkz.rpg.mapBuilder.domain

sealed class MapBuilderError(
    message: String,
) : Throwable(message = message) {
    class MapNotCreated : MapBuilderError("The map has not been created")

    class TileNotFound : MapBuilderError("The tile is out of the map")
}
