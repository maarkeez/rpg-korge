package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldCreated
import com.mkz.rpg.building.domain.BuildingEvent
import com.mkz.rpg.lootDrop.domain.LootDropEvent
import com.mkz.rpg.npc.domain.NpcEvent
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.CastGroupDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.CancelCast
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.ConfirmCast
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.DeselectBattleUnit
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.InitializeBattlefieldHud
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.ProcessAbilitySelected
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.ProcessTileSelected
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.RefreshMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.SelectBattleUnit
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.services.MovementService
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.unit.adapters.presentation.UnitApi

class BattlefieldPresenter(
    private val battlefieldView: BattlefieldView,
    private val battleUnitInfoView: BattleUnitInfoView,
    private val attackPreviewView: AttackPreviewView,
    private val playerCallToActionView: PlayerCallToActionView,
    private val battleHudView: BattleHudView,
    private val battlefieldApi: BattlefieldApi,
    private val battleUnitApi: BattleUnitApi,
    private val playerApi: PlayerApi,
    private val unitApi: UnitApi,
    private val abilityApi: AbilityApi,
    private val battleApi: BattleApi,
    private val partyApi: PartyApi,
    private val timeline: BattleSceneTimeline,
    eventBus: EventBus,
) : BattlefieldView.Delegate,
    AbilityButtonView.Delegate {
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val movementService =
        MovementService(
            searchBattleUnitById = battleUnitApi.searchBattleUnitById,
            searchTilesThatCanBeOccupied = battlefieldApi.searchTilesThatCanBeOccupied,
            canMoveTo = battleUnitApi.canMoveTo,
        )
    private val initializeBattlefieldHud = InitializeBattlefieldHud(battlefieldHudRepository)
    private val processTileSelected =
        ProcessTileSelected(
            searchOccupant = battlefieldApi.searchOccupant,
            searchBattleUnitById = battleUnitApi.searchBattleUnitById,
            searchPlayerById = playerApi.searchPlayerById,
            searchBattle = battleApi.searchBattle,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )
    private val processAbilitySelected =
        ProcessAbilitySelected(
            canCastAbility = battleUnitApi.canCastAbility,
            whereCanCast = battleUnitApi.whereCanCast,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )
    private val confirmCast =
        ConfirmCast(
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )
    private val cancelCast =
        CancelCast(
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )
    private val deselectBattleUnit =
        DeselectBattleUnit(
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )
    private val selectBattleUnit =
        SelectBattleUnit(
            searchPosition = battlefieldApi.searchPosition,
            searchBattle = battleApi.searchBattle,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )
    private val refreshMovementRange =
        RefreshMovementRange(
            searchPosition = battlefieldApi.searchPosition,
            searchBattle = battleApi.searchBattle,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )

    // The items displayed on each tile. Changed in timeline order, so each step draws the loot the battle had then.
    private val displayedLoot = mutableMapOf<PositionDto, List<String>>()

    // Takes the tapped tiles before the battlefield HUD, e.g.: while giving an item. Returns whether it took the tap.
    private var tileInterceptor: ((row: Int, column: Int) -> Boolean)? = null

    // The first battle unit of a swap and its unit, until the battle unit it swaps with has moved as well.
    private var pendingSwap: Pair<BattleUnitEvent.BattleUnitMoved, String>? = null

    private val subscriptions =
        listOf(
            eventBus.subscribe<BattlefieldCreated> { displayBattlefield() },
            eventBus.subscribe<BattleUnitEvent.BattleUnitDeployed> { event ->
                displayUnit(event.row, event.column, event.battleUnitId)
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitRespawned> { event ->
                displayUnit(event.row, event.column, event.battleUnitId)
            },
            eventBus.subscribe<BuildingEvent.BuildingPlaced> { event ->
                timeline.play { battlefieldView.displayBuilding(event.row, event.column, event.buildingId) }
            },
            eventBus.subscribe<NpcEvent.NpcPlaced> { event ->
                timeline.play { battlefieldView.displayNpc(event.row, event.column, event.npcId, event.unitId) }
            },
            // A recruited npc leaves its tile, where the battle unit it joins the party as is displayed, flashing once it has joined.
            eventBus.subscribe<NpcEvent.NpcRecruited> { event ->
                timeline.play { battlefieldView.removeNpc(event.row, event.column) }
            },
            eventBus.subscribe<PartyEvent.PartyJoined> { event ->
                flashUnit(event.battleUnitId)
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitMoved> { event ->
                moveUnit(event)
            },
            // While exploring, the camera trails the leader, e.g.: once a battle finishes, and stops when a battle starts.
            eventBus.subscribe<PartyEvent.ExplorationStarted> { trailPartyLeader() },
            eventBus.subscribe<PartyEvent.ExplorationResumed> { trailPartyLeader() },
            eventBus.subscribe<PartyEvent.ExplorationPaused> {
                timeline.play { battlefieldView.stopTrailing() }
            },
            // The selected caster is displayed again once its cast has been resolved.
            eventBus.subscribe<BattleUnitEvent.AbilityCastResolved> {
                refreshMovementRange()
            },
            // An occupied tile (e.g.: a battle unit moved or deployed) may close paths of the selected battle unit.
            eventBus.subscribe<BattlefieldEvent.BattlefieldTileOccupied> {
                refreshMovementRange()
            },
            // While exploring, alongside the timeline, so the party keeps walking, e.g.: while standing on water heals it.
            eventBus.subscribe<BattleUnitEvent.EffectReceived> { event ->
                flashUnit(event.battleUnitId, isAlongside = !isBattling())
            },
            eventBus.subscribe<BattleUnitEvent.OnTurnStartedEffectApplied> { event ->
                flashUnit(event.battleUnitId)
            },
            // While exploring, the health is displayed as the battle unit flashes, e.g.: so standing on lava is seen hurting.
            eventBus.subscribe<BattleUnitEvent.BattleUnitDamaged> { event ->
                if (!isBattling()) flashHealth(event.battleUnitId)
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitHealed> { event ->
                if (!isBattling()) flashHealth(event.battleUnitId)
            },
            // A vanished summon is deleted, so it is flashed from the event, before its tile is vacated.
            eventBus.subscribe<BattleUnitEvent.BattleUnitVanished> { event ->
                timeline.play { battlefieldView.flashBattleUnit(row = event.row, column = event.column, unitId = event.unitId) }
            },
            // A vacated tile (e.g.: a battle unit defeated) may open paths of the selected battle unit.
            eventBus.subscribe<BattlefieldEvent.OccupantRemoved> { event ->
                removeUnit(event.row, event.column)
                refreshMovementRange()
            },
            eventBus.subscribe<LootDropEvent.LootDropped> { event ->
                timeline.play { dropLoot(event.row, event.column, event.lootableItemId) }
            },
            eventBus.subscribe<LootDropEvent.DroppedItemPickedUp> { event ->
                timeline.play { pickUpLoot(event.row, event.column, event.lootableItemId) }
            },
            // The selection is cleared before the turn is announced, and can not be acted on during the new turn. A human
            // turn then selects the party leader, so the player sees what it can do. Its selection is published while the
            // turn starts, so it is drawn once the turn has been announced.
            eventBus.subscribe<BattleEvent.PlayerTurnStarted> { event ->
                timeline.play { clearSelection() }
                deselectBattleUnit()
                selectPartyLeader(event)
            },
            // The selection is cleared when the battle finishes, as the party explores again.
            eventBus.subscribe<BattleEvent.BattleFinished> {
                timeline.play { clearSelection() }
                deselectBattleUnit()
            },
            eventBus.subscribe<BattlefieldHudEvent.SelectedBattleUnit> { event ->
                timeline.play(displayMovementRange(event))
            },
            eventBus.subscribe<BattlefieldHudEvent.Idle> {
                timeline.play { clearSelection() }
            },
            eventBus.subscribe<BattlefieldHudEvent.SelectedBattleUnitAbility> { event ->
                timeline.play(displayAbilitySelected(event))
            },
            eventBus.subscribe<BattlefieldHudEvent.AbilityDeselected> {
                timeline.play { clearSelection() }
            },
            eventBus.subscribe<BattlefieldHudEvent.SelfAbilityCastPreviewed> { event ->
                timeline.play(displaySelfAbilityCastPreview(event))
            },
            eventBus.subscribe<BattlefieldHudEvent.EnemyAbilityCastPreviewed> { event ->
                timeline.play(displayEnemyAbilityCastPreview(event))
            },
        )

    init {
        initializeBattlefieldHud.invoke()
        battlefieldView.setDelegate(this)
        battleUnitInfoView.setDelegate(this)
    }

    // Each function reads what it needs straight away and draws it once the previous timeline steps have been played.

    fun displayBattlefield() {
        val battlefield = battlefieldApi.searchBattlefield()!!
        timeline.play { battlefieldView.displayBattlefield(battlefield) }
    }

    fun displayUnit(
        row: Int,
        column: Int,
        battleUnitId: String,
    ) {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return
        timeline.play { battlefieldView.displayBattleUnit(row, column, battleUnit.unitId) }
    }

    fun removeUnit(
        row: Int,
        column: Int,
    ) {
        timeline.play { battlefieldView.removeBattleUnit(row, column) }
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    /** Hands the tapped tiles to the interceptor first, which returns whether it took the tap, e.g.: to give an item. */
    fun setTileInterceptor(interceptor: (row: Int, column: Int) -> Boolean) {
        tileInterceptor = interceptor
    }

    /** Selects the battle unit, as if the player had tapped its tile, e.g.: the party member the inventory switches to. */
    fun select(battleUnitId: String) = selectBattleUnit(battleUnitId)

    private fun moveUnit(event: BattleUnitEvent.BattleUnitMoved) {
        val battleUnit = battleUnitApi.searchBattleUnitById(event.battleUnitId) ?: return
        if (event.swappedWithBattleUnitId != null) {
            swapUnit(event, battleUnit.unitId)
            return
        }
        if (isWandering(battleUnit)) {
            wanderUnit(event, battleUnit.unitId)
            return
        }
        val isBattling = isBattling()
        val stepDuration = if (isBattling) BattlefieldView.MOVE_STEP_DURATION else BattlefieldView.EXPLORING_STEP_DURATION
        // While exploring, the battlefield only trails the leader, so it does not scroll from one party member to another,
        // and the followers glide together with the leader, so the party walks a tile in a single step.
        val isFollowing = !isBattling && isPartyFollower(battleUnit)
        val following =
            when {
                isBattling -> BattlefieldView.Following.CENTER
                isFollowing -> BattlefieldView.Following.NONE
                else -> BattlefieldView.Following.TRAIL
            }
        val step: suspend () -> Unit = {
            battlefieldView.resetTiles()
            battlefieldView.moveBattleUnit(
                fromRow = event.fromRow,
                fromColumn = event.fromColumn,
                path = event.path,
                unitId = battleUnit.unitId,
                stepDuration = stepDuration,
                following = following,
            )
        }
        if (isFollowing) timeline.playTogether(step) else timeline.play(step)
    }

    // Reads where the leader stands when the exploration starts or resumes, unless a battle has already started.
    private fun trailPartyLeader() {
        val party = partyApi.searchParty()?.takeIf { party -> party.isExploring } ?: return
        val leaderPosition = party.leaderBattleUnitId?.let(battlefieldApi.searchPosition::invoke) ?: return
        timeline.play { battlefieldView.trail(leaderPosition.row, leaderPosition.column) }
    }

    // Both battle units of a swap are animated in a single step, once both have moved: the party leader hops over the
    // member, whichever of them moved first.
    private fun swapUnit(
        event: BattleUnitEvent.BattleUnitMoved,
        unitId: String,
    ) {
        val firstSwapped =
            pendingSwap?.takeIf { (firstEvent, _) -> firstEvent.battleUnitId == event.swappedWithBattleUnitId } ?: run {
                pendingSwap = event to unitId
                return
            }
        pendingSwap = null
        val secondSwapped = event to unitId
        val leaderId = partyApi.searchParty()?.leaderBattleUnitId
        val (leader, member) = if (event.battleUnitId == leaderId) secondSwapped to firstSwapped else firstSwapped to secondSwapped
        val (leaderEvent, leaderUnitId) = leader
        val (memberEvent, memberUnitId) = member
        timeline.play {
            battlefieldView.resetTiles()
            battlefieldView.swapBattleUnits(
                leaderRow = leaderEvent.fromRow,
                leaderColumn = leaderEvent.fromColumn,
                leaderUnitId = leaderUnitId,
                memberRow = memberEvent.fromRow,
                memberColumn = memberEvent.fromColumn,
                memberUnitId = memberUnitId,
            )
        }
    }

    private fun isPartyFollower(battleUnit: BattleUnit.Dto): Boolean {
        val party = partyApi.searchParty() ?: return false
        return battleUnit.id in party.battleUnitIds && battleUnit.id != party.leaderBattleUnitId
    }

    // The CPU battle units out of the battles wander without scrolling the battlefield away from the party, nor clearing
    // the selection (e.g.: to use the inventory). While exploring, alongside the timeline, so the party keeps walking
    // smoothly. When a round finishes, in the timeline, so they are seen before the next round starts.
    private fun wanderUnit(
        event: BattleUnitEvent.BattleUnitMoved,
        unitId: String,
    ) {
        val isBattling = isBattling()
        val stepDuration = if (isBattling) BattlefieldView.MOVE_STEP_DURATION else BattlefieldView.WANDERING_STEP_DURATION
        val step: suspend () -> Unit = {
            battlefieldView.moveBattleUnit(
                fromRow = event.fromRow,
                fromColumn = event.fromColumn,
                path = event.path,
                unitId = unitId,
                stepDuration = stepDuration,
                following = BattlefieldView.Following.NONE,
            )
        }
        if (isBattling) timeline.play(step) else timeline.playAlongside(step)
    }

    private fun dropLoot(
        row: Int,
        column: Int,
        lootableItemId: String,
    ) {
        val position = PositionDto(row, column)
        val lootableItemIds = displayedLoot[position].orEmpty() + lootableItemId
        displayedLoot[position] = lootableItemIds
        battlefieldView.displayLoot(row = row, column = column, lootableItemIds = lootableItemIds)
    }

    private suspend fun pickUpLoot(
        row: Int,
        column: Int,
        lootableItemId: String,
    ) {
        val position = PositionDto(row, column)
        val remainingLootableItemIds = displayedLoot[position].orEmpty() - lootableItemId
        displayedLoot[position] = remainingLootableItemIds
        battlefieldView.pickUpLoot(row = row, column = column, lootableItemId = lootableItemId, remainingLootableItemIds = remainingLootableItemIds)
    }

    private fun flashUnit(
        battleUnitId: String,
        isAlongside: Boolean = false,
    ) {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return
        val position = battlefieldApi.searchPosition(battleUnitId) ?: return
        val step: suspend () -> Unit = { battlefieldView.flashBattleUnit(row = position.row, column = position.column, unitId = battleUnit.unitId) }
        if (isAlongside) timeline.playAlongside(step) else timeline.play(step)
    }

    // Alongside the timeline, as the flash while exploring, so the party keeps walking.
    private fun flashHealth(battleUnitId: String) {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return
        val position = battlefieldApi.searchPosition(battleUnitId) ?: return
        val previewOption =
            BattlefieldHealthBarPreviewView.PreviewOption.of(
                remaining = battleUnit.remainingHealthPoints,
                maximum = battleUnit.maximumHealthPoints,
            )
        timeline.playAlongside { battlefieldView.flashHealth(row = position.row, column = position.column, previewOption = previewOption) }
    }

    private fun displayMovementRange(selectedBattleUnitEvent: BattlefieldHudEvent.SelectedBattleUnit): suspend () -> Unit {
        val battleUnit = battleUnitApi.searchBattleUnitById(selectedBattleUnitEvent.battleUnitId)!!
        val unit = unitApi.searchUnitById(battleUnit.unitId)!!
        val abilityAvailabilities = battleUnitApi.searchAbilityAvailabilities(battleUnit.id)
        val isBattling = isBattling()
        return {
            // In a battle, the battlefield scrolls to the selected battle unit (e.g.: the party leader when its turn
            // starts), when it is out of the viewport or on its edge.
            if (isBattling) battlefieldView.scrollToTile(row = selectedBattleUnitEvent.tile.row, column = selectedBattleUnitEvent.tile.column)
            battlefieldView.resetTiles()
            battleUnitInfoView.display(battleUnit, unit, abilityAvailabilities)
            battleHudView.displayBattleUnitInfoView()
            selectedBattleUnitEvent.tilesWhereCanBeMoved.forEach { tile ->
                battlefieldView.displayPotentialMovement(row = tile.row, column = tile.column)
            }
            battlefieldView.displayTileSelection(
                row = selectedBattleUnitEvent.tile.row,
                column = selectedBattleUnitEvent.tile.column,
            )
            val selectableTiles = selectedBattleUnitEvent.tilesWhereCanBeMoved + selectedBattleUnitEvent.tile
            battlefieldView.dimTilesExcept(selectableTiles.map { tile -> PositionDto(tile.row, tile.column) }.toSet())
        }
    }

    // The party leader, when it is in the battle and the turn is the human player's.
    private fun selectPartyLeader(event: BattleEvent.PlayerTurnStarted) {
        if (playerApi.searchPlayerById(event.playerId)?.type != Player.Dto.PlayerTypeDto.HUMAN) return
        val leaderId = partyApi.searchParty()?.leaderBattleUnitId ?: return
        if (leaderId in event.battleUnitIds) selectBattleUnit(leaderId)
    }

    private fun clearSelection() {
        battleUnitInfoView.hide()
        battlefieldView.resetTiles()
        battleHudView.hide()
    }

    private fun displayAbilitySelected(event: BattlefieldHudEvent.SelectedBattleUnitAbility): suspend () -> Unit {
        val battleUnit = battleUnitApi.searchBattleUnitById(event.battleUnitId)!!
        val abilityIndex = battleUnit.abilityCooldowns.keys.indexOf(event.abilityId)
        return {
            battlefieldView.resetTiles()
            battleUnitInfoView.displayAbilitySelected(abilityIndex)
            battleHudView.displayBattleUnitInfoView()
            event.castGroupsWhereCanCast.forEach { castGroup ->
                castGroup.tiles.forEach { tilePosition ->
                    battlefieldView.displayPotentialCast(
                        row = tilePosition.row,
                        column = tilePosition.column,
                    )
                }
            }
            val selectableTiles = event.castGroupsWhereCanCast.flatMap { castGroup -> castGroup.tiles } + event.casterTile
            battlefieldView.dimTilesExcept(selectableTiles.map { tile -> PositionDto(tile.row, tile.column) }.toSet())
        }
    }

    private fun displaySelfAbilityCastPreview(event: BattlefieldHudEvent.SelfAbilityCastPreviewed): suspend () -> Unit {
        val casterBattleUnit = battleUnitApi.searchBattleUnitById(event.casterBattleUnitId)!!
        val casterUnit = unitApi.searchUnitById(casterBattleUnit.unitId)!!
        val ability = abilityApi.searchAbilityById(event.abilityId)!!
        val remainingHealth = previewCastRemainingHealth(event.casterBattleUnitId, event.abilityId, event.castGroup)
        val castGroupTiles = castGroupTiles(event.castGroup, remainingHealth)
        val newEffectIds = previewCastOngoingEffects(event.casterBattleUnitId, event.abilityId, event.castGroup)
        return {
            attackPreviewView.display(
                casterBattleUnit = casterBattleUnit,
                casterUnit = casterUnit,
                manaCost = ability.cost,
                receivers = emptyList(),
                casterRemainingHealthPoints = remainingHealth[casterBattleUnit.id] ?: casterBattleUnit.remainingHealthPoints,
                casterNewEffectIds = newEffectIds[casterBattleUnit.id].orEmpty(),
            )
            displayCastGroup(castGroupTiles)
            displayAttackPreview()
        }
    }

    private fun displayEnemyAbilityCastPreview(event: BattlefieldHudEvent.EnemyAbilityCastPreviewed): suspend () -> Unit {
        val casterBattleUnit = battleUnitApi.searchBattleUnitById(event.casterBattleUnitId)!!
        val casterUnit = unitApi.searchUnitById(casterBattleUnit.unitId)!!
        val ability = abilityApi.searchAbilityById(event.abilityId)!!
        val remainingHealth = previewCastRemainingHealth(event.casterBattleUnitId, event.abilityId, event.castGroup)
        val castGroupTiles = castGroupTiles(event.castGroup, remainingHealth)
        val newEffectIds = previewCastOngoingEffects(event.casterBattleUnitId, event.abilityId, event.castGroup)
        val receivers =
            event.enemyBattleUnitIds.map { enemyBattleUnitId ->
                val enemyBattleUnit = battleUnitApi.searchBattleUnitById(enemyBattleUnitId)!!
                val enemyUnit = unitApi.searchUnitById(enemyBattleUnit.unitId)!!
                AttackPreviewView.Receiver(
                    battleUnit = enemyBattleUnit,
                    unit = enemyUnit,
                    remainingHealthPoints = remainingHealth[enemyBattleUnitId] ?: enemyBattleUnit.remainingHealthPoints,
                    newEffectIds = newEffectIds[enemyBattleUnitId].orEmpty(),
                )
            }
        return {
            attackPreviewView.display(
                casterBattleUnit = casterBattleUnit,
                casterUnit = casterUnit,
                manaCost = ability.cost,
                receivers = receivers,
                casterRemainingHealthPoints = remainingHealth[casterBattleUnit.id] ?: casterBattleUnit.remainingHealthPoints,
                casterNewEffectIds = newEffectIds[casterBattleUnit.id].orEmpty(),
            )
            displayCastGroup(castGroupTiles)
            displayAttackPreview()
        }
    }

    /** A tile of the previewed cast group, and the health its battle unit would have left. Null when the tile is vacant. */
    private data class CastGroupTile(
        val row: Int,
        val column: Int,
        val healthPreview: BattlefieldHealthBarPreviewView.PreviewOption?,
    )

    // The health each battle unit would have left once the ability is cast on the cast group, keyed by battle unit id.
    // Only the battle units whose health the cast changes are listed.
    private fun previewCastRemainingHealth(
        casterBattleUnitId: String,
        abilityId: String,
        castGroup: CastGroupDto,
    ): Map<String, Int> =
        battleUnitApi.previewCastRemainingHealth(
            battleUnitId = casterBattleUnitId,
            abilityId = abilityId,
            castGroup = castGroup.tiles.map { tile -> PositionDto(row = tile.row, column = tile.column) },
        )

    private fun castGroupTiles(
        castGroup: CastGroupDto,
        remainingHealth: Map<String, Int>,
    ): List<CastGroupTile> =
        castGroup.tiles.map { tile ->
            val battleUnit = battlefieldApi.searchOccupant(tile.row, tile.column)?.let { battleUnitId -> battleUnitApi.searchBattleUnitById(battleUnitId) }
            val healthPreview =
                battleUnit?.let {
                    BattlefieldHealthBarPreviewView.PreviewOption.of(
                        remaining = remainingHealth[battleUnit.id] ?: battleUnit.remainingHealthPoints,
                        maximum = battleUnit.maximumHealthPoints,
                    )
                }
            CastGroupTile(row = tile.row, column = tile.column, healthPreview = healthPreview)
        }

    // The battle units of the cast group display the health they would have left, and the vacant tiles are selected.
    private fun displayCastGroup(castGroupTiles: List<CastGroupTile>) {
        castGroupTiles.forEach { tile ->
            if (tile.healthPreview == null) {
                battlefieldView.displayTileSelection(row = tile.row, column = tile.column)
            } else {
                battlefieldView.displayHealthPreview(row = tile.row, column = tile.column, previewOption = tile.healthPreview)
            }
        }
    }

    // The effects that would stay on each battle unit once the ability is cast on the cast group, keyed by battle unit id.
    private fun previewCastOngoingEffects(
        casterBattleUnitId: String,
        abilityId: String,
        castGroup: CastGroupDto,
    ): Map<String, List<String>> =
        battleUnitApi.previewCastOngoingEffects(
            battleUnitId = casterBattleUnitId,
            abilityId = abilityId,
            castGroup = castGroup.tiles.map { tile -> PositionDto(row = tile.row, column = tile.column) },
        )

    private fun displayAttackPreview() {
        battleHudView.displayAttackPreviewView()
        playerCallToActionView.displayCancelAndConfirm(
            onCancelled = { whenIdle { cancelCast() } },
            onConfirmed = { whenIdle { confirmCast() } },
        )
    }

    // The player can only act once the scene shows the current state of the battle.
    private fun whenIdle(action: () -> Unit) {
        if (!timeline.isPlaying) action()
    }

    // Out of a battle (e.g.: while exploring), the battle units are not selected.
    private fun isBattling(): Boolean = battleApi.searchBattle()?.isFinished == false

    private fun isCpuBattleUnit(battleUnit: BattleUnit.Dto): Boolean = playerApi.searchPlayerById(battleUnit.playerId)?.type == Player.Dto.PlayerTypeDto.CPU

    // A CPU battle unit out of the battles moves only to wander: while exploring, or out of the battle in progress.
    private fun isWandering(battleUnit: BattleUnit.Dto): Boolean {
        if (!isCpuBattleUnit(battleUnit)) return false
        val battle = battleApi.searchBattle()?.takeIf { battle -> !battle.isFinished } ?: return true
        return battleUnit.id !in battle.battleUnitIds
    }

    // While talking to an npc, time stops: the tiles can not be selected.
    private fun isTalking(): Boolean = partyApi.searchParty()?.talkingNpcId != null

    // Delegates
    override fun tileSelected(
        row: Int,
        column: Int,
    ) = whenIdle {
        if (isTalking() || tileInterceptor?.invoke(row, column) == true) return@whenIdle
        processTileSelected(row = row, column = column)
    }

    // The abilities can only be cast in a battle.
    override fun abilitySelected(abilityId: String) = whenIdle { if (isBattling()) processAbilitySelected(abilityId) }
}
