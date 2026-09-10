package com.mkz.rpg.battleUnit.domain

import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.domain.UnitMother.unit
import kotlin.random.Random

object BattleUnitMother {
    fun battleUnit(
        id: String = id(),
        unit: Unit.Dto = unit().toDto(),
        player: Player.Dto = player().toDto(),
        deployAtRow: Int = 0,
        deployAtColumn: Int = 0,
    ): BattleUnit = BattleUnit.deploy(id, unit, player, deployAtRow, deployAtColumn).pullEvents().second

    fun id() = "battle-unit-${Random.nextInt(1, 10000)}"
}
