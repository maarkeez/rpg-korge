package com.mkz.tessavar.screen.adapters.presentation.selection.usecases.commands

import com.mkz.tessavar.ability.domain.Ability
import com.mkz.tessavar.ability.domain.AbilityMother.ability
import com.mkz.tessavar.battle.domain.BattleMother.battle
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.screen.adapters.presentation.selection.adapters.storage.InMemorySelectionRepository
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.AbilityInfoDisplayed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.AbilitySelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.BattleUnitSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionError
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionEvent
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.abilityInfoDisplayed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.abilitySelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.battleUnitSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.tile
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ProcessAbilitySelectedTest {
    private val queries = InMemoryQueries()
    private val selectionRepository = InMemorySelectionRepository()
    private val eventBus = FakeEventBus()
    private val processAbilitySelected =
        ProcessAbilitySelected(
            canCastAbility = queries.canCastAbility,
            whereCanCast = queries.whereCanCast,
            searchBattleUnitById = queries.searchBattleUnitById,
            searchPlayerById = queries.searchPlayerById,
            searchBattle = queries.searchBattle,
            selectionRepository = selectionRepository,
            eventBus = eventBus,
        )

    private fun givenKnightBetweenRats(
        knightAbilities: List<String>,
        currentPlayer: String = "human",
        battleState: BattleState = BattleState.IN_PROGRESS,
    ) {
        queries.abilityRepository.create(ability(id = "ability-1", manaCost = 0, cooldown = 0, targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY))
        queries.abilityRepository.create(ability(id = "ability-2", manaCost = 0, cooldown = 0, targeting = Ability.Dto.TargetingDto.CASTER))
        queries.abilityRepository.create(ability(id = "ability-3", manaCost = 10, cooldown = 0, targeting = Ability.Dto.TargetingDto.CASTER))
        val human = player(id = "human", type = HUMAN).also(queries.playerRepository::create).toDto()
        val cpu = player(id = "cpu", type = CPU).also(queries.playerRepository::create).toDto()
        val battle = battle(players = listOf(currentPlayer) + (listOf("human", "cpu") - currentPlayer), battleUnitIds = setOf("battle-unit-1", "rat-1", "rat-2"))
        when (battleState) {
            BattleState.IN_PROGRESS -> queries.battleRepository.create(battle)
            BattleState.FINISHED ->
                queries.battleRepository.create(
                    battle
                        .finish()
                        .pullEvents()
                        .second,
                )
            BattleState.NONE -> Unit
        }
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1", unit = unit(startingAbilities = knightAbilities, baseMaximumMana = 4).toDto(), player = human))
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", unit = unit(startingAbilities = knightAbilities).toDto(), player = cpu))
        queries.battleUnitRepository.create(battleUnit(id = "rat-2", player = cpu))
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
    fun `should select the ability when a battle unit is selected and the battle unit can cast`() {
        // Given
        val castGroupsWhereCanCast =
            listOf(
                com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.Dto
                    .CastGroupDto(tiles = listOf(tile(0, 1))),
                com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.Dto
                    .CastGroupDto(tiles = listOf(tile(1, 0))),
            )
        selectionRepository.create(
            battleUnitSelected(
                tile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
            ),
        )
        givenKnightBetweenRats(knightAbilities = listOf("ability-1"))
        // When
        processAbilitySelected("ability-1")
        // Then
        val storedSelection = selectionRepository.search() as AbilitySelected
        storedSelection.abilityId shouldBe "ability-1"
        storedSelection.castGroupsWhereCanCast shouldBe castGroupsWhereCanCast
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.AbilitySelected(
                casterTile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            ),
        )
    }

    @Test
    fun `ProcessAbilitySelected should display the ability info when the ability is in cooldown`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-1"))
        val knight = queries.battleUnitRepository.searchById("battle-unit-1")!!
        queries.battleUnitRepository.update(knight.castAbility(abilityId = "ability-1", abilityCooldown = 2, abilityManaCost = 0, castGroup = emptyList()))
        // When
        processAbilitySelected("ability-1")
        // Then
        val storedSelection = selectionRepository.search() as AbilityInfoDisplayed
        storedSelection.abilityId shouldBe "ability-1"
        eventBus.shouldHavePublishedEvents(SelectionEvent.AbilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1"))
    }

    @Test
    fun `ProcessAbilitySelected should display the ability info when the battle unit has not the mana`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-3"))
        // When
        processAbilitySelected("ability-3")
        // Then
        (selectionRepository.search() as AbilityInfoDisplayed).abilityId shouldBe "ability-3"
        eventBus.shouldHavePublishedEvents(SelectionEvent.AbilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-3"))
    }

    @Test
    fun `ProcessAbilitySelected should display the ability info when the battle unit belongs to the CPU`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 1), battleUnitId = "rat-1"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-1"), currentPlayer = "cpu")
        // When
        processAbilitySelected("ability-1")
        // Then
        (selectionRepository.search() as AbilityInfoDisplayed).abilityId shouldBe "ability-1"
        eventBus.shouldHavePublishedEvents(SelectionEvent.AbilityInfoDisplayed(casterTile = tile(0, 1), battleUnitId = "rat-1", abilityId = "ability-1"))
    }

    @Test
    fun `ProcessAbilitySelected should display the ability info when it is not the turn of the battle unit's player`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-1"), currentPlayer = "cpu")
        // When
        processAbilitySelected("ability-1")
        // Then
        (selectionRepository.search() as AbilityInfoDisplayed).abilityId shouldBe "ability-1"
    }

    @Test
    fun `ProcessAbilitySelected should display the ability info when there is no battle`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-1"), battleState = BattleState.NONE)
        // When
        processAbilitySelected("ability-1")
        // Then
        (selectionRepository.search() as AbilityInfoDisplayed).abilityId shouldBe "ability-1"
        eventBus.shouldHavePublishedEvents(SelectionEvent.AbilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1"))
    }

    @Test
    fun `ProcessAbilitySelected should display the ability info when the battle is finished on the human player's turn`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-1"), battleState = BattleState.FINISHED)
        // When
        processAbilitySelected("ability-1")
        // Then
        (selectionRepository.search() as AbilityInfoDisplayed).abilityId shouldBe "ability-1"
        eventBus.shouldHavePublishedEvents(SelectionEvent.AbilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1"))
    }

    @Test
    fun `ProcessAbilitySelected should hide the ability info when the ability on show is selected again`() {
        // Given
        val walkingRange = setOf(tile(1, 1))
        selectionRepository.create(abilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1", walkingRange = walkingRange))
        // When
        processAbilitySelected("ability-1")
        // Then
        (selectionRepository.search() as BattleUnitSelected).walkingRange shouldBe walkingRange
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.AbilityDeselected(abilityId = "ability-1"),
            SelectionEvent.BattleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1", walkingRange = walkingRange),
        )
    }

    @Test
    fun `ProcessAbilitySelected should display the cast range when a castable ability is selected while displaying the ability info`() {
        // Given
        selectionRepository.create(abilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-3"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-2", "ability-3"))
        // When
        processAbilitySelected("ability-2")
        // Then
        (selectionRepository.search() as AbilitySelected).abilityId shouldBe "ability-2"
    }

    @Test
    fun `should deselect the ability when the same ability is selected while displaying the ability cast range`() {
        // Given
        val walkingRange =
            setOf(
                tile(0, 1),
            )
        selectionRepository.create(
            abilitySelected(
                casterTile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                walkingRange = walkingRange,
            ),
        )
        // When
        processAbilitySelected("ability-1")
        // Then
        val storedSelection = selectionRepository.search() as BattleUnitSelected
        storedSelection.tile shouldBe tile(0, 0)
        storedSelection.walkingRange shouldBe walkingRange
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.AbilityDeselected(abilityId = "ability-1"),
            SelectionEvent.BattleUnitSelected(
                tile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                walkingRange = walkingRange,
            ),
        )
    }

    @Test
    fun `should switch to the new ability when a different ability is selected while displaying the ability cast range`() {
        // Given
        val newCastGroupsWhereCanCast =
            listOf(
                com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection.Dto
                    .CastGroupDto(tiles = listOf(tile(0, 0))),
            )
        selectionRepository.create(
            abilitySelected(
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
        val storedSelection = selectionRepository.search() as AbilitySelected
        storedSelection.abilityId shouldBe "ability-2"
        storedSelection.castGroupsWhereCanCast shouldBe newCastGroupsWhereCanCast
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.AbilitySelected(
                casterTile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-2",
                castGroupsWhereCanCast = newCastGroupsWhereCanCast,
            ),
        )
    }

    @Test
    fun `should display the ability info when a different ability in cooldown is selected while displaying the ability cast range`() {
        // Given
        val selection =
            abilitySelected(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
            )
        selectionRepository.create(selection)
        givenKnightBetweenRats(knightAbilities = listOf("ability-1", "ability-2"))
        val knight = queries.battleUnitRepository.searchById("battle-unit-1")!!
        queries.battleUnitRepository.update(knight.castAbility(abilityId = "ability-2", abilityCooldown = 2, abilityManaCost = 0, castGroup = emptyList()))
        // When
        processAbilitySelected("ability-2")
        // Then
        (selectionRepository.search() as AbilityInfoDisplayed).abilityId shouldBe "ability-2"
        eventBus.shouldHavePublishedEvents(SelectionEvent.AbilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-2"))
    }

    @Test
    fun `should throw an invalid selection state error when nothing is selected`() {
        // Given
        selectionRepository.create(
            com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother
                .nothingSelected(),
        )
        // When
        // Then
        shouldThrow<SelectionError.InvalidSelectionState> { processAbilitySelected("ability-1") }
    }

    @Test
    fun `should throw an invalid selection state error when the selection is previewing the ability cast`() {
        // Given
        selectionRepository.create(
            com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother
                .castPreviewed(),
        )
        // When
        // Then
        shouldThrow<SelectionError.InvalidSelectionState> { processAbilitySelected("ability-1") }
    }

    @Test
    fun `should throw a selection not found error when no selection exists`() {
        // Given
        // When
        // Then
        shouldThrow<SelectionError.SelectionNotFound> { processAbilitySelected("ability-1") }
    }

    private enum class BattleState { IN_PROGRESS, FINISHED, NONE }
}
