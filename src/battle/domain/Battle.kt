package battle.domain

import battle.domain.BattleError.MinimumTwoPlayersRequired
import battle.domain.BattleEvent.*
import korlibs.event.Event
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Battle private constructor(
    private val turnQueue: TurnQueue,
    private val currentRound: CurrentRound,
    private val currentPlayerTurn: CurrentPlayerTurn,
    private val isFinished: IsFinished,
    private val events: Set<BattleEvent>,
) {

    companion object {
        fun startFirstRound(
            players: List<String>
        ): Battle{
            return Battle(
                turnQueue = TurnQueue(players),
                currentRound = CurrentRound(1),
                currentPlayerTurn = CurrentPlayerTurn(players.first()),
                isFinished = IsFinished(false),
                events = setOf(BattleStarted, PlayerTurnStarted(players.first()))
            )
        }
    }
    
    fun toDto() = Dto(
        currentPlayerTurn = currentPlayerTurn.value,
        currentRound = currentRound.value
    )
    
    fun startNextRound(): Battle {
        val nextRound = CurrentRound(currentRound.value + 1)
        val nextPlayerTurn = CurrentPlayerTurn(turnQueue.value.first())
        return copy(
            currentRound = nextRound,
            currentPlayerTurn = CurrentPlayerTurn(turnQueue.value.first()),
            events = events + setOf(BattleRoundStarted(nextRound.value ) , PlayerTurnStarted(nextPlayerTurn.value))
        )
    }

    fun finishPlayerTurn(): Battle {
        val currentPlayerTurnIndex = turnQueue.value.indexOf(currentPlayerTurn.value)
        val nextPlayerTurnIndex = currentPlayerTurnIndex + 1
        val roundFinished = nextPlayerTurnIndex >= turnQueue.value.size
        if (roundFinished){
            return copy(
                events = events + setOf(BattleRoundFinished(currentRound.value ))
            )
        } else{
            val nextPlayerTurn = turnQueue.value[nextPlayerTurnIndex]
            return copy(
                currentPlayerTurn = CurrentPlayerTurn(nextPlayerTurn),
                events = events + setOf(PlayerTurnStarted(nextPlayerTurn))
            )
        }
    }

    fun finishBattle(): Battle = copy(
        isFinished = IsFinished(true),
        events = events + PlayerVictory(turnQueue.value.single()),
    )

    fun defeatPlayer(playerId: String): Battle {
        val newTurnQueue = buildList {
            addAll(turnQueue.value)
            remove(playerId)
        }
        val battleWithPlayerTurnUpdated: Battle = if(currentPlayerTurn.value == playerId) finishPlayerTurn() else this
        return battleWithPlayerTurnUpdated.copy(
            turnQueue = TurnQueue(newTurnQueue),
            events = events + setOf(PlayerDefeated(playerId))
        )
    }

    fun pullEvents() = events to copy(events = emptySet())

    @JvmInline private value class TurnQueue(val value: List<String>){
        init {
            if(value.size < 2) throw MinimumTwoPlayersRequired()
        }
    }
    @JvmInline private value class CurrentRound(val value: Int)
    @JvmInline private value class CurrentPlayerTurn(val value: String)
    @JvmInline private value class IsFinished(val value: Boolean)
    
    
    data class Dto(
        val currentPlayerTurn: String,
        val currentRound: Int,
    )
}
