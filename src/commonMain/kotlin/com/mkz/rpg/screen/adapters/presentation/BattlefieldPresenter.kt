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
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.lootDrop.adapters.presentation.LootDropApi
import com.mkz.rpg.lootDrop.domain.LootDropEvent
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.npc.domain.NpcEvent
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.save.domain.SaveEvent
import com.mkz.rpg.screen.adapters.presentation.selection.adapters.presentation.SelectionApi
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection.Dto.CastGroupDto
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.unit.adapters.presentation.UnitApi

class BattlefieldPresenter(
    private val battlefieldView: BattlefieldView,
    private val battleUnitInfoView: BattleUnitInfoView,
    private val castPreviewView: CastPreviewView,
    private val callToActionView: CallToActionView,
    private val hudView: HudView,
    private val battlefieldApi: BattlefieldApi,
    private val battleUnitApi: BattleUnitApi,
    private val playerApi: PlayerApi,
    private val abilityApi: AbilityApi,
    private val effectApi: EffectApi,
    private val unitApi: UnitApi,
    private val battleApi: BattleApi,
    private val partyApi: PartyApi,
    private val npcApi: NpcApi,
    private val lootDropApi: LootDropApi,
    private val timeline: GameSceneTimeline,
    eventBus: EventBus,
) : BattlefieldView.Delegate,
    AbilityButtonView.Delegate {
    private val selectionApi = SelectionApi(battlefieldApi, battleUnitApi, playerApi, battleApi, eventBus)

    private val displayedDroppedItems = mutableMapOf<PositionDto, List<String>>()

    private var tileInterceptor: ((row: Int, column: Int) -> Boolean)? = null

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
            eventBus.subscribe<NpcEvent.NpcRecruited> { event ->
                timeline.play { battlefieldView.removeNpc(event.row, event.column) }
            },
            eventBus.subscribe<PartyEvent.PartyJoined> { event ->
                blinkUnit(event.battleUnitId)
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitMoved> { event ->
                moveUnit(event)
            },
            eventBus.subscribe<PartyEvent.ExplorationStarted> { trailPartyLeader() },
            eventBus.subscribe<SaveEvent.GameLoaded> {
                displayLoadedGame()
                trailPartyLeader()
            },
            eventBus.subscribe<PartyEvent.ExplorationResumed> { trailPartyLeader() },
            eventBus.subscribe<PartyEvent.FormationClosed> { trailPartyLeader() },
            eventBus.subscribe<PartyEvent.ExplorationInterrupted> {
                timeline.play {
                    battlefieldView.stopTrailing()
                    battlefieldView.removeHealthBlinks()
                }
            },
            eventBus.subscribe<BattleUnitEvent.AbilityCastResolved> {
                selectionApi.refreshWalkingRange()
            },
            eventBus.subscribe<BattlefieldEvent.TileOccupied> {
                selectionApi.refreshWalkingRange()
            },
            eventBus.subscribe<BattleUnitEvent.EffectReceived> { event ->
                blinkUnit(event.battleUnitId, isAlongside = !isBattling())
            },
            eventBus.subscribe<BattleUnitEvent.OnPlayerTurnStartedEffectTriggered> { event ->
                blinkUnit(event.battleUnitId)
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitDamaged> { event ->
                if (!isBattling()) blinkHealth(event.battleUnitId)
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitHealed> { event ->
                if (!isBattling()) blinkHealth(event.battleUnitId)
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitVanished> { event ->
                timeline.play { battlefieldView.blinkBattleUnit(row = event.row, column = event.column, unitId = event.unitId) }
            },
            eventBus.subscribe<BattlefieldEvent.OccupantRemoved> { event ->
                removeUnit(event.row, event.column)
                selectionApi.refreshWalkingRange()
            },
            eventBus.subscribe<LootDropEvent.ItemDropped> { event ->
                timeline.play { dropItems(event.row, event.column, event.lootableItemId) }
            },
            eventBus.subscribe<LootDropEvent.DroppedItemLooted> { event ->
                timeline.play { lootDroppedItem(event.row, event.column, event.lootableItemId) }
            },
            eventBus.subscribe<BattleEvent.PlayerTurnStarted> { event ->
                timeline.play { clearSelection() }
                selectionApi.deselectBattleUnit()
                selectPartyLeader(event)
            },
            eventBus.subscribe<BattleEvent.BattleFinished> {
                timeline.play { clearSelection() }
                selectionApi.deselectBattleUnit()
            },
            eventBus.subscribe<SelectionEvent.BattleUnitSelected> { event ->
                timeline.play(displayWalkingRange(event))
            },
            eventBus.subscribe<SelectionEvent.Deselected> {
                timeline.play { clearSelection() }
            },
            eventBus.subscribe<SelectionEvent.AbilitySelected> { event ->
                timeline.play(displayAbilitySelected(event))
            },
            eventBus.subscribe<SelectionEvent.AbilityInfoDisplayed> { event ->
                timeline.play(displayAbilityInfo(event))
            },
            eventBus.subscribe<SelectionEvent.AbilityDeselected> {
                timeline.play { clearSelection() }
            },
            eventBus.subscribe<SelectionEvent.CastOnCasterOrVacantTilePreviewed> { event ->
                timeline.play(displayCastOnCasterOrVacantTilePreview(event))
            },
            eventBus.subscribe<SelectionEvent.CastOnBattleUnitsPreviewed> { event ->
                timeline.play(displayCastOnBattleUnitsPreview(event))
            },
        )

    init {
        selectionApi.initializeSelection()
        battlefieldView.setDelegate(this)
        battleUnitInfoView.setDelegate(this)
    }

    fun displayBattlefield() {
        val battlefield = battlefieldApi.searchBattlefield()!!
        timeline.play { battlefieldView.displayBattlefield(BattlefieldMapper.terrain(battlefield)) }
    }

    fun displayUnit(
        row: Int,
        column: Int,
        battleUnitId: String,
    ) {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return
        timeline.play { battlefieldView.displayBattleUnit(row, column, battleUnit.unitId) }
    }

    private fun displayLoadedGame() {
        val battlefield = battlefieldApi.searchBattlefield() ?: return
        val occupants = battlefield.tiles.mapNotNull { (position, tile) -> tile.occupantId?.let { occupantId -> position to occupantId } }
        val battleUnits = occupants.mapNotNull { (position, occupantId) -> battleUnitApi.searchBattleUnitById(occupantId)?.let { battleUnit -> position to battleUnit } }
        val npcs = occupants.mapNotNull { (position, occupantId) -> npcApi.searchNpcById(occupantId)?.let { npc -> position to npc } }
        val droppedItems = lootDropApi.searchDroppedItems()
        timeline.play {
            battleUnits.forEach { (position, battleUnit) -> battlefieldView.displayBattleUnit(position.row, position.column, battleUnit.unitId) }
            npcs.forEach { (position, npc) -> battlefieldView.displayNpc(position.row, position.column, npc.id, npc.unitId) }
            droppedItems.forEach { (position, lootableItemIds) -> lootableItemIds.forEach { lootableItemId -> dropItems(position.row, position.column, lootableItemId) } }
        }
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

    fun setTileInterceptor(interceptor: (row: Int, column: Int) -> Boolean) {
        tileInterceptor = interceptor
    }

    fun select(battleUnitId: String) = selectionApi.selectBattleUnit(battleUnitId)

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
                path = event.path.map(BattlefieldMapper::position),
                unitId = battleUnit.unitId,
                stepDuration = stepDuration,
                following = following,
            )
        }
        if (isFollowing) timeline.playTogether(step) else timeline.play(step)
    }

    private fun trailPartyLeader() {
        val party = partyApi.searchParty()?.takeIf { party -> party.isExploring } ?: return
        val leaderPosition = party.leaderBattleUnitId?.let(battlefieldApi.searchPosition::invoke) ?: return
        timeline.play { battlefieldView.trail(leaderPosition.row, leaderPosition.column) }
    }

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
                path = event.path.map(BattlefieldMapper::position),
                unitId = unitId,
                stepDuration = stepDuration,
                following = BattlefieldView.Following.NONE,
            )
        }
        if (isBattling) timeline.play(step) else timeline.playAlongside(step)
    }

    private fun dropItems(
        row: Int,
        column: Int,
        lootableItemId: String,
    ) {
        val position = PositionDto(row, column)
        val lootableItemIds = displayedDroppedItems[position].orEmpty() + lootableItemId
        displayedDroppedItems[position] = lootableItemIds
        battlefieldView.displayDroppedItems(row = row, column = column, lootableItemIds = lootableItemIds)
    }

    private suspend fun lootDroppedItem(
        row: Int,
        column: Int,
        lootableItemId: String,
    ) {
        val position = PositionDto(row, column)
        val remainingLootableItemIds = displayedDroppedItems[position].orEmpty() - lootableItemId
        displayedDroppedItems[position] = remainingLootableItemIds
        battlefieldView.lootDroppedItem(row = row, column = column, lootableItemId = lootableItemId, remainingLootableItemIds = remainingLootableItemIds)
    }

    private fun blinkUnit(
        battleUnitId: String,
        isAlongside: Boolean = false,
    ) {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return
        val position = battlefieldApi.searchPosition(battleUnitId) ?: return
        val step: suspend () -> Unit = { battlefieldView.blinkBattleUnit(row = position.row, column = position.column, unitId = battleUnit.unitId) }
        if (isAlongside) timeline.playAlongside(step) else timeline.play(step)
    }

    private fun blinkHealth(battleUnitId: String) {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return
        val position = battlefieldApi.searchPosition(battleUnitId) ?: return
        val previewOption =
            BattlefieldHealthBarPreviewView.PreviewOption.of(
                remaining = battleUnit.remainingHealth,
                maximum = battleUnit.maximumHealth,
            )
        timeline.playAlongside { battlefieldView.blinkHealth(row = position.row, column = position.column, previewOption = previewOption) }
    }

    private fun displayWalkingRange(battleUnitSelectedEvent: SelectionEvent.BattleUnitSelected): suspend () -> Unit {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitSelectedEvent.battleUnitId)!!
        val abilityAvailabilities = battleUnitApi.searchAbilityAvailabilities(battleUnit.id)
        val isBattling = isBattling()
        return {
            if (isBattling) battlefieldView.scrollToTile(row = battleUnitSelectedEvent.tile.row, column = battleUnitSelectedEvent.tile.column)
            battlefieldView.resetTiles()
            battleUnitInfoView.display(
                battleUnit = BattleUnitInfoMapper.battleUnitInfo(battleUnit, isHumanPlayers = !isCpuBattleUnit(battleUnit)),
                turnActions = BattleUnitInfoMapper.turnActions(battleUnit),
                remainingTurns = battleUnit.remainingSummonDuration,
                abilities = BattleUnitInfoMapper.abilities(abilityAvailabilities),
            )
            hudView.displayBattleUnitInfoView()
            battleUnitSelectedEvent.walkingRange.forEach { tile ->
                battlefieldView.displayPotentialMovement(row = tile.row, column = tile.column)
            }
            battlefieldView.displayTileSelection(
                row = battleUnitSelectedEvent.tile.row,
                column = battleUnitSelectedEvent.tile.column,
            )
            val selectableTiles = battleUnitSelectedEvent.walkingRange + battleUnitSelectedEvent.tile
            battlefieldView.dimTilesExcept(selectableTiles.map { tile -> TilePosition(tile.row, tile.column) }.toSet())
        }
    }

    private fun selectPartyLeader(event: BattleEvent.PlayerTurnStarted) {
        if (playerApi.searchPlayerById(event.playerId)?.type != Player.Dto.PlayerTypeDto.HUMAN) return
        val leaderId = partyApi.searchParty()?.leaderBattleUnitId ?: return
        if (leaderId in event.battleUnitIds) selectionApi.selectBattleUnit(leaderId)
    }

    private fun clearSelection() {
        battleUnitInfoView.hide()
        battlefieldView.resetTiles()
        hudView.hide()
    }

    private fun displayAbilitySelected(event: SelectionEvent.AbilitySelected): suspend () -> Unit {
        val battleUnit = battleUnitApi.searchBattleUnitById(event.battleUnitId)!!
        val abilityIndex = battleUnit.knownAbilities.keys.indexOf(event.abilityId)
        val abilityInfo = abilityInfo(event.abilityId)
        return {
            battlefieldView.resetTiles()
            battleUnitInfoView.displayAbilitySelected(abilityIndex)
            battleUnitInfoView.displayAbilityInfo(abilityInfo)
            hudView.displayBattleUnitInfoView()
            event.castGroupsWhereCanCast.forEach { castGroup ->
                castGroup.tiles.forEach { tilePosition ->
                    battlefieldView.displayPotentialCast(
                        row = tilePosition.row,
                        column = tilePosition.column,
                    )
                }
            }
            val selectableTiles = event.castGroupsWhereCanCast.flatMap { castGroup -> castGroup.tiles } + event.casterTile
            battlefieldView.dimTilesExcept(selectableTiles.map { tile -> TilePosition(tile.row, tile.column) }.toSet())
        }
    }

    private fun displayAbilityInfo(event: SelectionEvent.AbilityInfoDisplayed): suspend () -> Unit {
        val battleUnit = battleUnitApi.searchBattleUnitById(event.battleUnitId)!!
        val abilityIndex = battleUnit.knownAbilities.keys.indexOf(event.abilityId)
        val ability = abilityApi.searchAbilityById(event.abilityId)!!
        val reason =
            if (isCpuBattleUnit(battleUnit)) {
                null
            } else {
                AbilityInfoMapper.reason(
                    ability = ability,
                    remainingCooldown = battleUnit.knownAbilities[event.abilityId] ?: 0,
                    remainingMana = battleUnit.remainingMana,
                )
            }
        val abilityInfo = abilityInfo(event.abilityId, reason)
        return {
            battlefieldView.resetTiles()
            battleUnitInfoView.displayAbilitySelected(abilityIndex)
            battleUnitInfoView.displayAbilityInfo(abilityInfo)
            hudView.displayBattleUnitInfoView()
            battlefieldView.dimTilesExcept(setOf(TilePosition(event.casterTile.row, event.casterTile.column)))
        }
    }

    private fun abilityInfo(
        abilityId: String,
        reason: AbilityInfoView.AbilityInfo.Reason? = null,
    ): AbilityInfoView.AbilityInfo =
        AbilityInfoMapper.abilityInfo(
            ability = abilityApi.searchAbilityById(abilityId)!!,
            searchEffect = { effectId -> effectApi.searchEffectById(effectId) },
            searchUnit = { unitId -> unitApi.searchUnitById(unitId) },
            reason = reason,
        )

    private fun displayCastOnCasterOrVacantTilePreview(event: SelectionEvent.CastOnCasterOrVacantTilePreviewed): suspend () -> Unit {
        val casterBattleUnit = battleUnitApi.searchBattleUnitById(event.casterBattleUnitId)!!
        val ability = abilityApi.searchAbilityById(event.abilityId)!!
        val remainingHealth = previewCastRemainingHealth(event.casterBattleUnitId, event.abilityId, event.castGroup)
        val castGroupTiles = castGroupTiles(event.castGroup, remainingHealth)
        val newEffectIds = previewCastOngoingEffects(event.casterBattleUnitId, event.abilityId, event.castGroup)
        return {
            castPreviewView.display(
                abilityId = event.abilityId,
                casterBattleUnit = BattleUnitInfoMapper.battleUnitInfo(casterBattleUnit, isHumanPlayers = !isCpuBattleUnit(casterBattleUnit)),
                manaCost = ability.manaCost,
                recipients = emptyList(),
                casterRemainingHealth = remainingHealth[casterBattleUnit.id] ?: casterBattleUnit.remainingHealth,
                casterNewEffectIds = newEffectIds[casterBattleUnit.id].orEmpty(),
            )
            displayCastGroup(castGroupTiles)
            displayCastPreview()
        }
    }

    private fun displayCastOnBattleUnitsPreview(event: SelectionEvent.CastOnBattleUnitsPreviewed): suspend () -> Unit {
        val casterBattleUnit = battleUnitApi.searchBattleUnitById(event.casterBattleUnitId)!!
        val ability = abilityApi.searchAbilityById(event.abilityId)!!
        val remainingHealth = previewCastRemainingHealth(event.casterBattleUnitId, event.abilityId, event.castGroup)
        val castGroupTiles = castGroupTiles(event.castGroup, remainingHealth)
        val newEffectIds = previewCastOngoingEffects(event.casterBattleUnitId, event.abilityId, event.castGroup)
        val recipients =
            event.recipientBattleUnitIds.map { enemyBattleUnitId ->
                val enemyBattleUnit = battleUnitApi.searchBattleUnitById(enemyBattleUnitId)!!
                CastPreviewView.Recipient(
                    battleUnit = BattleUnitInfoMapper.battleUnitInfo(enemyBattleUnit, isHumanPlayers = !isCpuBattleUnit(enemyBattleUnit)),
                    remainingHealth = remainingHealth[enemyBattleUnitId] ?: enemyBattleUnit.remainingHealth,
                    newEffectIds = newEffectIds[enemyBattleUnitId].orEmpty(),
                )
            }
        return {
            castPreviewView.display(
                abilityId = event.abilityId,
                casterBattleUnit = BattleUnitInfoMapper.battleUnitInfo(casterBattleUnit, isHumanPlayers = !isCpuBattleUnit(casterBattleUnit)),
                manaCost = ability.manaCost,
                recipients = recipients,
                casterRemainingHealth = remainingHealth[casterBattleUnit.id] ?: casterBattleUnit.remainingHealth,
                casterNewEffectIds = newEffectIds[casterBattleUnit.id].orEmpty(),
            )
            displayCastGroup(castGroupTiles)
            displayCastPreview()
        }
    }

    private data class CastGroupTile(
        val row: Int,
        val column: Int,
        val healthPreview: BattlefieldHealthBarPreviewView.PreviewOption?,
    )

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
                        remaining = remainingHealth[battleUnit.id] ?: battleUnit.remainingHealth,
                        maximum = battleUnit.maximumHealth,
                    )
                }
            CastGroupTile(row = tile.row, column = tile.column, healthPreview = healthPreview)
        }

    private fun displayCastGroup(castGroupTiles: List<CastGroupTile>) {
        castGroupTiles.forEach { tile ->
            if (tile.healthPreview == null) {
                battlefieldView.displayTileSelection(row = tile.row, column = tile.column)
            } else {
                battlefieldView.displayHealthPreview(row = tile.row, column = tile.column, previewOption = tile.healthPreview)
            }
        }
    }

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

    private fun displayCastPreview() {
        battleUnitInfoView.hideAbilityInfo()
        hudView.displayCastPreviewView()
        callToActionView.displayCancelAndConfirm(
            onCancelled = { whenIdle { selectionApi.cancelCast() } },
            onConfirmed = { whenIdle { selectionApi.confirmCast() } },
        )
    }

    private fun whenIdle(action: () -> Unit) {
        if (!timeline.isPlaying) action()
    }

    private fun isBattling(): Boolean = battleApi.searchBattle()?.isFinished == false

    private fun isCpuBattleUnit(battleUnit: BattleUnit.Dto): Boolean = playerApi.searchPlayerById(battleUnit.playerId)?.type == Player.Dto.PlayerTypeDto.CPU

    private fun isWandering(battleUnit: BattleUnit.Dto): Boolean {
        if (!isCpuBattleUnit(battleUnit)) return false
        val battle = battleApi.searchBattle()?.takeIf { battle -> !battle.isFinished } ?: return true
        return battleUnit.id !in battle.battleUnitIds
    }

    private fun isTimeStopped(): Boolean = partyApi.searchParty()?.let { party -> party.talkingNpcId != null || party.isInFormation || party.isPaused || party.isReadingQuest } == true

    override fun tileSelected(
        row: Int,
        column: Int,
    ) = whenIdle {
        if (isTimeStopped() || tileInterceptor?.invoke(row, column) == true) return@whenIdle
        selectionApi.processTileSelected(row = row, column = column)
    }

    override fun abilitySelected(abilityId: String) = whenIdle { selectionApi.processAbilitySelected(abilityId) }
}
