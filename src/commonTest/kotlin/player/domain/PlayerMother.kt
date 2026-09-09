package player.domain

import kotlin.random.Random

object PlayerMother {
    fun player(
        id: String = id(),
        name: String = name(),
        type: Player.Dto.PlayerTypeDto = type(),
    ): Player = if (type == Player.Dto.PlayerTypeDto.CPU) Player.createCpu(id, name) else Player.createHuman(id, name)

    fun id() = "player-${Random.nextInt(1, 100)}"

    fun name() = "Player ${Random.nextInt(1, 999)}"

    fun type() = listOf(Player.Dto.PlayerTypeDto.HUMAN, Player.Dto.PlayerTypeDto.CPU).random()
}
