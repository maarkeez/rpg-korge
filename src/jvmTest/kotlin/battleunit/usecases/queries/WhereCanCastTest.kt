package battleunit.usecases.queries

import ability.domain.Ability
import ability.domain.AbilityMother
import ability.usecases.queries.SearchAbilityById
import battlefield.domain.Battlefield.Dto.PositionDto
import battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import battlefield.usecases.queries.SearchOccupant
import battlefield.usecases.queries.SearchPosition
import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitMother
import battleunit.usecases.services.DistanceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import player.domain.PlayerMother

class WhereCanCastTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchPosition: SearchPosition = mock()
    private val searchAbilityById: SearchAbilityById = mock()
    private val searchOccupant: SearchOccupant = mock()
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied = mock()
    private val distanceService = DistanceService()
    private val whereCanCast =
        WhereCanCast(
            battleUnitRepository = battleUnitRepository,
            searchPosition = searchPosition,
            searchAbilityById = searchAbilityById,
            searchOccupant = searchOccupant,
            distanceService = distanceService,
            canBattlefieldTileBeOccupied = canBattlefieldTileBeOccupied,
        )

    @Test
    fun `should return own position when the ability targets self`() {
        // Given
        val battleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother
                .battleUnit()
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        whenever(searchPosition(battleUnitId)).thenReturn(PositionDto(2, 2))
        whenever(searchAbilityById("ability-1")).thenReturn(
            _root_ide_package_.ability.domain.AbilityMother
                .ability(targetPattern = Ability.Dto.TargetPatternDto.SELF)
                .toDto(),
        )
        // When
        val result = whereCanCast(battleUnitId, "ability-1")
        // Then
        assertThat(result)
            .containsExactly(WhereCanCast.PositionDto(2, 2))
    }

    @Test
    fun `should return adjacent enemy positions when the ability targets adjacent enemy`() {
        // Given
        val player =
            _root_ide_package_.player.domain.PlayerMother
                .player(id = "player-1")
        val enemyPlayer =
            _root_ide_package_.player.domain.PlayerMother
                .player(id = "player-2")
        val battleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother
                .battleUnit(player = player.toDto())
        val enemyBattleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother
                .battleUnit(player = enemyPlayer.toDto())
        battleUnitRepository.create(battleUnit)
        battleUnitRepository.create(enemyBattleUnit)
        val battleUnitId = battleUnit.toDto().id
        whenever(searchPosition(battleUnitId)).thenReturn(PositionDto(1, 1))
        whenever(searchAbilityById("ability-1")).thenReturn(
            _root_ide_package_.ability.domain.AbilityMother
                .ability(targetPattern = Ability.Dto.TargetPatternDto.ADJACENT_ENEMY)
                .toDto(),
        )
        whenever(searchOccupant(1, 2)).thenReturn(enemyBattleUnit.toDto().id)
        whenever(searchPosition(enemyBattleUnit.toDto().id)).thenReturn(PositionDto(1, 2))
        // When
        val result = whereCanCast(battleUnitId, "ability-1")
        // Then
        assertThat(result)
            .containsExactly(WhereCanCast.PositionDto(1, 2))
    }

    @Test
    fun `should return vacant tiles adjacent to other battle units when the ability targets vacant adjacent tiles`() {
        // Given
        val caster =
            _root_ide_package_.battleunit.domain.BattleUnitMother
                .battleUnit()
        val otherBattleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother
                .battleUnit()
        battleUnitRepository.create(caster)
        battleUnitRepository.create(otherBattleUnit)
        val casterId = caster.toDto().id
        whenever(searchPosition(casterId)).thenReturn(PositionDto(0, 0))
        whenever(searchAbilityById("ability-1")).thenReturn(
            _root_ide_package_.ability.domain.AbilityMother
                .ability(targetPattern = Ability.Dto.TargetPatternDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT)
                .toDto(),
        )
        whenever(searchPosition(otherBattleUnit.toDto().id)).thenReturn(PositionDto(2, 2))
        whenever(canBattlefieldTileBeOccupied(1, 2)).thenReturn(true)
        whenever(canBattlefieldTileBeOccupied(3, 2)).thenReturn(false)
        whenever(canBattlefieldTileBeOccupied(2, 3)).thenReturn(false)
        whenever(canBattlefieldTileBeOccupied(2, 1)).thenReturn(true)
        // When
        val result = whereCanCast(casterId, "ability-1")
        // Then
        assertThat(result).containsExactlyInAnyOrder(
            WhereCanCast.PositionDto(1, 2),
            WhereCanCast.PositionDto(2, 1),
        )
    }

    @Test
    fun `should return empty list when the ability does not exist`() {
        // Given
        val battleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother
                .battleUnit()
        battleUnitRepository.create(battleUnit)
        whenever(searchPosition(battleUnit.toDto().id)).thenReturn(PositionDto(1, 1))
        whenever(searchAbilityById("unknown-ability")).thenReturn(null)
        // When
        val result = whereCanCast(battleUnit.toDto().id, "unknown-ability")
        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should return empty list when the battle unit does not exist`() {
        // Given
        // When
        val result = whereCanCast("unknown-battle-unit", "ability-1")
        // Then
        assertThat(result).isEmpty()
    }
}
