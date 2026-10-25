package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.CASTER
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TARGET
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TILE
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.SELF
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_SELF
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.battle.domain.BattleEvent.BattleFinished
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.AbilityCastResolved
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDeployed
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitRespawned
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitVanished
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.EffectReceived
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.OnTurnStartedEffectApplied
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestMoveBattleUnit
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldCreated
import com.mkz.rpg.building.domain.BuildingEvent.BuildingPlaced
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.deployBattleUnitEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.domain.NpcEvent
import com.mkz.rpg.npc.domain.NpcEvent.NpcPlaced
import com.mkz.rpg.npc.domain.NpcMother.npc
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.screen.adapters.presentation.BattlefieldHealthBarPreviewView.PreviewOption
import com.mkz.rpg.screen.adapters.presentation.FakeBattlefieldView.BattleUnitAt
import com.mkz.rpg.screen.adapters.presentation.FakeBattlefieldView.BuildingAt
import com.mkz.rpg.screen.adapters.presentation.FakeBattlefieldView.MovedBattleUnit
import com.mkz.rpg.screen.adapters.presentation.FakeBattlefieldView.NpcAt
import com.mkz.rpg.screen.adapters.presentation.FakeBattlefieldView.PickedUpLoot
import com.mkz.rpg.screen.adapters.presentation.FakeBattlefieldView.SwappedBattleUnits
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.CastGroupDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test

