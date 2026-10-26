package com.mkz.rpg.player.domain

import com.mkz.rpg.player.domain.PlayerError.EmptyPlayerId
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Player private constructor(
    private val id: Id,
    private val type: Type,
    private val events: Set<PlayerEvent>,
) {
    companion object {
        fun createHuman(id: String) =
            Player(
                id = Id(id),
                type = Type.HUMAN,
                events =
                    setOf(
                        PlayerEvent.PlayerCreated(
                            playerId = id,
                            playerType = Type.HUMAN.toDto(),
                        ),
                    ),
            )

        fun createCpu(id: String) =
            Player(
                id = Id(id),
                type = Type.CPU,
                events =
                    setOf(
                        PlayerEvent.PlayerCreated(
                            playerId = id,
                            playerType = Type.CPU.toDto(),
                        ),
                    ),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.toDto(),
            type = type.toDto(),
        )

    data class Dto(
        val id: String,
        val type: PlayerTypeDto,
    ) {
        enum class PlayerTypeDto {
            CPU,
            HUMAN,
        }
    }

    @JvmInline
    private value class Id(
        val value: String,
    ) {
        init {
            if (value.isEmpty()) throw EmptyPlayerId()
        }

        fun toDto() = value
    }

    private enum class Type {
        CPU,
        HUMAN,
        ;

        fun toDto() = Dto.PlayerTypeDto.valueOf(name)
    }
}
