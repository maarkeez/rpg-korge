package com.mkz.tessavar.battleUnit.domain

import com.mkz.tessavar.effect.domain.EffectMother.damageEffect
import com.mkz.tessavar.player.domain.Player
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.unit.domain.Unit
import com.mkz.tessavar.unit.domain.UnitMother.unit
import korlibs.io.util.UUID

object BattleUnitMother {
    fun battleUnit(
        id: String = id(),
        unit: Unit.Dto = unit().toDto(),
        player: Player.Dto = player().toDto(),
        maximumAbilities: Int = 6,
        deployAtRow: Int = 0,
        deployAtColumn: Int = 0,
        remainingHealth: Int = unit.baseMaximumHealth,
        remainingSummonDuration: Int? = null,
    ): BattleUnit {
        val deployedBattleUnit = BattleUnit.deploy(id, unit, player, maximumAbilities, deployAtRow, deployAtColumn, remainingSummonDuration).pullEvents().second
        val damage = unit.baseMaximumHealth - remainingHealth
        if (damage == 0) return deployedBattleUnit
        val damageEffect = damageEffect(damage = damage, timingType = "IMMEDIATELY").toDto()
        return deployedBattleUnit.triggerImmediateEffect(damageEffect, deployAtRow, deployAtColumn).pullEvents().second
    }

    fun id() = "battle-unit-${UUID.randomUUID()}"
}
