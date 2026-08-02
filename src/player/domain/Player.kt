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

    fun pullEvents(): Pair<Set<PlayerEvent>, Player> =
        events to copy(events = emptySet())

    fun toDto()= PlayerDto(
        id = id.toDto(),
        name = name.toDto(),
        type = type.toDto(),
    )

    data class PlayerDto(
        val id: String,
        val name: String,
        val type: String,
    )

    @JvmInline
    private value class Id private constructor(val value: String){

        companion object {
            operator fun invoke(value: String): Id {
               if(value.isEmpty()) throw EmptyPlayerId()
                return Player.Id(value)
            }
        }
        fun toDto() = value
    }
    @JvmInline
    private value class Name private constructor(val value: String){

        companion object {
            operator fun invoke(value: String): Name {
                if(value.isEmpty()) throw EmptyPlayerName()
                if(value.count() > 50) throw PlayerNameLongerThanExpected()
                return Player.Name(value)
            }
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
