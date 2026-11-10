package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleRepository
import com.mkz.rpg.shared.domain.EventBus

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
