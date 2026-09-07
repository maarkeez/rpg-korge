package battleunit.domain

import player.domain.*
import unit.domain.*
import unit.domain.Unit
import kotlin.random.*

object BattleUnitMother {

    fun battleUnit(
        id: String = id(),
        unit: Unit.Dto = UnitMother.unit().toDto(),
        player: Player.Dto = PlayerMother.player().toDto(),
        deployAtRow: Int = 0,
        deployAtColumn: Int = 0,
    ): BattleUnit =
        BattleUnit.deploy(id, unit, player, deployAtRow, deployAtColumn).pullEvents().second

    fun id() = "battle-unit-${Random.nextInt(1, 10000)}"
}
