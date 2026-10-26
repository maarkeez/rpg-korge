package com.mkz.rpg.player.domain

import korlibs.io.util.UUID

object PlayerMother {
    fun player(
        id: String = id(),
        type: Player.Dto.PlayerTypeDto = type(),
    ): Player = (if (type == Player.Dto.PlayerTypeDto.CPU) Player.createCpu(id) else Player.createHuman(id)).pullEvents().second

    fun id() = "player-${UUID.randomUUID()}"

    fun type() = listOf(Player.Dto.PlayerTypeDto.HUMAN, Player.Dto.PlayerTypeDto.CPU).random()
}
