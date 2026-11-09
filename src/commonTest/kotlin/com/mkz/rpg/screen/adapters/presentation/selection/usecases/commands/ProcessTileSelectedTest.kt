package com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands

import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.selection.adapters.storage.InMemorySelectionRepository
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.BattleUnitSelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.CastPreviewed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.NothingSelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionEvent
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.abilityInfoDisplayed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.abilitySelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.battleUnitSelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.castGroup
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.castPreviewed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.nothingSelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.tile
import com.mkz.rpg.screen.adapters.presentation.selection.usecases.services.WalkingRangeService
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class ProcessTileSelectedTest {
    private val queries = InMemoryQueries()
    private val selectionRepository = InMemorySelectionRepository()
    private val eventBus = FakeEventBus()
    private val processTileSelected =
        ProcessTileSelected(
            searchOccupant = queries.searchOccupant,
            searchBattleUnitById = queries.searchBattleUnitById,
            searchPlayerById = queries.searchPlayerById,
            searchBattle = queries.searchBattle,
            selectionRepository = selectionRepository,
            eventBus = eventBus,
            walkingRangeService = WalkingRangeService(queries.searchBattleUnitById, queries.searchVacantTiles, queries.canWalkTo),
        )

    /**
     * Stores the players, the battle in the turn of the first one, and the battle units standing on the battlefield map, which walk one tile:
     * - `K` is battle-unit-1, of the [knightPlayerType] player-1.
     * - `S` is battle-unit-2, of player-1.
     * - `E` is enemy-battle-unit-1, of player-2.
     * - `F` is enemy-battle-unit-2, of player-2.
     *
     * Every battle unit is in the battle, but the [outOfBattle] ones. Without [isBattleStarted], no battle is stored, as
     * while exploring before the first one.
     */
    private fun givenBattle(
        map: String,
        knightPlayerType: Player.Dto.PlayerTypeDto = HUMAN,
        playerOrder: List<String> = listOf("player-1", "player-2"),
        outOfBattle: Set<String> = emptySet(),
        isBattleStarted: Boolean = true,
    ) {
        val player = player(id = "player-1", type = knightPlayerType).also(queries.playerRepository::create).toDto()
        val opponent = player(id = "player-2", type = CPU).also(queries.playerRepository::create).toDto()
        val battleUnits = mapOf('K' to "battle-unit-1", 'S' to "battle-unit-2", 'E' to "enemy-battle-unit-1", 'F' to "enemy-battle-unit-2")
        if (isBattleStarted) queries.battleRepository.create(battle(players = playerOrder, battleUnitIds = battleUnits.values.toSet() - outOfBattle))
        battleUnits.forEach { (letter, battleUnitId) ->
            queries.battleUnitRepository.create(battleUnit(id = battleUnitId, unit = unit(movementRange = 1).toDto(), player = if (letter in "EF") opponent else player))
        }
        queries.battlefieldRepository.create(battlefield(map, occupants = battleUnits.filterKeys { letter -> letter in map }))
    }

    @Test
    fun `ProcessTileSelected should hide the ability info when a tile is selected while it is displayed`() {
        // Given
        val walkingRange = setOf(tile(0, 1))
        selectionRepository.create(abilityInfoDisplayed(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "skull", walkingRange = walkingRange))
        givenBattle("K E")
        // When
        processTileSelected(0, 1)
        // Then
        selectionRepository.search().shouldBeInstanceOf<BattleUnitSelected>()
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.AbilityDeselected(abilityId = "skull"),
            SelectionEvent.BattleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1", walkingRange = walkingRange),
        )
    }

    @Test
    fun `should select the battle unit when a tile occupied by a battle unit is selected while nothing is selected`() {
        // Given
        selectionRepository.create(nothingSelected())
        givenBattle("K . #")
        // When
        processTileSelected(0, 0)
        // Then
        val walkingRange = setOf(tile(0, 1))
        val storedSelection = selectionRepository.search() as BattleUnitSelected
        storedSelection.battleUnitId shouldBe "battle-unit-1"
        storedSelection.tile shouldBe tile(0, 0)
        storedSelection.walkingRange shouldBe walkingRange
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.BattleUnitSelected(
                tile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                walkingRange = walkingRange,
            ),
        )
    }

    @Test
    fun `should deselect when a battle unit out of the battle is selected while nothing is selected`() {
        // Given
        selectionRepository.create(nothingSelected())
        givenBattle("K E", outOfBattle = setOf("enemy-battle-unit-1"))
        // When
        processTileSelected(0, 1)
        // Then
        selectionRepository.search().shouldBeInstanceOf<NothingSelected>()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should deselect instead of selecting a battle unit out of the battle while displaying the walking range`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1", walkingRange = setOf(tile(0, 1))))
        givenBattle("K . E", outOfBattle = setOf("enemy-battle-unit-1"))
        // When
        processTileSelected(0, 2)
        // Then
        selectionRepository.search().shouldBeInstanceOf<NothingSelected>()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should deselect when an empty tile is selected while nothing is selected`() {
        // Given
        selectionRepository.create(nothingSelected())
        givenBattle(". .")
        // When
        processTileSelected(0, 0)
        // Then
        selectionRepository.search().shouldBeInstanceOf<NothingSelected>()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should move the battle unit when a tile within the walking range is selected`() {
        // Given
        val selection = battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1", walkingRange = setOf(tile(0, 2)))
        selectionRepository.create(selection)
        givenBattle("K . .")
        // When
        processTileSelected(0, 2)
        // Then
        selectionRepository.search() shouldBe selection
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestWalkBattleUnitInBattle(
                battleUnitId = "battle-unit-1",
                walkToRow = 0,
                walkToColumn = 2,
            ),
        )
    }

    @Test
    fun `should deselect when a tile outside the walking range is selected`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1", walkingRange = setOf(tile(0, 2))))
        givenBattle("K . . .")
        // When
        processTileSelected(0, 3)
        // Then
        selectionRepository.search().shouldBeInstanceOf<NothingSelected>()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should deselect without moving the battle unit when it belongs to a non-human player`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1", walkingRange = setOf(tile(0, 2))))
        givenBattle("K . .", knightPlayerType = CPU)
        // When
        processTileSelected(0, 2)
        // Then
        selectionRepository.search().shouldBeInstanceOf<NothingSelected>()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should deselect when the tile is occupied by the same battle unit`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenBattle("K .")
        // When
        processTileSelected(0, 0)
        // Then
        selectionRepository.search().shouldBeInstanceOf<NothingSelected>()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should select the other battle unit when the tile is occupied by another battle unit`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenBattle("K . S .")
        // When
        processTileSelected(0, 2)
        // Then
        val walkingRange = setOf(tile(0, 1), tile(0, 3))
        val storedSelection = selectionRepository.search() as BattleUnitSelected
        storedSelection.battleUnitId shouldBe "battle-unit-2"
        storedSelection.tile shouldBe tile(0, 2)
        storedSelection.walkingRange shouldBe walkingRange
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.Deselected,
            SelectionEvent.BattleUnitSelected(
                tile = tile(0, 2),
                battleUnitId = "battle-unit-2",
                walkingRange = walkingRange,
            ),
        )
    }

    @Test
    fun `should do nothing when the selection is previewing the ability cast`() {
        // Given
        val selection = castPreviewed()
        selectionRepository.create(selection)
        // When
        processTileSelected(1, 1)
        // Then
        selectionRepository.search() shouldBe selection
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should preview the self ability cast when the selected tile belongs to a cast group with no occupant`() {
        // Given
        val castGroup = castGroup(tile(0, 1))
        selectionRepository.create(
            abilitySelected(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1", castGroupsWhereCanCast = listOf(castGroup)),
        )
        givenBattle("K .")
        // When
        processTileSelected(0, 1)
        // Then
        val storedSelection = selectionRepository.search() as CastPreviewed
        storedSelection.castGroup shouldBe castGroup
        storedSelection.recipientBattleUnitIds.shouldBeEmpty()
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.CastOnCasterOrVacantTilePreviewed(
                casterBattleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroup = castGroup,
            ),
        )
    }

    @Test
    fun `should preview the enemy ability cast when the selected tile is occupied by an enemy battle unit`() {
        // Given
        val castGroup = castGroup(tile(0, 1))
        selectionRepository.create(
            abilitySelected(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1", castGroupsWhereCanCast = listOf(castGroup)),
        )
        givenBattle("K E")
        // When
        processTileSelected(0, 1)
        // Then
        val storedSelection = selectionRepository.search() as CastPreviewed
        storedSelection.castGroup shouldBe castGroup
        storedSelection.recipientBattleUnitIds shouldBe listOf("enemy-battle-unit-1")
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.CastOnBattleUnitsPreviewed(
                casterBattleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroup = castGroup,
                recipientBattleUnitIds = listOf("enemy-battle-unit-1"),
            ),
        )
    }

    @Test
    fun `should preview the enemy ability cast on every enemy of the cast group by row and column when one of them is selected`() {
        // Given
        val castGroup = castGroup(tile(1, 1), tile(0, 1))
        selectionRepository.create(
            abilitySelected(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1", castGroupsWhereCanCast = listOf(castGroup)),
        )
        givenBattle("K F\n. E")
        // When
        processTileSelected(1, 1)
        // Then
        val storedSelection = selectionRepository.search() as CastPreviewed
        storedSelection.recipientBattleUnitIds shouldBe listOf("enemy-battle-unit-2", "enemy-battle-unit-1")
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.CastOnBattleUnitsPreviewed(
                casterBattleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroup = castGroup,
                recipientBattleUnitIds = listOf("enemy-battle-unit-2", "enemy-battle-unit-1"),
            ),
        )
    }

    @Test
    fun `should deselect when the selected tile does not belong to any cast group`() {
        // Given
        selectionRepository.create(
            abilitySelected(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1", castGroupsWhereCanCast = listOf(castGroup(tile(0, 1)))),
        )
        givenBattle("K . .")
        // When
        processTileSelected(0, 2)
        // Then
        selectionRepository.search().shouldBeInstanceOf<NothingSelected>()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should deselect when the caster is not the current player`() {
        // Given
        selectionRepository.create(
            abilitySelected(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1", castGroupsWhereCanCast = listOf(castGroup(tile(0, 1)))),
        )
        givenBattle("K .", playerOrder = listOf("player-2", "player-1"))
        // When
        processTileSelected(0, 1)
        // Then
        selectionRepository.search().shouldBeInstanceOf<NothingSelected>()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should do nothing when no battle exists`() {
        // Given
        val selection = abilitySelected(battleUnitId = "battle-unit-1")
        selectionRepository.create(selection)
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1"))
        // When
        processTileSelected(1, 1)
        // Then
        selectionRepository.search() shouldBe selection
        eventBus.publishedEvents.shouldBeEmpty()
    }

    // Out of the battles: the players and the battle units on the map are stored as for a battle, but the battle has finished.
    private fun givenExploring(map: String) {
        givenBattle(map)
        queries.battleRepository.create(battle(players = listOf("player-2", "player-1")).finishWithoutWinner())
    }

    @Test
    fun `should select a battle unit of the human player without any walking range when it is selected while exploring`() {
        // Given
        selectionRepository.create(nothingSelected())
        givenExploring("K . .")
        // When
        processTileSelected(0, 0)
        // Then
        val storedSelection = selectionRepository.search() as BattleUnitSelected
        storedSelection.battleUnitId shouldBe "battle-unit-1"
        storedSelection.walkingRange.shouldBeEmpty()
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.BattleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1", walkingRange = emptySet()),
        )
    }

    @Test
    fun `should select a battle unit of the human player when it is selected while exploring before any battle`() {
        // Given
        selectionRepository.create(nothingSelected())
        givenBattle("K . .", isBattleStarted = false)
        // When
        processTileSelected(0, 0)
        // Then
        (selectionRepository.search() as BattleUnitSelected).battleUnitId shouldBe "battle-unit-1"
    }

    @Test
    fun `should deselect when a cpu battle unit is selected while exploring`() {
        // Given
        selectionRepository.create(nothingSelected())
        givenExploring("K . E")
        // When
        processTileSelected(0, 2)
        // Then
        selectionRepository.search().shouldBeInstanceOf<NothingSelected>()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should deselect without moving the selected battle unit when an empty tile is selected while exploring`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1", walkingRange = setOf(tile(0, 1))))
        givenExploring("K . .")
        // When
        processTileSelected(0, 1)
        // Then
        selectionRepository.search().shouldBeInstanceOf<NothingSelected>()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should deselect when the selected battle unit is selected again while exploring`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1", walkingRange = emptySet()))
        givenExploring("K S .")
        // When
        processTileSelected(0, 0)
        // Then
        selectionRepository.search().shouldBeInstanceOf<NothingSelected>()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should select the other battle unit of the human player when it is selected while exploring`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1", walkingRange = emptySet()))
        givenExploring("K S .")
        // When
        processTileSelected(0, 1)
        // Then
        (selectionRepository.search() as BattleUnitSelected).battleUnitId shouldBe "battle-unit-2"
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.Deselected,
            SelectionEvent.BattleUnitSelected(tile = tile(0, 1), battleUnitId = "battle-unit-2", walkingRange = emptySet()),
        )
    }
}
