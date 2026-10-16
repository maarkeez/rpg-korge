package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TARGET
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.AbilityCastResolved
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDeployed
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.EffectReceived
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.OnTurnStartedEffectApplied
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestMoveBattleUnit
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldCreated
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.screen.adapters.presentation.FakeBattlefieldView.BattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.FakeBattlefieldView.MovedBattleUnit
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.CastGroupDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BattlefieldPresenterTest {
    // The battle has started with the human player turn. The human knight-1 stands at (1,1) and can walk 3 tiles,
    // and the cpu rat-1 and rat-2 stand at (1,3) and (0,3), on a 4x4 sand battlefield. The knight can cast the mushroom, dealing 7 damage.
    private val apis =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            units = listOf(unit(id = "knight", movementRange = 3).toDto(), unit(id = "rat").toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            effects = listOf(decreaseHealthEffect(id = "poison", damage = 7, applicationType = "IMMEDIATELY").toDto()),
            abilities = listOf(ability(id = "mushroom", cost = 10, effectSpecs = listOf(effectSpec(effectId = "poison", target = SELECTED_TARGET)), targeting = ALL_ADJACENT_ENEMIES).toDto()),
            tiles = List(4) { List(4) { "sand" } },
            deployments =
                listOf(
                    Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1),
                    Deployment(battleUnitId = "rat-1", unitId = "rat", playerId = "cpu", row = 1, column = 3),
                    Deployment(battleUnitId = "rat-2", unitId = "rat", playerId = "cpu", row = 0, column = 3),
                ),
            playerOrder = listOf("human", "cpu"),
        ).also(SubdomainApis::initialise)
    private val eventBus = apis.eventBus
    private val battlefieldView = FakeBattlefieldView()
    private val battleUnitInfoView = FakeBattleUnitInfoView()
    private val attackPreviewView = FakeAttackPreviewView()
    private val timeline = battleSceneTimeline()

    // The presenter is created once the battle has started, so it only reacts to what the test publishes.
    private val battlefieldPresenter =
        BattlefieldPresenter(
            battlefieldView = battlefieldView,
            battleUnitInfoView = battleUnitInfoView,
            attackPreviewView = attackPreviewView,
            playerCallToActionView = FakePlayerCallToActionView(),
            battleHudView = FakeBattleHudView(),
            battlefieldApi = apis.battlefieldApi,
            battleUnitApi = apis.battleUnitApi,
            playerApi = apis.playerApi,
            unitApi = apis.unitApi,
            abilityApi = apis.abilityApi,
            battleApi = apis.battleApi,
            timeline = timeline,
            eventBus = eventBus,
        )

    private fun position(
        row: Int,
        column: Int,
    ) = PositionDto(row = row, column = column)

    private fun publish(event: DomainEvent) {
        eventBus.publish(event)
        eventBus.dispatch()
    }

    @Test
    fun `DisplayBattlefield should display battlefield on view when battlefield is displayed`() {
        // Given
        // When
        battlefieldPresenter.displayBattlefield()
        // Then
        battlefieldView.displayedBattlefields.shouldContainExactly(apis.battlefieldApi.searchBattlefield()!!)
    }

    @Test
    fun `DisplayBattlefield should display battlefield on view when battlefield is created`() {
        // Given
        eventBus.publish(BattlefieldCreated)
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.displayedBattlefields.shouldContainExactly(apis.battlefieldApi.searchBattlefield()!!)
    }

    @Test
    fun `DisplayUnit should display knight battle unit on view when knight battle unit is displayed`() {
        // Given
        // When
        battlefieldPresenter.displayUnit(row = 1, column = 2, battleUnitId = "knight-1")
        // Then
        battlefieldView.displayedBattleUnits.shouldContainExactly(BattleUnitAt(row = 1, column = 2, unitId = "knight"))
    }

    @Test
    fun `DisplayUnit should display rat battle unit on view when rat battle unit is displayed`() {
        // Given
        // When
        battlefieldPresenter.displayUnit(row = 1, column = 2, battleUnitId = "rat-1")
        // Then
        battlefieldView.displayedBattleUnits.shouldContainExactly(BattleUnitAt(row = 1, column = 2, unitId = "rat"))
    }

    @Test
    fun `DisplayUnit should not display battle unit on view when battle unit is not found`() {
        // Given
        // When
        battlefieldPresenter.displayUnit(row = 1, column = 2, battleUnitId = "unknown-battle-unit")
        // Then
        battlefieldView.displayedBattleUnits.shouldBeEmpty()
    }

    @Test
    fun `RemoveUnit should remove battle unit from view when battle unit is removed`() {
        // Given
        // When
        battlefieldPresenter.removeUnit(row = 1, column = 2)
        // Then
        battlefieldView.removedBattleUnits.shouldContainExactly(position(1, 2))
    }

    @Test
    fun `DisplayUnitInOrder should display the deployed battle unit once the previous steps have been played`() {
        // Given
        val previousStep = timeline.playLongStep()
        publish(BattleUnitDeployed(battleUnitId = "knight-1", row = 2, column = 2))
        battlefieldView.displayedBattleUnits.shouldBeEmpty()
        // When
        previousStep.complete(Unit)
        // Then
        battlefieldView.displayedBattleUnits.shouldContainExactly(BattleUnitAt(row = 2, column = 2, unitId = "knight"))
    }

    @Test
    fun `MoveUnit should move the battle unit along its path on view when the battle unit is moved`() {
        // Given
        val path = listOf(position(2, 3), position(3, 3))
        eventBus.publish(BattleUnitMoved(battleUnitId = "rat-1", fromRow = 1, fromColumn = 3, toRow = 3, toColumn = 3, path = path))
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.movedBattleUnits.shouldContainExactly(MovedBattleUnit(fromRow = 1, fromColumn = 3, path = path, unitId = "rat"))
    }

    @Test
    fun `MoveUnit should not move the battle unit on view when the battle unit is not found`() {
        // Given
        val path = listOf(position(3, 0))
        eventBus.publish(BattleUnitMoved(battleUnitId = "unknown-battle-unit", fromRow = 2, fromColumn = 0, toRow = 3, toColumn = 0, path = path))
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.movedBattleUnits.shouldBeEmpty()
    }

    @Test
    fun `FlashUnit should flash the battle unit on view when it receives an effect`() {
        // Given
        eventBus.publish(EffectReceived(battleUnitId = "knight-1", effectId = "lava-damage"))
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.flashedBattleUnits.shouldContainExactly(BattleUnitAt(row = 1, column = 1, unitId = "knight"))
    }

    @Test
    fun `FlashUnit should flash the battle unit on view when an effect is applied at the start of its turn`() {
        // Given
        eventBus.publish(OnTurnStartedEffectApplied(battleUnitId = "knight-1", effectId = "venom-damage"))
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.flashedBattleUnits.shouldContainExactly(BattleUnitAt(row = 1, column = 1, unitId = "knight"))
    }

    @Test
    fun `FlashUnit should not flash the battle unit on view when it is not on the battlefield`() {
        // Given
        apis.battlefieldApi.removeOccupant("rat-1")
        eventBus.dispatch()
        eventBus.publish(EffectReceived(battleUnitId = "rat-1", effectId = "lava-damage"))
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.flashedBattleUnits.shouldBeEmpty()
    }

    @Test
    fun `FlashUnit should flash the battle unit once the previous steps have been played`() {
        // Given
        val previousStep = timeline.playLongStep()
        publish(EffectReceived(battleUnitId = "knight-1", effectId = "lava-damage"))
        battlefieldView.flashedBattleUnits.shouldBeEmpty()
        // When
        previousStep.complete(Unit)
        // Then
        battlefieldView.flashedBattleUnits.shouldContainExactly(BattleUnitAt(row = 1, column = 1, unitId = "knight"))
    }

    @Test
    fun `DisplayLoot should display the dropped item on its tile when an item is dropped on the battlefield`() {
        // Given
        apis.battlefieldApi.placeDroppedItem(lootableItemId = "health-potion", row = 2, column = 2)
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.displayedLoot shouldBe mapOf(position(2, 2) to listOf("health-potion"))
    }

    @Test
    fun `DisplayLoot should display every item lying on the tile when several items are dropped on it`() {
        // Given
        apis.battlefieldApi.placeDroppedItem(lootableItemId = "health-potion", row = 2, column = 2)
        apis.battlefieldApi.placeDroppedItem(lootableItemId = "hammer", row = 2, column = 2)
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.displayedLoot shouldBe mapOf(position(2, 2) to listOf("health-potion", "hammer"))
    }

    @Test
    fun `DisplayLoot should display the items left on the tile when a dropped item is picked up`() {
        // Given
        apis.battlefieldApi.placeDroppedItem(lootableItemId = "health-potion", row = 2, column = 2)
        apis.battlefieldApi.placeDroppedItem(lootableItemId = "hammer", row = 2, column = 2)
        eventBus.dispatch()
        apis.battlefieldApi.pickUpDroppedItem(lootableItemId = "health-potion", row = 2, column = 2)
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.displayedLoot shouldBe mapOf(position(2, 2) to listOf("hammer"))
    }

    @Test
    fun `DisplayLoot should display the dropped item once the previous steps have been played`() {
        // Given
        val previousStep = timeline.playLongStep()
        apis.battlefieldApi.placeDroppedItem(lootableItemId = "health-potion", row = 2, column = 2)
        eventBus.dispatch()
        require(battlefieldView.displayedLoot.isEmpty())
        // When
        previousStep.complete(Unit)
        // Then
        battlefieldView.displayedLoot shouldBe mapOf(position(2, 2) to listOf("health-potion"))
    }

    @Test
    fun `DimTiles should dim every tile but the movement range and the selected battle unit when a battle unit is selected`() {
        // Given
        eventBus.publish(
            BattlefieldHudEvent.SelectedBattleUnit(
                tile = TileDto(row = 1, column = 1),
                battleUnitId = "knight-1",
                tilesWhereCanBeMoved = setOf(TileDto(row = 1, column = 2), TileDto(row = 2, column = 1)),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.tilesNotDimmed.shouldContainExactly(listOf(setOf(position(1, 2), position(2, 1), position(1, 1))))
    }

    @Test
    fun `DimTiles should dim every tile but the cast tiles and the caster when an ability is selected`() {
        // Given
        eventBus.publish(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile = TileDto(row = 1, column = 1),
                battleUnitId = "knight-1",
                abilityId = "sword",
                castGroupsWhereCanCast =
                    listOf(
                        CastGroupDto(tiles = listOf(TileDto(row = 0, column = 1))),
                        CastGroupDto(tiles = listOf(TileDto(row = 1, column = 0))),
                    ),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.tilesNotDimmed.shouldContainExactly(listOf(setOf(position(0, 1), position(1, 0), position(1, 1))))
    }

    @Test
    fun `DimTiles should reset the tiles when the battlefield hud is idle`() {
        // Given
        eventBus.publish(BattlefieldHudEvent.Idle)
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.resetTilesCount shouldBe 1
    }

    // The player selects the knight-1, so its movement range is displayed.
    private fun selectKnight() {
        battlefieldPresenter.tileSelected(row = 1, column = 1)
        eventBus.dispatch()
    }

    @Test
    fun `RefreshSelectedBattleUnit should display the new paths of the selected battle unit when a tile is vacated`() {
        // Given
        selectKnight()
        battlefieldView.potentialMovements shouldNotContain position(1, 3)
        apis.battlefieldApi.removeOccupant("rat-1")
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.potentialMovements shouldContain position(1, 3)
    }

    @Test
    fun `RefreshSelectedBattleUnit should stop displaying the closed paths of the selected battle unit when a tile is occupied`() {
        // Given
        selectKnight()
        battlefieldView.tilesNotDimmed.last() shouldContain position(1, 2)
        apis.battlefieldApi.updateBattlefieldOccupancy(row = 1, column = 2, battleUnitId = "bee-1")
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.tilesNotDimmed.last() shouldNotContain position(1, 2)
    }

    @Test
    fun `RefreshSelectedBattleUnit should display the selected battle unit again when its cast has been resolved`() {
        // Given
        selectKnight()
        eventBus.publish(AbilityCastResolved(battleUnitId = "knight-1", abilityId = "heal"))
        // When
        eventBus.dispatch()
        // Then
        battleUnitInfoView.displayedBattleUnits.shouldHaveSize(2)
    }

    @Test
    fun `RefreshSelectedBattleUnit should not move the previously selected battle unit when a turn starts and an empty tile is selected`() {
        // Given
        selectKnight()
        val requestedMoves = mutableListOf<RequestMoveBattleUnit>()
        eventBus.subscribe<RequestMoveBattleUnit> { event -> requestedMoves += event }
        publish(PlayerTurnStarted(playerId = "human"))
        // When
        battlefieldPresenter.tileSelected(row = 1, column = 2)
        eventBus.dispatch()
        // Then
        requestedMoves.shouldBeEmpty()
    }

    @Test
    fun `TileSelected should process the selected tile when the timeline is idle`() {
        // Given
        val selections = mutableListOf<BattlefieldHudEvent.SelectedBattleUnit>()
        eventBus.subscribe<BattlefieldHudEvent.SelectedBattleUnit> { event -> selections += event }
        // When
        battlefieldPresenter.tileSelected(row = 1, column = 1)
        eventBus.dispatch()
        // Then
        selections.map { selection -> selection.battleUnitId }.shouldContainExactly("knight-1")
    }

    @Test
    fun `TileSelected should ignore the selected tile when the timeline is playing`() {
        // Given
        val selections = mutableListOf<BattlefieldHudEvent.SelectedBattleUnit>()
        eventBus.subscribe<BattlefieldHudEvent.SelectedBattleUnit> { event -> selections += event }
        timeline.playLongStep()
        // When
        battlefieldPresenter.tileSelected(row = 1, column = 1)
        eventBus.dispatch()
        // Then
        selections.shouldBeEmpty()
    }

    @Test
    fun `DisplayEnemyAbilityCastPreview should preview every enemy receiving the cast in order when an enemy ability cast is previewed`() {
        // Given
        eventBus.publish(
            BattlefieldHudEvent.EnemyAbilityCastPreviewed(
                casterBattleUnitId = "knight-1",
                abilityId = "mushroom",
                castGroup = CastGroupDto(tiles = listOf(TileDto(row = 1, column = 3), TileDto(row = 0, column = 3))),
                enemyBattleUnitIds = listOf("rat-1", "rat-2"),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        val preview = attackPreviewView.displayedAttackPreviews.single()
        preview.casterBattleUnit.id shouldBe "knight-1"
        preview.manaCost shouldBe 10
        preview.receivers.map { receiver -> receiver.battleUnit.id }.shouldContainExactly("rat-1", "rat-2")
        preview.receivers.map { receiver -> receiver.unit.id }.shouldContainExactly("rat", "rat")
        preview.receivers.map { receiver -> receiver.damage }.shouldContainExactly(7, 7)
    }
}
