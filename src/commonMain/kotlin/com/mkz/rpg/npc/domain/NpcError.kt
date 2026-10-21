package com.mkz.rpg.npc.domain

sealed class NpcError(
    message: String,
) : Throwable(message = message) {
    class NpcNameRequired : NpcError("The npc name is required")

    class NpcLineRequired : NpcError("The npc line is required")

    class NpcTileCanNotBeOccupied : NpcError("The npc tile can not be occupied")

    class NpcStockTooLarge : NpcError("The npc stock has more than ${Npc.MAX_STOCK_SIZE} items")

    class NpcStockItemRepeated : NpcError("The npc stock lists an item more than once")

    class NpcStockItemNotFound : NpcError("The npc stock item was not found")

    class NpcNotRecruitable : NpcError("The npc is not recruitable")
}
