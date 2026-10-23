package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Once the battle is disengaged during a round (e.g.: the last CPU battle unit engaged was defeated), the battle units
 * joining it (e.g.: the CPU battle units out of it with the party within reach) join it, so it goes on. It finishes
 * without a winner instead, when nobody re-engages it. Nothing happens once the battle is finished, nor when only one
 * player is left in it (e.g.: the player who left it is defeated), as that player wins.
 * Joining player: the player of the battle units joining, null when none joins.
 */
class FinishDisengagedBattle(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        joiningPlayerId: String?,
        joiningBattleUnitIds: List<String>,
    ) {
        val storedBattle = battleRepository.search() ?: return
        if (storedBattle.toDto().isFinished || storedBattle.isBattleFinished()) return
        val joinedBattle =
            joiningPlayerId
                ?.let { playerId -> joiningBattleUnitIds.fold(storedBattle) { battle, battleUnitId -> battle.join(battleUnitId, playerId) } }
                ?: storedBattle
        val nextBattle = if (joinedBattle.isDisengaged()) joinedBattle.finishWithoutWinner() else joinedBattle
        val (events, battle) = nextBattle.pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
