package battle.domain

sealed interface BattleEvent {
    object BattleStarted : BattleEvent
    data class BattleRoundStarted(val round: Int) : BattleEvent
    data class BattleRoundFinished(val round: Int) : BattleEvent
    data class PlayerTurnStarted(val playerId: String) : BattleEvent
    data class PlayerVictory(val playerId: String) : BattleEvent
    data class PlayerDefeated(val playerId: String) : BattleEvent
}
