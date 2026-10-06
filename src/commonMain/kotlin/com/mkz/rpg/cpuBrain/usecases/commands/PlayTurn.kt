package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.cpuBrain.usecases.queries.WhereShouldMove
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.EventBus
import kotlin.random.Random

class PlayTurn(
    private val searchPlayerById: SearchPlayerById,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val whereCanCast: WhereCanCast,
    private val canCastAbility: CanCastAbility,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val whereShouldMove: WhereShouldMove,
    private val eventBus: EventBus,
    private val random: Random = Random.Default,
) {
    operator fun invoke(playerId: String) {
        val player = searchPlayerById(playerId) ?: return
        if (player.type != Player.Dto.PlayerTypeDto.CPU) return
        val battleUnits = searchBattleUnitsByPlayerId(playerId)
        battleUnits.forEach { battleUnit ->
            tryToCastAbility(battleUnit)
            whereShouldMove(battleUnitId = battleUnit.id)?.let { newPosition ->
                eventBus.publish(
                    BattleUnitEvent.RequestMoveBattleUnit(
                        battleUnitId = battleUnit.id,
                        moveToRow = newPosition.row,
                        moveToColumn = newPosition.column,
                    ),
                )
            }
        }
        eventBus.publish(BattleEvent.RequestFinishPlayerTurn)
    }

    private fun tryToCastAbility(battleUnit: BattleUnit.Dto) {
        val battleUnit = searchBattleUnitById(battleUnit.id) ?: return
        if (battleUnit.remainingTurnActions.remainingCasts > 0) {
            battleUnit.abilityCooldowns.entries
                .filter { it.value == 0 }
                .filter { (abilityId, _) -> canCastAbility(battleUnitId = battleUnit.id, abilityId = abilityId) }
                .pick(random)
                ?.key
                ?.let { abilityId ->

                    whereCanCast(battleUnitId = battleUnit.id, abilityId = abilityId).pick(random)?.let { castGroup ->
                        eventBus.publish(
                            BattleUnitEvent.RequestCastAbility(
                                battleUnitId = battleUnit.id,
                                abilityId = abilityId,
                                castGroup =
                                    castGroup.positions.map { position ->
                                        Battlefield.Dto.PositionDto(row = position.row, column = position.column)
                                    },
                            ),
                        )
                    }
                }
        }
    }

    private fun <T> List<T>.pick(random: Random): T? = if (isEmpty()) null else this[random.nextInt(size)]
}
