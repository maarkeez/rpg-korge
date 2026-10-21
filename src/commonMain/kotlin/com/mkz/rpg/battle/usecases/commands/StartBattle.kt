package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Starts the first round of a battle, e.g.: when the CPU engages the exploring player. The engaging player plays first,
 * with the battle units that engaged, and the engaged player second, with the battle units engaged (e.g.: all their
 * battle units). Nothing happens while another battle is in progress. A new battle replaces the finished one.
 */
class StartBattle(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        engagingPlayerId: String,
        engagedPlayerId: String,
        engagingBattleUnitIds: List<String>,
        engagedBattleUnitIds: List<String>,
    ) {
        if (battleRepository.search()?.toDto()?.isFinished == false) return
        val (events, battle) =
            Battle
                .startFirstRound(
                    players = listOf(engagingPlayerId, engagedPlayerId),
                    battleUnits = engagingBattleUnitIds.associateWith { engagingPlayerId } + engagedBattleUnitIds.associateWith { engagedPlayerId },
                ).pullEvents()
        battleRepository.create(battle)
        eventBus.publish(events)
    }
}
