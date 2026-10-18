package com.mkz.rpg.npc.domain

sealed class NpcError(
    message: String,
) : Throwable(message = message) {
    class NpcNameRequired : NpcError("The npc name is required")

    class NpcLineRequired : NpcError("The npc line is required")

    class NpcTileCanNotBeOccupied : NpcError("The npc tile can not be occupied")

    class NpcUnitNotFound : NpcError("The npc unit was not found")
}
