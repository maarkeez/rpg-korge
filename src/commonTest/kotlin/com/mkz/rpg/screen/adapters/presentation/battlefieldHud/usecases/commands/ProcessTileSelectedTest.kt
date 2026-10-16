package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.castGroup
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastPreview
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.idle
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.tile
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.services.MovementService
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
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val processTileSelected =
        ProcessTileSelected(
            searchOccupant = queries.searchOccupant,
            searchBattleUnitById = queries.searchBattleUnitById,
            searchPlayerById = queries.searchPlayerById,
            searchBattle = queries.searchBattle,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = MovementService(queries.searchBattleUnitById, queries.searchTilesThatCanBeOccupied, queries.canMoveTo),
        )

    /**
     * Stores the players, the battle in the turn of the first one, and the battle units standing on the battlefield map, which walk one tile:
     * - `K` is battle-unit-1, of the [knightPlayerType] player-1.
     * - `S` is battle-unit-2, of player-1.
     * - `E` is enemy-battle-unit-1, of player-2.
     * - `F` is enemy-battle-unit-2, of player-2.
     */
    private fun givenBattle(
        map: String,
        knightPlayerType: Player.Dto.PlayerTypeDto = HUMAN,
        playerOrder: List<String> = listOf("player-1", "player-2"),
    ) {
        val player = player(id = "player-1", type = knightPlayerType).also(queries.playerRepository::create).toDto()
        val enemyPlayer = player(id = "player-2", type = CPU).also(queries.playerRepository::create).toDto()
        queries.battleRepository.create(battle(players = playerOrder))
        val battleUnits = mapOf('K' to "battle-unit-1", 'S' to "battle-unit-2", 'E' to "enemy-battle-unit-1", 'F' to "enemy-battle-unit-2")
        battleUnits.forEach { (letter, battleUnitId) ->
            queries.battleUnitRepository.create(battleUnit(id = battleUnitId, unit = unit(movementRange = 1).toDto(), player = if (letter in "EF") enemyPlayer else player))
        }
        queries.battlefieldRepository.create(battlefield(map, occupants = battleUnits.filterKeys { letter -> letter in map }))
    }

    @Test
    fun `should select the battle unit when a tile occupied by a battle unit is selected while idle`() {
        // Given
        battlefieldHudRepository.create(idle())
        givenBattle("K . #")
        // When
        processTileSelected(0, 0)
        // Then
        val tilesWhereCanBeMoved = setOf(tile(0, 1))
        val storedHud = battlefieldHudRepository.search() as DisplayMovementRange
        storedHud.battleUnitId shouldBe "battle-unit-1"
        storedHud.tile shouldBe tile(0, 0)
        storedHud.tilesWhereCanBeMoved shouldBe tilesWhereCanBeMoved
        eventBus.shouldHavePublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnit(
                tile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            ),
        )
    }

    @Test
    fun `should go idle when an empty tile is selected while idle`() {
        // Given
        battlefieldHudRepository.create(idle())
        givenBattle(". .")
        // When
        processTileSelected(0, 0)
        // Then
        battlefieldHudRepository.search().shouldBeInstanceOf<Idle>()
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should move the battle unit when a tile within the movement range is selected`() {
        // Given
        val hud = displayMovementRange(tile = tile(0, 0), battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = setOf(tile(0, 2)))
        battlefieldHudRepository.create(hud)
        givenBattle("K . .")
        // When
        processTileSelected(0, 2)
        // Then
        battlefieldHudRepository.search() shouldBe hud
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestMoveBattleUnit(
                battleUnitId = "battle-unit-1",
                moveToRow = 0,
                moveToColumn = 2,
            ),
        )
    }

    @Test
    fun `should go idle when a tile outside the movement range is selected`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(tile = tile(0, 0), battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = setOf(tile(0, 2))))
        givenBattle("K . . .")
        // When
        processTileSelected(0, 3)
        // Then
        battlefieldHudRepository.search().shouldBeInstanceOf<Idle>()
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should go idle without moving the battle unit when it belongs to a non-human player`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(tile = tile(0, 0), battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = setOf(tile(0, 2))))
        givenBattle("K . .", knightPlayerType = CPU)
        // When
        processTileSelected(0, 2)
        // Then
        battlefieldHudRepository.search().shouldBeInstanceOf<Idle>()
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should go idle when the tile is occupied by the same battle unit`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenBattle("K .")
        // When
        processTileSelected(0, 0)
        // Then
        battlefieldHudRepository.search().shouldBeInstanceOf<Idle>()
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should select the other battle unit when the tile is occupied by another battle unit`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        givenBattle("K . S .")
        // When
        processTileSelected(0, 2)
        // Then
        val tilesWhereCanBeMoved = setOf(tile(0, 1), tile(0, 3))
        val storedHud = battlefieldHudRepository.search() as DisplayMovementRange
        storedHud.battleUnitId shouldBe "battle-unit-2"
        storedHud.tile shouldBe tile(0, 2)
        storedHud.tilesWhereCanBeMoved shouldBe tilesWhereCanBeMoved
        eventBus.shouldHavePublishedEvents(
            BattlefieldHudEvent.Idle,
            BattlefieldHudEvent.SelectedBattleUnit(
                tile = tile(0, 2),
                battleUnitId = "battle-unit-2",
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            ),
        )
    }

    @Test
    fun `should do nothing when the hud is previewing the ability cast`() {
        // Given
        val hud = displayAbilityCastPreview()
        battlefieldHudRepository.create(hud)
        // When
        processTileSelected(1, 1)
        // Then
        battlefieldHudRepository.search() shouldBe hud
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should preview the self ability cast when the selected tile belongs to a cast group with no occupant`() {
        // Given
        val castGroup = castGroup(tile(0, 1))
        battlefieldHudRepository.create(
            displayAbilityCastRange(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1", castGroupsWhereCanCast = listOf(castGroup)),
        )
        givenBattle("K .")
        // When
        processTileSelected(0, 1)
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastPreview
        storedHud.castGroup shouldBe castGroup
        storedHud.enemyBattleUnitIds.shouldBeEmpty()
        eventBus.shouldHavePublishedEvents(
            BattlefieldHudEvent.SelfAbilityCastPreviewed(
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
        battlefieldHudRepository.create(
            displayAbilityCastRange(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1", castGroupsWhereCanCast = listOf(castGroup)),
        )
        givenBattle("K E")
        // When
        processTileSelected(0, 1)
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastPreview
        storedHud.castGroup shouldBe castGroup
        storedHud.enemyBattleUnitIds shouldBe listOf("enemy-battle-unit-1")
        eventBus.shouldHavePublishedEvents(
            BattlefieldHudEvent.EnemyAbilityCastPreviewed(
                casterBattleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroup = castGroup,
                enemyBattleUnitIds = listOf("enemy-battle-unit-1"),
            ),
        )
    }

    @Test
    fun `should preview the enemy ability cast on every enemy of the cast group by row and column when one of them is selected`() {
        // Given
        val castGroup = castGroup(tile(1, 1), tile(0, 1))
        battlefieldHudRepository.create(
            displayAbilityCastRange(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1", castGroupsWhereCanCast = listOf(castGroup)),
        )
        givenBattle("K F\n. E")
        // When
        processTileSelected(1, 1)
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastPreview
        storedHud.enemyBattleUnitIds shouldBe listOf("enemy-battle-unit-2", "enemy-battle-unit-1")
        eventBus.shouldHavePublishedEvents(
            BattlefieldHudEvent.EnemyAbilityCastPreviewed(
                casterBattleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroup = castGroup,
                enemyBattleUnitIds = listOf("enemy-battle-unit-2", "enemy-battle-unit-1"),
            ),
        )
    }

    @Test
    fun `should go idle when the selected tile does not belong to any cast group`() {
        // Given
        battlefieldHudRepository.create(
            displayAbilityCastRange(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1", castGroupsWhereCanCast = listOf(castGroup(tile(0, 1)))),
        )
        givenBattle("K . .")
        // When
        processTileSelected(0, 2)
        // Then
        battlefieldHudRepository.search().shouldBeInstanceOf<Idle>()
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should go idle when the caster is not the current player`() {
        // Given
        battlefieldHudRepository.create(
            displayAbilityCastRange(casterTile = tile(0, 0), battleUnitId = "battle-unit-1", abilityId = "ability-1", castGroupsWhereCanCast = listOf(castGroup(tile(0, 1)))),
        )
        givenBattle("K .", playerOrder = listOf("player-2", "player-1"))
        // When
        processTileSelected(0, 1)
        // Then
        battlefieldHudRepository.search().shouldBeInstanceOf<Idle>()
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should do nothing when no battle exists`() {
        // Given
        val hud = displayAbilityCastRange(battleUnitId = "battle-unit-1")
        battlefieldHudRepository.create(hud)
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1"))
        // When
        processTileSelected(1, 1)
        // Then
        battlefieldHudRepository.search() shouldBe hud
        eventBus.publishedEvents.shouldBeEmpty()
    }
}
