package com.mkz.rpg.battlefieldHud.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability.AbilityAvailability
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability.AbilityAvailability.Status
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.tile
import com.mkz.rpg.screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudError
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.battlefieldHud.usecases.commands.ProcessAbilitySelected
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ProcessAbilitySelectedTest {
    private val searchAbilityAvailability: SearchAbilityAvailability = mock()
    private val whereCanCast: WhereCanCast = mock()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val processAbilitySelected =
        ProcessAbilitySelected(
            searchAbilityAvailability = searchAbilityAvailability,
            whereCanCast = whereCanCast,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should select the ability when the hud is displaying the movement range and the battle unit can cast`() {
        // Given
        val castGroupsWhereCanCast =
            listOf(
                com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto
                    .CastGroupDto(tiles = listOf(tile(1, 1))),
                com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto
                    .CastGroupDto(tiles = listOf(tile(2, 2))),
            )
        battlefieldHudRepository.create(
            displayMovementRange(
                tile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
            ),
        )
        whenever(searchAbilityAvailability("battle-unit-1")).thenReturn(listOf(availability("ability-1", Status.READY)))
        whenever(whereCanCast("battle-unit-1", "ability-1"))
            .thenReturn(
                castGroupsWhereCanCast.map { castGroup ->
                    WhereCanCast.CastGroup(
                        positions =
                            castGroup.tiles
                                .map { tile -> WhereCanCast.PositionDto(row = tile.row, column = tile.column) },
                    )
                },
            )
        // When
        processAbilitySelected("ability-1")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastRange
        assertThat(storedHud.abilityId).isEqualTo("ability-1")
        assertThat(storedHud.castGroupsWhereCanCast).isEqualTo(castGroupsWhereCanCast)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            ),
        )
    }

    @Test
    fun `should publish ability unavailable on cooldown and keep the hud when the ability is on cooldown`() {
        // Given
        val hud = displayMovementRange(battleUnitId = "battle-unit-1")
        battlefieldHudRepository.create(hud)
        whenever(searchAbilityAvailability("battle-unit-1"))
            .thenReturn(listOf(availability("ability-1", Status.COOLDOWN, cooldownTurnsLeft = 2)))
        // When
        processAbilitySelected("ability-1")
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.AbilityUnavailable("ability-1", BattlefieldHudEvent.AbilityUnavailable.Reason.OnCooldown(turnsLeft = 2)),
        )
    }

    @Test
    fun `should publish ability unavailable with the cost and keep the hud when the battle unit has not enough mana`() {
        // Given
        val hud = displayMovementRange(battleUnitId = "battle-unit-1")
        battlefieldHudRepository.create(hud)
        whenever(searchAbilityAvailability("battle-unit-1"))
            .thenReturn(listOf(availability("ability-1", Status.NOT_ENOUGH_MANA, cost = 10)))
        // When
        processAbilitySelected("ability-1")
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.AbilityUnavailable("ability-1", BattlefieldHudEvent.AbilityUnavailable.Reason.NotEnoughMana(cost = 10)),
        )
    }

    @Test
    fun `should publish ability unavailable and keep the hud when the battle unit has no casts left`() {
        // Given
        val hud = displayMovementRange(battleUnitId = "battle-unit-1")
        battlefieldHudRepository.create(hud)
        whenever(searchAbilityAvailability("battle-unit-1"))
            .thenReturn(listOf(availability("ability-1", Status.NO_CASTS_LEFT)))
        // When
        processAbilitySelected("ability-1")
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.AbilityUnavailable("ability-1", BattlefieldHudEvent.AbilityUnavailable.Reason.NoCastsLeft),
        )
    }

    @Test
    fun `should publish ability unavailable and keep the cast range when another ability is unavailable while displaying the ability cast range`() {
        // Given
        val hud = displayAbilityCastRange(battleUnitId = "battle-unit-1", abilityId = "ability-1")
        battlefieldHudRepository.create(hud)
        whenever(searchAbilityAvailability("battle-unit-1"))
            .thenReturn(listOf(availability("ability-2", Status.COOLDOWN, cooldownTurnsLeft = 1)))
        // When
        processAbilitySelected("ability-2")
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.AbilityUnavailable("ability-2", BattlefieldHudEvent.AbilityUnavailable.Reason.OnCooldown(turnsLeft = 1)),
        )
    }

    @Test
    fun `should deselect the ability when the same ability is selected while displaying the ability cast range`() {
        // Given
        val tilesWhereCanBeMoved =
            setOf(
                tile(0, 1),
            )
        battlefieldHudRepository.create(
            displayAbilityCastRange(
                casterTile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            ),
        )
        // When
        processAbilitySelected("ability-1")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayMovementRange
        assertThat(storedHud.tile).isEqualTo(
            tile(0, 0),
        )
        assertThat(storedHud.tilesWhereCanBeMoved).isEqualTo(tilesWhereCanBeMoved)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.AbilityDeselected(abilityId = "ability-1"),
            BattlefieldHudEvent.SelectedBattleUnit(
                tile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            ),
        )
    }

    @Test
    fun `should switch to the new ability when a different ability is selected while displaying the ability cast range`() {
        // Given
        val newCastGroupsWhereCanCast =
            listOf(
                com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto
                    .CastGroupDto(tiles = listOf(tile(2, 2))),
            )
        battlefieldHudRepository.create(
            displayAbilityCastRange(
                casterTile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
            ),
        )
        whenever(whereCanCast("battle-unit-1", "ability-2"))
            .thenReturn(
                newCastGroupsWhereCanCast.map { castGroup ->
                    WhereCanCast.CastGroup(
                        positions =
                            castGroup.tiles
                                .map { tile -> WhereCanCast.PositionDto(row = tile.row, column = tile.column) },
                    )
                },
            )
        // When
        processAbilitySelected("ability-2")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastRange
        assertThat(storedHud.abilityId).isEqualTo("ability-2")
        assertThat(storedHud.castGroupsWhereCanCast).isEqualTo(newCastGroupsWhereCanCast)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-2",
                castGroupsWhereCanCast = newCastGroupsWhereCanCast,
            ),
        )
    }

    @Test
    fun `should throw an invalid hud state error when the hud is idle`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
                .idle(),
        )
        // When
        // Then
        assertThatThrownBy { processAbilitySelected("ability-1") }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is previewing the ability cast`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
                .displayAbilityCastPreview(),
        )
        // When
        // Then
        assertThatThrownBy { processAbilitySelected("ability-1") }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw a battlefield hud not found error when no battlefield hud exists`() {
        // Given
        // When
        // Then
        assertThatThrownBy { processAbilitySelected("ability-1") }
            .isInstanceOf(BattlefieldHudError.BattlefieldHudNotFound::class.java)
    }

    private fun availability(
        abilityId: String,
        status: Status,
        cost: Int = 0,
        cooldownTurnsLeft: Int = 0,
    ) = AbilityAvailability(abilityId = abilityId, name = "Ability", cost = cost, cooldownTurnsLeft = cooldownTurnsLeft, status = status)
}
