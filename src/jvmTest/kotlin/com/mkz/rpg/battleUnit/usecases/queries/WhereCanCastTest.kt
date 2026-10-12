package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import kotlin.test.Test

class WhereCanCastTest {
    private val queries = InMemoryQueries()
    private val whereCanCast = queries.whereCanCast
    private val player = player(id = "player-1").toDto()
    private val enemyPlayer = player(id = "player-2").toDto()

    private fun storeAbility(targeting: Ability.Dto.TargetingDto) = queries.abilityRepository.create(ability(id = "ability-1", targeting = targeting))

    // Stores the battle units of the given players and the battlefield where they stand.
    private fun battlefieldWith(
        map: String,
        players: Map<Char, String>,
    ) {
        players.forEach { (letter, playerId) ->
            queries.battleUnitRepository.create(battleUnit(id = "$letter-1", player = if (playerId == player.id) player else enemyPlayer))
        }
        queries.battlefieldRepository.create(battlefield(map, occupants = players.keys.associateWith { letter -> "$letter-1" }))
    }

    @Test
    fun `should return own position when the ability targets self`() {
        // Given
        storeAbility(targeting = Ability.Dto.TargetingDto.SELF)
        battlefieldWith(
            """
            . . .
            . . .
            . . K
            """,
            players = mapOf('K' to player.id),
        )
        // When
        val result = whereCanCast("K-1", "ability-1")
        // Then
        result.shouldContainExactly(WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(2, 2))))
    }

    @Test
    fun `should return adjacent enemy positions when the ability targets adjacent enemy`() {
        // Given
        storeAbility(targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY)
        battlefieldWith(
            """
            . . .
            . K R
            . . .
            """,
            players = mapOf('K' to player.id, 'R' to enemyPlayer.id),
        )
        // When
        val result = whereCanCast("K-1", "ability-1")
        // Then
        result.shouldContainExactly(WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(1, 2))))
    }

    @Test
    fun `should find enemy in the northern tile when the ability targets adjacent enemy`() {
        // Given
        storeAbility(targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY)
        battlefieldWith(
            """
            . R .
            . K .
            . . .
            """,
            players = mapOf('K' to player.id, 'R' to enemyPlayer.id),
        )
        // When
        val result = whereCanCast("K-1", "ability-1")
        // Then
        result.shouldContainExactly(WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 1))))
    }

    @Test
    fun `should return a single cast group with all adjacent enemy positions when the ability targets all adjacent enemies`() {
        // Given
        storeAbility(targeting = Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES)
        // E and F are adjacent enemies, G is a diagonal enemy and A is an ally.
        battlefieldWith(
            """
            . . .
            A K E
            . F G
            """,
            players = mapOf('K' to player.id, 'A' to player.id, 'E' to enemyPlayer.id, 'F' to enemyPlayer.id, 'G' to enemyPlayer.id),
        )
        // When
        val result = whereCanCast("K-1", "ability-1")
        // Then
        result.shouldHaveSize(1)
        result.first().positions.shouldContainExactlyInAnyOrder(
            WhereCanCast.PositionDto(1, 2),
            WhereCanCast.PositionDto(2, 1),
        )
    }

    @Test
    fun `should return empty list when the ability targets all adjacent enemies and there are no adjacent enemies`() {
        // Given
        storeAbility(targeting = Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES)
        battlefieldWith(
            """
            . . .
            . K .
            . . .
            """,
            players = mapOf('K' to player.id),
        )
        // When
        val result = whereCanCast("K-1", "ability-1")
        // Then
        result.shouldBeEmpty()
    }

    @Test
    fun `should return vacant tiles adjacent to other battle units when the ability targets vacant adjacent tiles`() {
        // Given
        storeAbility(targeting = Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT)
        battlefieldWith(
            """
            C . .
            . . .
            . . O
            """,
            players = mapOf('C' to player.id, 'O' to enemyPlayer.id),
        )
        // When
        val result = whereCanCast("C-1", "ability-1")
        // Then
        result.shouldContainExactlyInAnyOrder(
            WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(1, 2))),
            WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(2, 1))),
        )
    }

    @Test
    fun `should return empty list when the ability does not exist`() {
        // Given
        battlefieldWith(
            """
            . . .
            . K .
            . . .
            """,
            players = mapOf('K' to player.id),
        )
        // When
        val result = whereCanCast("K-1", "unknown-ability")
        // Then
        result.shouldBeEmpty()
    }

    @Test
    fun `should return empty list when the battle unit does not exist`() {
        // Given
        // When
        val result = whereCanCast("unknown-battle-unit", "ability-1")
        // Then
        result.shouldBeEmpty()
    }

    @Test
    fun `should return adjacent vacant tiles when the ability targets vacant adjacent tiles to self`() {
        // Given
        storeAbility(targeting = Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_SELF)
        // The diagonal tile can be occupied too, but it is not adjacent.
        battlefieldWith(
            """
            C .
            # .
            """,
            players = mapOf('C' to player.id),
        )
        // When
        val result = whereCanCast("C-1", "ability-1")
        // Then
        result.shouldContainExactly(WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(row = 0, column = 1))))
    }
}
