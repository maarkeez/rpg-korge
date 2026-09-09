package battleunit.domain

import player.domain.Player
import player.domain.PlayerMother
import unit.domain.Unit
import unit.domain.UnitMother
import kotlin.random.Random

object BattleUnitMother {
    fun battleUnit(
        id: String = id(),
        unit: Unit.Dto =
            _root_ide_package_.unit.domain.UnitMother
                .unit()
                .toDto(),
        player: Player.Dto =
            _root_ide_package_.player.domain.PlayerMother
                .player()
                .toDto(),
        deployAtRow: Int = 0,
        deployAtColumn: Int = 0,
    ): BattleUnit = BattleUnit.deploy(id, unit, player, deployAtRow, deployAtColumn).pullEvents().second

    fun id() = "battle-unit-${Random.nextInt(1, 10000)}"
}
