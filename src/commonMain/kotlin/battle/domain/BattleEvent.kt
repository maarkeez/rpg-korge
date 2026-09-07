package battle.domain

import shared.domain.DomainEvent

sealed interface BattleEvent: DomainEvent {
    object BattleStarted : BattleEvent
    data class BattleRoundStarted(val round: Int) : BattleEvent
    data class BattleRoundFinished(val round: Int) : BattleEvent
    data class PlayerTurnStarted(val playerId: String) : BattleEvent
    data class PlayerVictory(val playerId: String) : BattleEvent
    data class PlayerDefeated(val playerId: String) : BattleEvent
}
