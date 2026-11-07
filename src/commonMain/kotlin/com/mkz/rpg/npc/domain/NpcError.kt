package com.mkz.rpg.npc.domain

sealed class NpcError(
    message: String,
) : Throwable(message = message) {
    class NpcTileNotVacant : NpcError("The npc tile is not vacant")

    class NpcStockTooLarge : NpcError("The npc stock has more than ${Npc.MAX_STOCK_SIZE} items")

    class NpcStockItemRepeated : NpcError("The npc stock lists an item more than once")

    class NpcStockItemNotFound : NpcError("The npc stock item was not found")

    class NpcNotRecruitable : NpcError("The npc is not recruitable")
}
