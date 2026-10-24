package com.mkz.rpg.building.domain

sealed class BuildingError(
    message: String,
) : Throwable(message = message) {
    class BuildingSizeNotPositive : BuildingError("The building must be at least 1 row and 1 column")

    class BuildingTileCanNotBeOccupied : BuildingError("A tile of the building can not be occupied")
}
