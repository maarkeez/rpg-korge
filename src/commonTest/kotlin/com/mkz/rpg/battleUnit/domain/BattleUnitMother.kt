package com.mkz.rpg.battleUnit.domain

import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.domain.UnitMother.unit
import korlibs.io.util.UUID

object BattleUnitMother {
    fun battleUnit(
        id: String = id(),
        unit: Unit.Dto = unit().toDto(),
        player: Player.Dto = player().toDto(),
        deployAtRow: Int = 0,
        deployAtColumn: Int = 0,
        remainingHealthPoints: Int = unit.healthPoints,
    ): BattleUnit {
        val deployedBattleUnit = BattleUnit.deploy(id, unit, player, deployAtRow, deployAtColumn).pullEvents().second
        val damage = unit.healthPoints - remainingHealthPoints
        if (damage == 0) return deployedBattleUnit
        // A battle unit can only lose health by receiving damage.
        val damageEffect = decreaseHealthEffect(damage = damage, applicationType = "IMMEDIATELY").toDto()
        return deployedBattleUnit.applyImmediateEffect(damageEffect, deployAtRow, deployAtColumn).pullEvents().second
    }

    fun id() = "battle-unit-${UUID.randomUUID()}"
}
