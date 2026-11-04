package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayAbilityInfo
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudError
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityInfo
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.tile
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ProcessAbilitySelectedTest {
    private val queries = InMemoryQueries()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val processAbilitySelected =
        ProcessAbilitySelected(
            canCastAbility = queries.canCastAbility,
            whereCanCast = queries.whereCanCast,
            searchBattleUnitById = queries.searchBattleUnitById,
            searchPlayerById = queries.searchPlayerById,
            searchBattle = queries.searchBattle,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )

    /**
     * Stores the knight of the human player, that knows the given abilities and has 4 mana, at (0,0), with a rat of the CPU
     * on each side, in the battle in the [currentPlayer]'s turn: ability-1 hits one of them, ability-2 targets the knight,
     * and ability-3 costs 10 mana. The battle is in progress, finished on that turn, or none at all, as [battleState] says.
     */
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
                        .defeatPlayer("cpu")
                        .finishBattle()
                        .pullEvents()
                        .second,
                )
            BattleState.NONE -> Unit
        }
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1", unit = unit(abilities = knightAbilities, manaPoints = 4).toDto(), player = human))
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", unit = unit(abilities = knightAbilities).toDto(), player = cpu))
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
    fun `ProcessAbilitySelected should display the ability info when the ability is in cooldown`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-1"))
        val knight = queries.battleUnitRepository.searchById("battle-unit-1")!!
        queries.battleUnitRepository.update(knight.castAbility(abilityId = "ability-1", abilityCooldown = 2, abilityManaCost = 0, castGroup = emptyList()))
        // When
        processAbilitySelected("ability-1")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityInfo
        storedHud.abilityId shouldBe "ability-1"
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.AbilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1"))
    }

    @Test
    fun `ProcessAbilitySelected should display the ability info when the battle unit has not the mana`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-3"))
        // When
        processAbilitySelected("ability-3")
        // Then
        (battlefieldHudRepository.search() as DisplayAbilityInfo).abilityId shouldBe "ability-3"
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.AbilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-3"))
    }

    @Test
    fun `ProcessAbilitySelected should display the ability info when the battle unit belongs to the CPU`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(tile = tile(0, 1), battleUnitId = "rat-1"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-1"), currentPlayer = "cpu")
        // When
        processAbilitySelected("ability-1")
        // Then
        (battlefieldHudRepository.search() as DisplayAbilityInfo).abilityId shouldBe "ability-1"
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.AbilityInfoDisplayed(casterTile = tile(0, 1), battleUnitId = "rat-1", abilityId = "ability-1"))
    }

    @Test
    fun `ProcessAbilitySelected should display the ability info when it is not the turn of the battle unit's player`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-1"), currentPlayer = "cpu")
        // When
        processAbilitySelected("ability-1")
        // Then
        (battlefieldHudRepository.search() as DisplayAbilityInfo).abilityId shouldBe "ability-1"
    }

    @Test
    fun `ProcessAbilitySelected should display the ability info when there is no battle`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-1"), battleState = BattleState.NONE)
        // When
        processAbilitySelected("ability-1")
        // Then
        (battlefieldHudRepository.search() as DisplayAbilityInfo).abilityId shouldBe "ability-1"
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.AbilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1"))
    }

    @Test
    fun `ProcessAbilitySelected should display the ability info when the battle is finished on the human player's turn`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-1"), battleState = BattleState.FINISHED)
        // When
        processAbilitySelected("ability-1")
        // Then
        (battlefieldHudRepository.search() as DisplayAbilityInfo).abilityId shouldBe "ability-1"
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.AbilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1"))
    }

    @Test
    fun `ProcessAbilitySelected should hide the ability info when the ability on show is selected again`() {
        // Given
        val tilesWhereCanBeMoved = setOf(tile(1, 1))
        battlefieldHudRepository.create(displayAbilityInfo(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1", tilesWhereCanBeMoved = tilesWhereCanBeMoved))
        // When
        processAbilitySelected("ability-1")
        // Then
        (battlefieldHudRepository.search() as DisplayMovementRange).tilesWhereCanBeMoved shouldBe tilesWhereCanBeMoved
        eventBus.shouldHavePublishedEvents(
            BattlefieldHudEvent.AbilityDeselected(abilityId = "ability-1"),
            BattlefieldHudEvent.SelectedBattleUnit(tile = tile(0, 0), battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = tilesWhereCanBeMoved),
        )
    }

    @Test
    fun `ProcessAbilitySelected should display the cast range when a castable ability is selected while displaying the ability info`() {
        // Given
        battlefieldHudRepository.create(displayAbilityInfo(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-3"))
        givenKnightBetweenRats(knightAbilities = listOf("ability-2", "ability-3"))
        // When
        processAbilitySelected("ability-2")
        // Then
        (battlefieldHudRepository.search() as DisplayAbilityCastRange).abilityId shouldBe "ability-2"
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
    fun `should display the ability info when a different ability in cooldown is selected while displaying the ability cast range`() {
        // Given
        val hud =
            displayAbilityCastRange(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
            )
        battlefieldHudRepository.create(hud)
        givenKnightBetweenRats(knightAbilities = listOf("ability-1", "ability-2"))
        val knight = queries.battleUnitRepository.searchById("battle-unit-1")!!
        queries.battleUnitRepository.update(knight.castAbility(abilityId = "ability-2", abilityCooldown = 2, abilityManaCost = 0, castGroup = emptyList()))
        // When
        processAbilitySelected("ability-2")
        // Then
        (battlefieldHudRepository.search() as DisplayAbilityInfo).abilityId shouldBe "ability-2"
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.AbilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-2"))
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

    private enum class BattleState { IN_PROGRESS, FINISHED, NONE }
}