class BattlefieldPresenterTest {
    // The battle has started with the human player turn. The human knight-1, with 30 health points, stands at (1,1) and can walk 3 tiles,
    // and the cpu rat-1 and rat-2, with 21 health points, stand at (1,3) and (0,3), on a 4x4 sand battlefield. The knight
    // can cast the mushroom, dealing 7 damage and leaving a venom, the heal, healing itself 5, and the bee, deploying a rat.
    // A health potion, healing 5, can be looted. The knight explored before the battle, so it has an inventory.
    private val apis =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            units = listOf(unit(id = "knight", movementRange = 3, healthPoints = 30).toDto(), unit(id = "rat", healthPoints = 21).toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            // The rats drop nothing, so the items lying on the battlefield are only the ones the test places, as dropped by a rat.
            lootTables = listOf(lootTable(id = "rat-loot", entries = listOf(nothingEntry())).toDto()),
            lootDrops = listOf(lootDrop(unitId = "rat", lootTableIds = listOf("rat-loot")).toDto()),
            effects =
                listOf(
                    decreaseHealthEffect(id = "poison", damage = 7, applicationType = "IMMEDIATELY").toDto(),
                    decreaseHealthEffect(id = "venom", damage = 3, applicationType = "ON_TURN_STARTED").toDto(),
                    increaseHealthEffect(id = "cure", healing = 5).toDto(),
                    deployBattleUnitEffect(id = "summon", unitId = "rat").toDto(),
                ),
            abilities =
                listOf(
                    ability(
                        id = "mushroom",
                        cost = 10,
                        effectSpecs = listOf(effectSpec(effectId = "poison", target = SELECTED_TARGET), effectSpec(effectId = "venom", target = SELECTED_TARGET)),
                        targeting = ALL_ADJACENT_ENEMIES,
                    ).toDto(),
                    ability(id = "heal", cost = 10, effectSpecs = listOf(effectSpec(effectId = "cure", target = CASTER)), targeting = SELF).toDto(),
                    ability(id = "bee", cost = 10, effectSpecs = listOf(effectSpec(effectId = "summon", target = SELECTED_TILE)), targeting = VACANT_TILE_ADJACENT_TO_SELF, turnsDuration = 5).toDto(),
                ),
            lootableItems = listOf(consumableLootableItem(id = "health-potion", consumable = consumable(effectIds = setOf("cure"))).toDto()),
            tiles = List(4) { List(4) { "sand" } },
            deployments =
                listOf(
                    Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1),
                    Deployment(battleUnitId = "rat-1", unitId = "rat", playerId = "cpu", row = 1, column = 3),
                    Deployment(battleUnitId = "rat-2", unitId = "rat", playerId = "cpu", row = 0, column = 3),
                ),
            playerOrder = listOf("human", "cpu"),
            explore = true,
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
            partyApi = apis.partyApi,
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
        publish(BattleUnitDeployed(playerId = "human", battleUnitId = "knight-1", row = 2, column = 2))
        battlefieldView.displayedBattleUnits.shouldBeEmpty()
        // When
        previousStep.complete(Unit)
        // Then
        battlefieldView.displayedBattleUnits.shouldContainExactly(BattleUnitAt(row = 2, column = 2, unitId = "knight"))
    }

    @Test
    fun `DisplayUnitInOrder should display the respawned battle unit once the previous steps have been played`() {
        // Given
        val previousStep = timeline.playLongStep()
        publish(BattleUnitRespawned(playerId = "cpu", battleUnitId = "rat-1", row = 2, column = 3))
        battlefieldView.displayedBattleUnits.shouldBeEmpty()
        // When
        previousStep.complete(Unit)
        // Then
        battlefieldView.displayedBattleUnits.shouldContainExactly(BattleUnitAt(row = 2, column = 3, unitId = "rat"))
    }

    @Test
    fun `DisplayBuilding should display the placed building once the previous steps have been played`() {
        // Given
        val previousStep = timeline.playLongStep()
        publish(BuildingPlaced(buildingId = "fortress", row = 2, column = 2))
        battlefieldView.displayedBuildings.shouldBeEmpty()
        // When
        previousStep.complete(Unit)
        // Then
        battlefieldView.displayedBuildings.shouldContainExactly(BuildingAt(row = 2, column = 2, buildingId = "fortress"))
    }

    @Test
    fun `DisplayNpc should display the placed npc once the previous steps have been played`() {
        // Given
        val previousStep = timeline.playLongStep()
        publish(NpcPlaced(npcId = "the-guy", row = 2, column = 2))
        battlefieldView.displayedNpcs.shouldBeEmpty()
        // When
        previousStep.complete(Unit)
        // Then
        battlefieldView.displayedNpcs.shouldContainExactly(NpcAt(row = 2, column = 2, npcId = "the-guy"))
    }

    @Test
    fun `DisplayNpc should display a placed recruitable npc with its unit`() {
        // When
        publish(NpcPlaced(npcId = "viking", row = 2, column = 2, unitId = "viking"))
        // Then
        battlefieldView.displayedNpcs.shouldContainExactly(NpcAt(row = 2, column = 2, npcId = "viking", unitId = "viking"))
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
    fun `MoveUnit should move the battle unit at the battle pace when battling`() {
        // Given
        eventBus.publish(BattleUnitMoved(battleUnitId = "rat-1", fromRow = 1, fromColumn = 3, toRow = 2, toColumn = 3, path = listOf(position(2, 3))))
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.moveStepDurations.shouldContainExactly(BattlefieldView.MOVE_STEP_DURATION)
    }

    @Test
    fun `MoveUnit should center the battle unit when it moves in a battle`() {
        // Given
        eventBus.publish(BattleUnitMoved(battleUnitId = "rat-1", fromRow = 1, fromColumn = 3, toRow = 2, toColumn = 3, path = listOf(position(2, 3))))
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.moveFollowings.shouldContainExactly(BattlefieldView.Following.CENTER)
    }

    // The same battlefield, before any battle has started: the human player explores it.
    private fun exploringApis(
        deployments: List<Deployment> = listOf(Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1)),
        npcs: List<Npc.Dto> = emptyList(),
    ) = SubdomainApis(
        terrains = listOf(occupiableTerrain(id = "sand").toDto()),
        units = listOf(unit(id = "knight", movementRange = 3).toDto(), unit(id = "rat", movementRange = 2).toDto()),
        players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
        tiles = List(4) { List(4) { "sand" } },
        deployments = deployments,
        npcs = npcs,
    ).also(SubdomainApis::initialise)

    private fun exploringPresenter(
        apis: SubdomainApis,
        battlefieldView: FakeBattlefieldView,
        timeline: BattleSceneTimeline = battleSceneTimeline(),
    ) = BattlefieldPresenter(
        battlefieldView = battlefieldView,
        battleUnitInfoView = FakeBattleUnitInfoView(),
        attackPreviewView = FakeAttackPreviewView(),
        playerCallToActionView = FakePlayerCallToActionView(),
        battleHudView = FakeBattleHudView(),
        battlefieldApi = apis.battlefieldApi,
        battleUnitApi = apis.battleUnitApi,
        playerApi = apis.playerApi,
        unitApi = apis.unitApi,
        abilityApi = apis.abilityApi,
        battleApi = apis.battleApi,
        partyApi = apis.partyApi,
        timeline = timeline,
        eventBus = apis.eventBus,
    )

    @Test
    fun `MoveUnit should move the battle unit quicker when exploring`() {
        // Given
        val exploringApis = exploringApis()
        val exploringBattlefieldView = FakeBattlefieldView()
        exploringPresenter(exploringApis, exploringBattlefieldView)
        exploringApis.eventBus.publish(BattleUnitMoved(battleUnitId = "knight-1", fromRow = 1, fromColumn = 1, toRow = 0, toColumn = 1, path = listOf(position(0, 1))))
        // When
        exploringApis.eventBus.dispatch()
        // Then
        exploringBattlefieldView.moveStepDurations.shouldContainExactly(BattlefieldView.EXPLORING_STEP_DURATION)
    }

    @Test
    fun `MoveUnit should trail the leader of the party when it walks while exploring`() {
        // Given
        val exploringApis = exploringApis(deployments = partyOfTwoKnights)
        val exploringBattlefieldView = FakeBattlefieldView()
        exploringPresenter(exploringApis, exploringBattlefieldView)
        exploringApis.partyApi.startExploration()
        exploringApis.eventBus.dispatch()
        val path = listOf(position(0, 1))
        exploringApis.eventBus.publish(BattleUnitMoved(battleUnitId = "knight-1", fromRow = 1, fromColumn = 1, toRow = 0, toColumn = 1, path = path))
        // When
        exploringApis.eventBus.dispatch()
        // Then
        exploringBattlefieldView.movedBattleUnits.shouldContainExactly(MovedBattleUnit(fromRow = 1, fromColumn = 1, path = path, unitId = "knight"))
        exploringBattlefieldView.moveFollowings.shouldContainExactly(BattlefieldView.Following.TRAIL)
    }

    @Test
    fun `MoveUnit should not follow a follower of the party when it walks while exploring`() {
        // Given
        val exploringApis = exploringApis(deployments = partyOfTwoKnights)
        val exploringBattlefieldView = FakeBattlefieldView()
        exploringPresenter(exploringApis, exploringBattlefieldView)
        exploringApis.partyApi.startExploration()
        exploringApis.eventBus.dispatch()
        val path = listOf(position(2, 0))
        exploringApis.eventBus.publish(BattleUnitMoved(battleUnitId = "knight-2", fromRow = 2, fromColumn = 1, toRow = 2, toColumn = 0, path = path))
        // When
        exploringApis.eventBus.dispatch()
        // Then
        exploringBattlefieldView.movedBattleUnits.shouldContainExactly(MovedBattleUnit(fromRow = 2, fromColumn = 1, path = path, unitId = "knight"))
        exploringBattlefieldView.moveFollowings.shouldContainExactly(BattlefieldView.Following.NONE)
    }

    @Test
    fun `MoveUnit should glide a follower of the party together with the step being played when it walks while exploring`() {
        // Given
        val exploringApis = exploringApis(deployments = partyOfTwoKnights)
        val exploringBattlefieldView = FakeBattlefieldView()
        val timeline = battleSceneTimeline()
        exploringPresenter(exploringApis, exploringBattlefieldView, timeline)
        exploringApis.partyApi.startExploration()
        exploringApis.eventBus.dispatch()
        val leaderStep = timeline.playLongStep()
        val path = listOf(position(2, 0))
        exploringApis.eventBus.publish(BattleUnitMoved(battleUnitId = "knight-2", fromRow = 2, fromColumn = 1, toRow = 2, toColumn = 0, path = path))
        // When
        exploringApis.eventBus.dispatch()
        // Then
        exploringBattlefieldView.movedBattleUnits.shouldContainExactly(MovedBattleUnit(fromRow = 2, fromColumn = 1, path = path, unitId = "knight"))
        leaderStep.complete(Unit)
    }

    @Test
    fun `MoveUnit should wait for the step being played to move a battle unit in a battle`() {
        // Given
        val previousStep = timeline.playLongStep()
        eventBus.publish(BattleUnitMoved(battleUnitId = "rat-1", fromRow = 1, fromColumn = 3, toRow = 2, toColumn = 3, path = listOf(position(2, 3))))
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.movedBattleUnits.shouldBeEmpty()
        previousStep.complete(Unit)
        battlefieldView.movedBattleUnits.shouldHaveSize(1)
    }

    @Test
    fun `ExplorationStarted should trail the leader of the party`() {
        // Given
        val exploringApis = exploringApis(deployments = partyOfTwoKnights)
        val exploringBattlefieldView = FakeBattlefieldView()
        exploringPresenter(exploringApis, exploringBattlefieldView)
        exploringApis.partyApi.startExploration()
        // When
        exploringApis.eventBus.dispatch()
        // Then
        exploringBattlefieldView.trailedTiles.shouldContainExactly(position(1, 1))
    }

    @Test
    fun `ExplorationResumed should trail the leader of the party`() {
        // Given
        val exploringApis = exploringApis(deployments = partyOfTwoKnights)
        val exploringBattlefieldView = FakeBattlefieldView()
        exploringApis.partyApi.startExploration()
        exploringApis.eventBus.dispatch()
        exploringPresenter(exploringApis, exploringBattlefieldView)
        exploringApis.eventBus.publish(PartyEvent.ExplorationResumed(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2")))
        // When
        exploringApis.eventBus.dispatch()
        // Then
        exploringBattlefieldView.trailedTiles.shouldContainExactly(position(1, 1))
    }

    @Test
    fun `ExplorationPaused should stop trailing the leader of the party`() {
        // Given
        val exploringApis = exploringApis(deployments = partyOfTwoKnights)
        val exploringBattlefieldView = FakeBattlefieldView()
        exploringPresenter(exploringApis, exploringBattlefieldView)
        exploringApis.eventBus.publish(PartyEvent.ExplorationPaused(playerId = "human"))
        // When
        exploringApis.eventBus.dispatch()
        // Then
        exploringBattlefieldView.stopTrailingCount shouldBe 1
    }

    @Test
    fun `MoveUnit should swap the leader over the member whichever battle unit moved first`() {
        // Given
        val exploringApis = exploringApis(deployments = partyOfTwoKnights)
        val exploringBattlefieldView = FakeBattlefieldView()
        exploringPresenter(exploringApis, exploringBattlefieldView)
        exploringApis.partyApi.startExploration()
        exploringApis.eventBus.dispatch()
        exploringApis.eventBus.publish(
            BattleUnitMoved(battleUnitId = "knight-2", fromRow = 2, fromColumn = 1, toRow = 1, toColumn = 1, path = listOf(position(1, 1)), swappedWithBattleUnitId = "knight-1"),
        )
        exploringApis.eventBus.publish(
            BattleUnitMoved(battleUnitId = "knight-1", fromRow = 1, fromColumn = 1, toRow = 2, toColumn = 1, path = listOf(position(2, 1)), swappedWithBattleUnitId = "knight-2"),
        )
        // When
        exploringApis.eventBus.dispatch()
        // Then
        exploringBattlefieldView.swappedBattleUnits.shouldContainExactly(
            SwappedBattleUnits(leader = BattleUnitAt(row = 1, column = 1, unitId = "knight"), member = BattleUnitAt(row = 2, column = 1, unitId = "knight")),
        )
    }

    // knight-1 leads the party, and knight-2 follows it.
    private val partyOfTwoKnights =
        listOf(
            Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1),
            Deployment(battleUnitId = "knight-2", unitId = "knight", playerId = "human", row = 2, column = 1),
        )

    @Test
    fun `MoveUnit should move a cpu battle unit wandering while exploring alongside the timeline, without following it`() {
        // Given
        val exploringApis =
            exploringApis(
                deployments =
                    listOf(
                        Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1),
                        Deployment(battleUnitId = "rat-1", unitId = "rat", playerId = "cpu", row = 3, column = 3),
                    ),
            )
        val exploringBattlefieldView = FakeBattlefieldView()
        val timeline = battleSceneTimeline()
        exploringPresenter(exploringApis, exploringBattlefieldView, timeline)
        timeline.playLongStep()
        val path = listOf(position(3, 2))
        exploringApis.eventBus.publish(BattleUnitMoved(battleUnitId = "rat-1", fromRow = 3, fromColumn = 3, toRow = 3, toColumn = 2, path = path))
        exploringApis.eventBus.publish(BattlefieldEvent.RequestMoveOccupant(occupantId = "rat-1", toRow = 3, toColumn = 2))
        val resetTilesCount = exploringBattlefieldView.resetTilesCount
        // When
        exploringApis.eventBus.dispatch()
        // Then
        exploringBattlefieldView.movedBattleUnits.shouldContainExactly(MovedBattleUnit(fromRow = 3, fromColumn = 3, path = path, unitId = "rat"))
        exploringBattlefieldView.moveFollowings.shouldContainExactly(BattlefieldView.Following.NONE)
        exploringBattlefieldView.moveStepDurations.shouldContainExactly(BattlefieldView.WANDERING_STEP_DURATION)
        exploringBattlefieldView.resetTilesCount shouldBe resetTilesCount
    }

    @Test
    fun `MoveUnit should move a cpu battle unit out of the battle in the timeline, without following it, when it wanders after a round`() {
        // Given: rat-1 engaged the knight, and rat-2 stays out of the battle.
        val battlingApis =
            exploringApis(
                deployments =
                    listOf(
                        Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1),
                        Deployment(battleUnitId = "rat-1", unitId = "rat", playerId = "cpu", row = 1, column = 2),
                        Deployment(battleUnitId = "rat-2", unitId = "rat", playerId = "cpu", row = 3, column = 3),
                    ),
            )
        battlingApis.battleApi.startBattle(engagingPlayerId = "cpu", engagedPlayerId = "human", engagingBattleUnitIds = listOf("rat-1"), engagedBattleUnitIds = listOf("knight-1"))
        battlingApis.eventBus.dispatch()
        val battlingBattlefieldView = FakeBattlefieldView()
        val timeline = battleSceneTimeline()
        exploringPresenter(battlingApis, battlingBattlefieldView, timeline)
        val previousStep = timeline.playLongStep()
        val path = listOf(position(3, 2))
        battlingApis.eventBus.publish(BattleUnitMoved(battleUnitId = "rat-2", fromRow = 3, fromColumn = 3, toRow = 3, toColumn = 2, path = path))
        battlingApis.eventBus.dispatch()
        battlingBattlefieldView.movedBattleUnits.shouldBeEmpty()
        // When
        previousStep.complete(Unit)
        // Then
        battlingBattlefieldView.movedBattleUnits.shouldContainExactly(MovedBattleUnit(fromRow = 3, fromColumn = 3, path = path, unitId = "rat"))
        battlingBattlefieldView.moveFollowings.shouldContainExactly(BattlefieldView.Following.NONE)
        battlingBattlefieldView.moveStepDurations.shouldContainExactly(BattlefieldView.MOVE_STEP_DURATION)
    }

    @Test
    fun `TileSelected should not display any movement range when a battle unit is selected while exploring`() {
        // Given
        val exploringApis = exploringApis()
        val exploringBattlefieldView = FakeBattlefieldView()
        val presenter = exploringPresenter(exploringApis, exploringBattlefieldView)
        // When
        presenter.tileSelected(row = 1, column = 1)
        exploringApis.eventBus.dispatch()
        // Then
        exploringBattlefieldView.potentialMovements.shouldBeEmpty()
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
    fun `FlashUnit should flash the battle unit in the timeline when it receives an effect while battling`() {
        // Given
        battlefieldView.flashing = CompletableDeferred()
        eventBus.publish(EffectReceived(battleUnitId = "knight-1", effectId = "lava-damage"))
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.flashedBattleUnits.shouldContainExactly(BattleUnitAt(row = 1, column = 1, unitId = "knight"))
        withClue("The scene plays until the flash finishes") { timeline.isPlaying.shouldBeTrue() }
    }

    @Test
    fun `FlashUnit should flash the battle unit alongside the timeline when it receives an effect while exploring`() {
        // Given
        val exploringApis = exploringApis()
        val exploringBattlefieldView = FakeBattlefieldView().apply { flashing = CompletableDeferred() }
        val exploringTimeline = battleSceneTimeline()
        exploringPresenter(exploringApis, exploringBattlefieldView, exploringTimeline)
        exploringApis.eventBus.publish(EffectReceived(battleUnitId = "knight-1", effectId = "water-heal"))
        // When
        exploringApis.eventBus.dispatch()
        // Then
        exploringBattlefieldView.flashedBattleUnits.shouldContainExactly(BattleUnitAt(row = 1, column = 1, unitId = "knight"))
        withClue("The party can walk while it flashes") { exploringTimeline.isPlaying.shouldBeFalse() }
    }

    @Test
    fun `FlashUnit should flash the battle unit on view when it joins the party`() {
        // Given
        eventBus.publish(PartyEvent.PartyJoined(playerId = "player-1", battleUnitId = "knight-1", unitId = "knight"))
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.flashedBattleUnits.shouldContainExactly(BattleUnitAt(row = 1, column = 1, unitId = "knight"))
    }

    @Test
    fun `RemoveNpc should remove the recruited npc from its tile once the previous steps have been played`() {
        // Given
        val previousStep = timeline.playLongStep()
        publish(NpcEvent.NpcRecruited(npcId = "viking", row = 2, column = 2))
        battlefieldView.removedNpcs.shouldBeEmpty()
        // When
        previousStep.complete(Unit)
        // Then
        battlefieldView.removedNpcs.shouldContainExactly(PositionDto(2, 2))
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
    fun `FlashUnit should flash a vanished summon on the tile it vanished from, as it is deleted`() {
        // Given
        eventBus.publish(BattleUnitVanished(playerId = "human", battleUnitId = "bee-1", unitId = "bee", row = 2, column = 3))
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.flashedBattleUnits.shouldContainExactly(BattleUnitAt(row = 2, column = 3, unitId = "bee"))
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
        apis.lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "health-potion", row = 2, column = 2)
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.displayedLoot shouldBe mapOf(position(2, 2) to listOf("health-potion"))
    }

    @Test
    fun `DisplayLoot should display every item lying on the tile when several items are dropped on it`() {
        // Given
        apis.lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "health-potion", row = 2, column = 2)
        apis.lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "hammer", row = 2, column = 2)
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.displayedLoot shouldBe mapOf(position(2, 2) to listOf("health-potion", "hammer"))
    }

    @Test
    fun `DisplayLoot should display the items left on the tile when a dropped item is picked up`() {
        // Given
        apis.lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "health-potion", row = 2, column = 2)
        apis.lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "hammer", row = 2, column = 2)
        eventBus.dispatch()
        apis.lootDropApi.pickUpDroppedItem(lootableItemId = "health-potion", row = 2, column = 2)
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.displayedLoot shouldBe mapOf(position(2, 2) to listOf("hammer"))
    }

    @Test
    fun `DisplayLoot should display the dropped item once the previous steps have been played`() {
        // Given
        val previousStep = timeline.playLongStep()
        apis.lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "health-potion", row = 2, column = 2)
        eventBus.dispatch()
        require(battlefieldView.displayedLoot.isEmpty())
        // When
        previousStep.complete(Unit)
        // Then
        battlefieldView.displayedLoot shouldBe mapOf(position(2, 2) to listOf("health-potion"))
    }

    @Test
    fun `PickUpLoot should lift the item off its tile when a dropped item is picked up`() {
        // Given
        apis.lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "health-potion", row = 2, column = 2)
        eventBus.dispatch()
        apis.lootDropApi.pickUpDroppedItem(lootableItemId = "health-potion", row = 2, column = 2)
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.pickedUpLoot.shouldContainExactly(PickedUpLoot(row = 2, column = 2, lootableItemId = "health-potion"))
        battlefieldView.displayedLoot shouldBe mapOf(position(2, 2) to emptyList())
    }

    @Test
    fun `PickUpLoot should lift the item off its tile once the previous steps have been played`() {
        // Given
        apis.lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "health-potion", row = 2, column = 2)
        eventBus.dispatch()
        val previousStep = timeline.playLongStep()
        apis.lootDropApi.pickUpDroppedItem(lootableItemId = "health-potion", row = 2, column = 2)
        eventBus.dispatch()
        require(battlefieldView.pickedUpLoot.isEmpty())
        // When
        previousStep.complete(Unit)
        // Then
        battlefieldView.pickedUpLoot.shouldContainExactly(PickedUpLoot(row = 2, column = 2, lootableItemId = "health-potion"))
    }

    @Test
    fun `PickUpLoot should lift the item off its tile once the battle unit has walked over it when a human battle unit walks over loot`() {
        // Given
        apis.lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "health-potion", row = 2, column = 1)
        eventBus.dispatch()
        // When
        publish(RequestMoveBattleUnit(battleUnitId = "knight-1", moveToRow = 3, moveToColumn = 1))
        // Then
        battlefieldView.movedBattleUnits.single().path shouldBe listOf(position(2, 1), position(3, 1))
        battlefieldView.pickedUpLoot.shouldContainExactly(PickedUpLoot(row = 2, column = 1, lootableItemId = "health-potion"))
    }

    @Test
    fun `PickUpLoot should not lift anything when a cpu battle unit walks over loot`() {
        // Given
        apis.lootDropApi.placeDroppedItem(unitId = "rat", lootableItemId = "health-potion", row = 2, column = 3)
        eventBus.dispatch()
        publish(PlayerTurnStarted(playerId = "cpu", battleUnitIds = emptySet()))
        // When
        publish(RequestMoveBattleUnit(battleUnitId = "rat-1", moveToRow = 2, moveToColumn = 3))
        // Then
        battlefieldView.movedBattleUnits.shouldHaveSize(1)
        battlefieldView.pickedUpLoot.shouldBeEmpty()
        battlefieldView.displayedLoot shouldBe mapOf(position(2, 3) to listOf("health-potion"))
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

    @Test
    fun `ClearSelection should reset the tiles and hide the selected battle unit when the battle finishes`() {
        // Given
        selectKnight()
        val resetTilesCount = battlefieldView.resetTilesCount
        val hideCount = battleUnitInfoView.hideCount
        // When
        publish(BattleFinished)
        // Then
        battlefieldView.resetTilesCount shouldBeGreaterThan resetTilesCount
        battleUnitInfoView.hideCount shouldBeGreaterThan hideCount
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
        apis.battlefieldApi.updateBattlefieldOccupancy(row = 1, column = 2, occupantId = "bee-1")
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
    fun `SelectBattleUnit should display the ability availabilities of the selected battle unit when it is selected`() {
        // Given
        // When
        selectKnight()
        // Then
        val displayed = battleUnitInfoView.displayedBattleUnits.last()
        displayed.abilityAvailabilities shouldBe apis.battleUnitApi.searchAbilityAvailabilities("knight-1")
        displayed.abilityAvailabilities.map { availability -> availability.abilityId } shouldBe
            displayed.battleUnit.abilityCooldowns.keys
                .toList()
    }

    @Test
    fun `RefreshSelectedBattleUnit should not move the previously selected battle unit when a turn starts and an empty tile is selected`() {
        // Given
        selectKnight()
        val requestedMoves = mutableListOf<RequestMoveBattleUnit>()
        eventBus.subscribe<RequestMoveBattleUnit> { event -> requestedMoves += event }
        publish(PlayerTurnStarted(playerId = "human", battleUnitIds = emptySet()))
        // When
        battlefieldPresenter.tileSelected(row = 1, column = 2)
        eventBus.dispatch()
        // Then
        requestedMoves.shouldBeEmpty()
    }

    private val battleUnitIdsInBattle = setOf("knight-1", "rat-1", "rat-2")

    @Test
    fun `SelectPartyLeader should select the party leader with its movement range when a human turn starts`() {
        // Given
        // When
        publish(PlayerTurnStarted(playerId = "human", battleUnitIds = battleUnitIdsInBattle))
        // Then
        battleUnitInfoView.displayedBattleUnits.map { displayed -> displayed.battleUnit.id }.shouldContainExactly("knight-1")
        battlefieldView.tileSelections.shouldContainExactly(position(1, 1))
        battlefieldView.potentialMovements shouldContain position(1, 2)
    }

    @Test
    fun `SelectPartyLeader should scroll to the party leader when a human turn starts`() {
        // Given
        // When
        publish(PlayerTurnStarted(playerId = "human", battleUnitIds = battleUnitIdsInBattle))
        // Then
        battlefieldView.scrolledToTiles.shouldContainExactly(position(1, 1))
    }

    @Test
    fun `SelectPartyLeader should not select any battle unit when a cpu turn starts`() {
        // Given
        // When
        publish(PlayerTurnStarted(playerId = "cpu", battleUnitIds = battleUnitIdsInBattle))
        // Then
        battleUnitInfoView.displayedBattleUnits.shouldBeEmpty()
        battlefieldView.tileSelections.shouldBeEmpty()
    }

    @Test
    fun `SelectPartyLeader should not select the party leader when it is not in the battle`() {
        // Given
        // When
        publish(PlayerTurnStarted(playerId = "human", battleUnitIds = setOf("rat-1", "rat-2")))
        // Then
        battleUnitInfoView.displayedBattleUnits.shouldBeEmpty()
        battlefieldView.tileSelections.shouldBeEmpty()
    }

    @Test
    fun `SelectPartyLeader should select the new party leader when a human turn starts after the leader left the party`() {
        // Given
        // E.g.: the knight-1 has been defeated, so the knight-2 leads the party.
        val battlingApis =
            SubdomainApis(
                terrains = listOf(occupiableTerrain(id = "sand").toDto()),
                units = listOf(unit(id = "knight", movementRange = 3).toDto(), unit(id = "rat").toDto()),
                players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
                tiles = List(4) { List(4) { "sand" } },
                deployments = partyOfTwoKnights + Deployment(battleUnitId = "rat-1", unitId = "rat", playerId = "cpu", row = 1, column = 3),
                playerOrder = listOf("cpu", "human"),
                explore = true,
            ).also(SubdomainApis::initialise)
        val battlingBattlefieldView = FakeBattlefieldView()
        exploringPresenter(battlingApis, battlingBattlefieldView)
        battlingApis.partyApi.leaveParty("knight-1")
        // When
        battlingApis.eventBus.publish(PlayerTurnStarted(playerId = "human", battleUnitIds = setOf("knight-1", "knight-2", "rat-1")))
        battlingApis.eventBus.dispatch()
        // Then
        battlingBattlefieldView.tileSelections.shouldContainExactly(position(2, 1))
    }

    @Test
    fun `TileSelected should ignore the selected tile while talking to an npc`() {
        // Given
        val exploringApis = exploringApis(npcs = listOf(npc(id = "the-guy", row = 1, column = 0).toDto()))
        val presenter = exploringPresenter(exploringApis, FakeBattlefieldView())
        exploringApis.partyApi.startExploration()
        exploringApis.npcApi.talkToAdjacentNpc()
        exploringApis.eventBus.dispatch()
        val selections = mutableListOf<BattlefieldHudEvent.SelectedBattleUnit>()
        exploringApis.eventBus.subscribe<BattlefieldHudEvent.SelectedBattleUnit> { event -> selections += event }
        // When
        presenter.tileSelected(row = 1, column = 1)
        exploringApis.eventBus.dispatch()
        // Then
        selections.shouldBeEmpty()
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
    fun `TileSelected should hand the selected tile to the interceptor first, and ignore it when taken, e g while giving an item`() {
        // Given
        val selections = mutableListOf<BattlefieldHudEvent.SelectedBattleUnit>()
        eventBus.subscribe<BattlefieldHudEvent.SelectedBattleUnit> { event -> selections += event }
        val interceptedTiles = mutableListOf<Pair<Int, Int>>()
        var isTaken = true
        battlefieldPresenter.setTileInterceptor { row, column ->
            interceptedTiles += row to column
            isTaken
        }
        // When
        battlefieldPresenter.tileSelected(row = 1, column = 1)
        eventBus.dispatch()
        // Then
        interceptedTiles.shouldContainExactly(1 to 1)
        selections.shouldBeEmpty()
        // When
        isTaken = false
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
        preview.receivers.map { receiver -> receiver.remainingHealthPoints }.shouldContainExactly(14, 14)
    }

    @Test
    fun `DisplayEnemyAbilityCastPreview should preview the effects that would stay on each enemy when an enemy ability cast is previewed`() {
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
        preview.receivers.map { receiver -> receiver.newEffectIds }.shouldContainExactly(listOf("venom"), listOf("venom"))
        preview.casterNewEffectIds.shouldBeEmpty()
    }

    @Test
    fun `DisplayCastGroup should display the health each enemy would have left instead of the selection when an enemy ability cast is previewed`() {
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
        battlefieldView.healthPreviews shouldBe mapOf(position(1, 3) to PreviewOption.TWO_OUT_OF_THREE, position(0, 3) to PreviewOption.TWO_OUT_OF_THREE)
        battlefieldView.tileSelections.shouldBeEmpty()
    }

    @Test
    fun `DisplayCastGroup should display the health the caster would have left when a self ability cast is previewed on it`() {
        // Given
        eventBus.publish(
            BattlefieldHudEvent.SelfAbilityCastPreviewed(
                casterBattleUnitId = "knight-1",
                abilityId = "heal",
                castGroup = CastGroupDto(tiles = listOf(TileDto(row = 1, column = 1))),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.healthPreviews shouldBe mapOf(position(1, 1) to PreviewOption.FULL)
        battlefieldView.tileSelections.shouldBeEmpty()
    }

    @Test
    fun `DisplaySelfAbilityCastPreview should preview the health the caster would have left when it is healed`() {
        // Given
        val poisoning = EffectApplicationDto(source = ApplicationSourceDto.battleUnit("rat-1"), target = EffectTargetDto.Unit(id = "knight-1"), effectId = "poison")
        apis.battleUnitApi.applyEffect(poisoning)
        val knight = apis.battleUnitApi.searchBattleUnitById("knight-1")!!
        eventBus.publish(
            BattlefieldHudEvent.SelfAbilityCastPreviewed(
                casterBattleUnitId = "knight-1",
                abilityId = "heal",
                castGroup = CastGroupDto(tiles = listOf(TileDto(row = 1, column = 1))),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        attackPreviewView.displayedAttackPreviews.single().casterRemainingHealthPoints shouldBe knight.maximumHealthPoints - 7 + 5
    }

    @Test
    fun `DisplayCastGroup should select the vacant tiles when a self ability cast is previewed on them`() {
        // Given
        eventBus.publish(
            BattlefieldHudEvent.SelfAbilityCastPreviewed(
                casterBattleUnitId = "knight-1",
                abilityId = "bee",
                castGroup = CastGroupDto(tiles = listOf(TileDto(row = 1, column = 2))),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        battlefieldView.tileSelections.shouldContainExactly(position(1, 2))
        battlefieldView.healthPreviews.shouldBeEmpty()
    }
}
