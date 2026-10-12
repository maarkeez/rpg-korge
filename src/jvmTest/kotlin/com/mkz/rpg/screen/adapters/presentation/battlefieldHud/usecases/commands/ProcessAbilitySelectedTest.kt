package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudError
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.tile
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ProcessAbilitySelectedTest {
    private val queries = InMemoryQueries()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val processAbilitySelected =
        ProcessAbilitySelected(
            canCastAbility = queries.canCastAbility,
            whereCanCast = queries.whereCanCast,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )

    // Stores the knight, that knows the given abilities, at (0,0), with a rat on each side: the sword hits one of them, the heal targets the knight.
    private fun givenKnightBetweenRats(knightAbilities: List<String>) {
        queries.abilityRepository.create(ability(id = "ability-1", cost = 0, cooldown = 0, targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY))
        queries.abilityRepository.create(ability(id = "ability-2", cost = 0, cooldown = 0, targeting = Ability.Dto.TargetingDto.SELF))
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1", unit = unit(abilities = knightAbilities).toDto(), player = player(id = "human").toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = player(id = "cpu").toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-2", player = player(id = "cpu").toDto()))
        queries.battlefieldRepository.create(
            battlefield(
                """
                K R
                S .
                """,
                occupants = mapOf('K' to "battle-unit-1", 'R' to "rat-1", 'S' to "rat-2"),
            ),
        )
    }

    @Test
    fun `should select the ability when the hud is displaying the movement range and the battle unit can cast`() {
        // Given
        val castGroupsWhereCanCast =
            listOf(
                com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto
                    .CastGroupDto(tiles = listOf(tile(0, 1))),
                com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto
                    .CastGroupDto(tiles = listOf(tile(1, 0))),
            )
        battlefieldHudRepository.create(
            displayMovementRange(
                tile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
            ),
        )
        givenKnightBetweenRats(knightAbilities = listOf("ability-1"))
        // When
        processAbilitySelected("ability-1")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastRange
        storedHud.abilityId shouldBe "ability-1"
        storedHud.castGroupsWhereCanCast shouldBe castGroupsWhereCanCast
        eventBus.shouldHavePublishedEvents(
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
    fun `should not select the ability when the battle unit cannot cast`() {
        // Given
        val hud =
            displayMovementRange(battleUnitId = "battle-unit-1")
        battlefieldHudRepository.create(hud)
        // The knight does not know the ability.
        givenKnightBetweenRats(knightAbilities = listOf("ability-2"))
        // When
        processAbilitySelected("ability-1")
        // Then
        battlefieldHudRepository.search() shouldBe hud
        eventBus.publishedEvents.shouldBeEmpty()
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
        storedHud.tile shouldBe tile(0, 0)
        storedHud.tilesWhereCanBeMoved shouldBe tilesWhereCanBeMoved
        eventBus.shouldHavePublishedEvents(
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
                com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto
                    .CastGroupDto(tiles = listOf(tile(0, 0))),
            )
        battlefieldHudRepository.create(
            displayAbilityCastRange(
                casterTile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
            ),
        )
        givenKnightBetweenRats(knightAbilities = listOf("ability-1", "ability-2"))
        // When
        processAbilitySelected("ability-2")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastRange
        storedHud.abilityId shouldBe "ability-2"
        storedHud.castGroupsWhereCanCast shouldBe newCastGroupsWhereCanCast
        eventBus.shouldHavePublishedEvents(
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
            com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother
                .idle(),
        )
        // When
        // Then
        shouldThrow<BattlefieldHudError.InvalidBattlefieldHudState> { processAbilitySelected("ability-1") }
    }

    @Test
    fun `should throw an invalid hud state error when the hud is previewing the ability cast`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother
                .displayAbilityCastPreview(),
        )
        // When
        // Then
        shouldThrow<BattlefieldHudError.InvalidBattlefieldHudState> { processAbilitySelected("ability-1") }
    }

    @Test
    fun `should throw a battlefield hud not found error when no battlefield hud exists`() {
        // Given
        // When
        // Then
        shouldThrow<BattlefieldHudError.BattlefieldHudNotFound> { processAbilitySelected("ability-1") }
    }
}
