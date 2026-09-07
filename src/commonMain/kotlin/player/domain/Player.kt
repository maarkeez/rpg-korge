package player.domain

import player.domain.PlayerError.EmptyPlayerId
import player.domain.PlayerError.EmptyPlayerName
import player.domain.PlayerError.PlayerNameLongerThanExpected
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Player private constructor(
    private val id: Id,
    private val name: Name,
    private val type: Type,
    private val events: Set<PlayerEvent>,
) {

    companion object {
        fun createHuman(id: String, name: String) = Player(
            id = Id(id),
            name = Name(name),
            type = Type.HUMAN,
            events = setOf(PlayerEvent.PlayerCreated(
                playerId = id,
                playerName = name,
                playerType = Type.HUMAN.toDto()
            ))
        )
        fun createCpu(id: String, name: String) = Player(
            id = Id(id),
            name = Name(name),
            type = Type.CPU,
            events = setOf(PlayerEvent.PlayerCreated(
                playerId = id,
                playerName = name,
                playerType = Type.CPU.toDto()
            ))
        )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto()= Dto(
        id = id.toDto(),
        name = name.toDto(),
        type = type.toDto(),
    )

    data class Dto(
        val id: String,
        val name: String,
        val type: String,
    )

    @JvmInline
    private value class Id(val value: String){
        init {
            if(value.isEmpty()) throw EmptyPlayerId()
        }
        fun toDto() = value
    }
    @JvmInline
    private value class Name(val value: String){

        init {
            if(value.isEmpty()) throw EmptyPlayerName()
            if(value.count() > 50) throw PlayerNameLongerThanExpected()
        }

        fun toDto() = value
    }
    private enum class Type {
        CPU,
        HUMAN;

        fun toDto() = when(this) {
            CPU -> "CPU"
            HUMAN -> "HUMAN"
        }
    }
}
