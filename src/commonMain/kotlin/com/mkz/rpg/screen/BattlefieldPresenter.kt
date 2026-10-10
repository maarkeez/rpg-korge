package com.mkz.rpg.screen

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldCreated
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudRepository
import com.mkz.rpg.screen.battlefieldHud.usecases.commands.CancelCast
import com.mkz.rpg.screen.battlefieldHud.usecases.commands.ConfirmCast
import com.mkz.rpg.screen.battlefieldHud.usecases.commands.InitializeBattlefieldHud
import com.mkz.rpg.screen.battlefieldHud.usecases.commands.ProcessAbilitySelected
import com.mkz.rpg.screen.battlefieldHud.usecases.commands.ProcessTileSelected
import com.mkz.rpg.screen.battlefieldHud.usecases.commands.UpdateMovementRange
import com.mkz.rpg.screen.battlefieldHud.usecases.services.MovementService
import com.mkz.rpg.screen.feedback.FeedbackBeat
import com.mkz.rpg.screen.feedback.FeedbackQueue
import com.mkz.rpg.screen.feedback.FeedbackTiming
import com.mkz.rpg.screen.feedback.WalkPath
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
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
    eventBus: EventBus,
    private val battlefieldHudRepository: BattlefieldHudRepository = InMemoryBattlefieldHudRepository(),
    private val searchTerrainById: SearchTerrainById? = null,
    private val searchEffectById: SearchEffectById? = null,
    private val feedbackTiming: FeedbackTiming = FeedbackTiming.Instant,
    private val onBeatPerformed: (FeedbackBeat) -> Unit = {},
) : BattlefieldView.Delegate,
    AbilityButtonView.Delegate {
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
            searchAbilityAvailability = battleUnitApi.searchAbilityAvailability,
            whereCanCast = battleUnitApi.whereCanCast,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )
    private val abilitySummary = AbilitySummary(searchEffectById)
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
    private val updateMovementRange =
        UpdateMovementRange(
            searchPosition = battlefieldApi.searchPosition,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )

    private var previewedBattleUnitIds: Set<String> = emptySet()
    private val teleportingBattleUnitIds = mutableSetOf<String>()
    private val animate = !feedbackTiming.isInstant

    /** Which way each battle unit looks, kept here so it survives redraws of the battlefield. */
    private val facesRight = mutableMapOf<String, Boolean>()

    /** Where each unit is drawn right now. Beats play after the domain moved on, so the repositories can't tell. */
    private val displayedPositions = mutableMapOf<String, Pair<Int, Int>>()

    /** Where each unit is according to the events handled so far, which is what a later beat needs to know. */
    private val knownPositions = mutableMapOf<String, Pair<Int, Int>>()
    private val defeatedPositions = mutableMapOf<String, Pair<Int, Int>>()
    private var lastCameraFocus: Pair<Int, Int>? = null

    /** Called with true when timed playback starts and false when it ends. */
    var onPlaybackChanged: (Boolean) -> Unit = {}

    private val feedbackQueue =
        FeedbackQueue(
            timing = feedbackTiming,
            performer = ::perform,
            onPlaybackChanged = { playing ->
                playerCallToActionView.setPlaybackLocked(playing)
                onPlaybackChanged(playing)
            },
            onDrained = ::resyncUnits,
        )

    /** True while queued feedback is still playing. Taps are ignored during that time. */
    val isPlayingFeedback: Boolean get() = feedbackQueue.isPlaying

    private val subscriptions =
        listOf(
            eventBus.subscribe<BattlefieldCreated> { displayBattlefield() },
            eventBus.subscribe<BattleUnitEvent.BattleUnitDeployed> { event ->
                knownPositions[event.battleUnitId] = event.row to event.column
                feedbackQueue.enqueue(FeedbackBeat.Deployed(event.battleUnitId, event.row, event.column))
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitTeleported> { event ->
                teleportingBattleUnitIds += event.battleUnitId
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitMoved> { event ->
                knownPositions[event.battleUnitId] = event.toRow to event.toColumn
                enqueueMove(event)
            },
            eventBus.subscribe<BattleUnitEvent.AbilityCasted> { event ->
                knownPositions[event.battleUnitId]?.let { (row, column) -> followIfCpu(event.battleUnitId, row, column) }
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitDefeated> { event ->
                defeatedPositions[event.battleUnitId] = event.defeatedAtRow to event.defeatedAtColumn
                feedbackQueue.enqueue(FeedbackBeat.Defeated(event.battleUnitId, event.defeatedAtRow, event.defeatedAtColumn))
            },
            eventBus.subscribe<BattleUnitEvent.RequestApplyEffect> { event ->
                enqueueSpread(event)
            },
            eventBus.subscribe<BattlefieldEvent.OccupantRemoved> { event ->
                feedbackQueue.enqueue(FeedbackBeat.OccupantRemoved(event.row, event.column))
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitDamaged> { event ->
                feedbackQueue.enqueue(FeedbackBeat.Hit(event.battleUnitId, event.amount, event.remainingHealthPoints))
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitHealed> { event ->
                feedbackQueue.enqueue(FeedbackBeat.Heal(event.battleUnitId, event.amount, event.remainingHealthPoints))
            },
            eventBus.subscribe<BattleUnitEvent.EffectReceived> { event ->
                feedbackQueue.enqueue(
                    if (isOngoingEffect(event.effectId)) {
                        FeedbackBeat.StatusApplied(event.battleUnitId, event.effectId)
                    } else {
                        FeedbackBeat.OverlayRefresh(event.battleUnitId)
                    },
                )
            },
            eventBus.subscribe<BattleEvent.PlayerTurnStarted> { event ->
                lastCameraFocus = null
                feedbackQueue.enqueue(FeedbackBeat.TurnStarted(event.playerId))
            },
            eventBus.subscribe<BattlefieldHudEvent.SelectedBattleUnit> { event ->
                displayMovementRange(event)
            },
            eventBus.subscribe<BattlefieldHudEvent.Idle> {
                clearSelection()
            },
            eventBus.subscribe<BattlefieldHudEvent.SelectedBattleUnitAbility> { event ->
                displayAbilitySelected(event)
            },
            eventBus.subscribe<BattlefieldHudEvent.AbilityDeselected> {
                clearSelection()
            },
            eventBus.subscribe<BattlefieldHudEvent.AbilityUnavailable> { event ->
                battleUnitInfoView.displayAbilityMessage(unavailableReason(event.reason))
            },
            eventBus.subscribe<BattlefieldHudEvent.AbilityCastPreviewed> { event ->
                displayAbilityCastPreview(event)
            },
        )

    init {
        initializeBattlefieldHud.invoke()
        battlefieldView.setDelegate(this)
        battleUnitInfoView.setDelegate(this)
    }

    fun displayBattlefield() {
        val battlefield = battlefieldApi.searchBattlefield()!!
        battlefieldView.displayBattlefield(battlefield)
    }

    fun displayUnit(
        row: Int,
        column: Int,
        battleUnitId: String,
    ) {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return
        if (battleUnit.unitId == "knight") {
            battlefieldView.displayKnightBattleUnit(row, column)
        }
        if (battleUnit.unitId == "rat") {
            battlefieldView.displayRatBattleUnit(row, column)
        }
        if (battleUnit.unitId == "bee") {
            battlefieldView.displayBeeBattleUnit(row, column)
        }
        facesRight[battleUnitId]?.let { battlefieldView.turnUnit(row, column, towardsRight = it) }
        displayOverlay(row, column, battleUnit)
    }

    fun removeUnit(
        row: Int,
        column: Int,
    ) {
        battlefieldView.removeBattleUnit(row, column)
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    /** Advances timed playback. The scene calls this every frame. */
    fun updateFeedback(deltaMs: Double) {
        feedbackQueue.update(deltaMs)
        battlefieldView.advanceFx(deltaMs)
    }

    /** Every beat shows what its event reported, from the data the beat carries. */
    private fun perform(beat: FeedbackBeat) {
        onBeatPerformed(beat)
        when (beat) {
            is FeedbackBeat.Deployed -> {
                displayedPositions[beat.battleUnitId] = beat.row to beat.column
                displayUnit(beat.row, beat.column, beat.battleUnitId)
                if (animate) battlefieldView.playHitFlash(beat.row, beat.column, unitTypeOf(beat.battleUnitId))
            }
            is FeedbackBeat.Move -> performMoveHop(beat)
            is FeedbackBeat.Teleport -> {
                finishMove(beat.battleUnitId, beat.fromRow, beat.fromColumn, beat.toRow, beat.toColumn)
                if (animate) {
                    val unitType = unitTypeOf(beat.battleUnitId)
                    battlefieldView.playHitFlash(beat.fromRow, beat.fromColumn, unitType)
                    battlefieldView.playHitFlash(beat.toRow, beat.toColumn, unitType)
                }
            }
            is FeedbackBeat.Hit -> {
                refreshOverlay(beat.battleUnitId, beat.remainingHealthPoints)
                if (animate) playImpact(beat.battleUnitId, beat.amount, heal = false)
            }
            is FeedbackBeat.Heal -> {
                refreshOverlay(beat.battleUnitId, beat.remainingHealthPoints)
                if (animate) playImpact(beat.battleUnitId, beat.amount, heal = true)
            }
            is FeedbackBeat.StatusApplied -> {
                refreshOverlay(beat.battleUnitId)
                if (animate) positionOf(beat.battleUnitId)?.let { (row, column) -> battlefieldView.playStatusPop(row, column) }
            }
            is FeedbackBeat.OverlayRefresh -> refreshOverlay(beat.battleUnitId)
            is FeedbackBeat.Defeated -> {
                displayedPositions.remove(beat.battleUnitId)
                removeUnit(beat.row, beat.column)
                if (animate) battlefieldView.playPoof(beat.row, beat.column)
            }
            is FeedbackBeat.OccupantRemoved -> removeUnit(beat.row, beat.column)
            is FeedbackBeat.Spread ->
                if (animate) {
                    battlefieldView.playSpark(beat.fromRow, beat.fromColumn, beat.toRow, beat.toColumn, feedbackTiming.spreadMs)
                }
            is FeedbackBeat.CameraFocus -> battlefieldView.centerOn(row = beat.row, column = beat.column)
            is FeedbackBeat.TurnStarted -> {
                clearSelection()
                refreshAllOverlays(beat.playerId)
                centerOnFirstHumanUnit(beat.playerId)
            }
        }
    }

    private fun enqueueMove(event: BattleUnitEvent.BattleUnitMoved) {
        followIfCpu(event.battleUnitId, event.fromRow, event.fromColumn)
        if (teleportingBattleUnitIds.remove(event.battleUnitId)) {
            feedbackQueue.enqueue(FeedbackBeat.Teleport(event.battleUnitId, event.fromRow, event.fromColumn, event.toRow, event.toColumn))
            return
        }
        val from = event.fromRow to event.fromColumn
        val path = if (animate) walkPath(from, event.toRow to event.toColumn) else listOf(event.toRow to event.toColumn)
        var previous = from
        path.forEachIndexed { index, tile ->
            feedbackQueue.enqueue(
                FeedbackBeat.Move(event.battleUnitId, previous.first, previous.second, tile.first, tile.second, hops = path.size, hopIndex = index),
            )
            previous = tile
        }
    }

    private fun walkPath(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
    ): List<Pair<Int, Int>> {
        val canBeOccupied = battlefieldApi.canBattlefieldTileBeOccupied
        return WalkPath.find(from, to) { row, column -> canBeOccupied(row, column) }
    }

    /** The camera follows units of the CPU, so each of its actions happens where the player is looking. */
    private fun followIfCpu(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        if (!animate || lastCameraFocus == row to column) return
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return
        if (!isEnemy(battleUnit)) return
        lastCameraFocus = row to column
        feedbackQueue.enqueue(FeedbackBeat.CameraFocus(row, column))
    }

    /** A death spread shows as a spark from the defeated unit to each neighbour that receives the effect. */
    private fun enqueueSpread(event: BattleUnitEvent.RequestApplyEffect) {
        if (!animate) return
        val sourceId = event.application.source.battleUnitId ?: return
        val from = defeatedPositions[sourceId] ?: return
        val target = event.application.target as? Effect.Dto.EffectTargetDto.Unit ?: return
        val to = knownPositions[target.id] ?: return
        feedbackQueue.enqueue(FeedbackBeat.Spread(from.first, from.second, to.first, to.second))
    }

    private fun performMoveHop(beat: FeedbackBeat.Move) {
        if (beat.isFirstHop) {
            battlefieldView.resetTiles()
            removeUnit(beat.fromRow, beat.fromColumn)
            displayedPositions.remove(beat.battleUnitId)
        }
        rememberFacing(beat.battleUnitId, beat.fromColumn, beat.toColumn)
        if (beat.isLastHop) {
            battlefieldView.hideWalker(beat.battleUnitId)
            finishMove(beat.battleUnitId, beat.fromRow, beat.fromColumn, beat.toRow, beat.toColumn)
        } else {
            battlefieldView.showWalker(unitTypeOf(beat.battleUnitId), beat.toRow, beat.toColumn, beat.battleUnitId)
            facesRight[beat.battleUnitId]?.let { battlefieldView.turnWalker(beat.battleUnitId, towardsRight = it) }
        }
    }

    /** A unit looks the way it last walked; a purely vertical step keeps the side it already faced. */
    private fun rememberFacing(
        battleUnitId: String,
        fromColumn: Int,
        toColumn: Int,
    ) {
        if (toColumn != fromColumn) facesRight[battleUnitId] = toColumn > fromColumn
    }

    private fun finishMove(
        battleUnitId: String,
        fromRow: Int,
        fromColumn: Int,
        toRow: Int,
        toColumn: Int,
    ) {
        rememberFacing(battleUnitId, fromColumn, toColumn)
        battlefieldView.resetTiles()
        removeUnit(fromRow, fromColumn)
        displayedPositions[battleUnitId] = toRow to toColumn
        displayUnit(toRow, toColumn, battleUnitId)
        updateMovementRange(battleUnitId)
    }

    private fun playImpact(
        battleUnitId: String,
        amount: Int,
        heal: Boolean,
    ) {
        val (row, column) = positionOf(battleUnitId) ?: return
        if (!heal) {
            battlefieldView.playHitFlash(row, column, unitTypeOf(battleUnitId))
            battlefieldView.playHitSlash(row, column)
        }
        if (heal && amount > 0) battlefieldView.playHealSparkle(row, column)
        if (amount > 0) battlefieldView.playAmountPop(row, column, amount, heal)
    }

    private fun positionOf(battleUnitId: String): Pair<Int, Int>? = displayedPositions[battleUnitId] ?: battlefieldApi.searchPosition(battleUnitId)?.let { it.row to it.column }

    private fun unitTypeOf(battleUnitId: String): String = battleUnitApi.searchBattleUnitById(battleUnitId)?.unitId ?: battleUnitId

    private fun isOngoingEffect(effectId: String): Boolean {
        val effect = searchEffectById?.invoke(effectId) ?: return false
        return effect.application.type != Effect.Dto.ApplicationDto.ApplicationTypeDto.IMMEDIATELY
    }

    /**
     * Redraws every living unit from the domain once a timed playback ends, so a dropped or reordered beat
     * can't leave the view different from the battle.
     */
    private fun resyncUnits() {
        val currentPlayerId = battleApi.searchBattle()?.currentPlayerTurn ?: return
        val enemyPlayerId = playerApi.searchEnemyPlayer(currentPlayerId)?.id
        battlefieldView.removeAllBattleUnits()
        displayedPositions.clear()
        listOfNotNull(currentPlayerId, enemyPlayerId)
            .flatMap { battleUnitApi.searchBattleUnitsByPlayerId(it) }
            .filter { it.remainingHealthPoints > 0 }
            .forEach { battleUnit ->
                val position = battlefieldApi.searchPosition(battleUnit.id) ?: return@forEach
                displayedPositions[battleUnit.id] = position.row to position.column
                displayUnit(position.row, position.column, battleUnit.id)
            }
    }

    private fun displayMovementRange(selectedBattleUnitEvent: BattlefieldHudEvent.SelectedBattleUnit) {
        val battleUnit = battleUnitApi.searchBattleUnitById(selectedBattleUnitEvent.battleUnitId)!!
        val unit = unitApi.searchUnitById(battleUnit.unitId)!!
        battlefieldView.resetTiles()
        battleUnitInfoView.display(
            battleUnit = battleUnit,
            unit = unit,
            interactive = !isEnemy(battleUnit),
            abilities = battleUnitApi.searchAbilityAvailability(battleUnit.id),
        )
        battleHudView.displayBattleUnitInfoView()
        val style = movementStyle(battleUnit)
        val hazardousTiles = hazardousTiles(selectedBattleUnitEvent.tilesWhereCanBeMoved.map { it.row to it.column })
        selectedBattleUnitEvent.tilesWhereCanBeMoved.forEach { tile ->
            battlefieldView.displayPotentialMovement(
                row = tile.row,
                column = tile.column,
                style = style,
                hazard = (tile.row to tile.column) in hazardousTiles,
            )
        }
        battlefieldView.displayUnitSelection(
            row = selectedBattleUnitEvent.tile.row,
            column = selectedBattleUnitEvent.tile.column,
            isEnemy = isEnemy(battleUnit),
        )
    }

    /** Allies that can be commanded now get the dotted style. Everything else is only inspected. */
    private fun movementStyle(battleUnit: BattleUnit.Dto): MovementStyle {
        val isCommandable = !isEnemy(battleUnit) && battleApi.searchBattle()?.currentPlayerTurn == battleUnit.playerId
        return if (isCommandable) MovementStyle.ALLY else MovementStyle.INSPECT
    }

    private fun hazardousTiles(positions: List<Pair<Int, Int>>): Set<Pair<Int, Int>> {
        val searchTerrain = searchTerrainById ?: return emptySet()
        if (positions.isEmpty()) return emptySet()
        val tiles = battlefieldApi.searchBattlefield()?.tiles ?: return emptySet()
        return positions
            .filter { (row, column) ->
                val terrainId = tiles[PositionDto(row, column)]?.terrainId ?: return@filter false
                searchTerrain(terrainId)?.effectId != null
            }.toSet()
    }

    private fun refreshOverlay(
        battleUnitId: String,
        remainingHealthPoints: Int? = null,
    ) {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return
        val (row, column) = positionOf(battleUnitId) ?: return
        displayOverlay(row, column, battleUnit, remainingHealthPoints = remainingHealthPoints ?: battleUnit.remainingHealthPoints)
    }

    private fun refreshAllOverlays(playerId: String) {
        val enemyPlayerId = playerApi.searchEnemyPlayer(playerId)?.id
        listOfNotNull(playerId, enemyPlayerId)
            .flatMap { battleUnitApi.searchBattleUnitsByPlayerId(it) }
            .forEach { refreshOverlay(it.id) }
    }

    private fun displayOverlay(
        row: Int,
        column: Int,
        battleUnit: BattleUnit.Dto,
        preview: UnitPreviewState? = null,
        remainingHealthPoints: Int = battleUnit.remainingHealthPoints,
    ) {
        val unit = unitApi.searchUnitById(battleUnit.unitId) ?: return
        battlefieldView.displayUnitOverlay(
            row = row,
            column = column,
            state =
                UnitOverlayState(
                    remainingHealthPoints = remainingHealthPoints,
                    maximumHealthPoints = unit.healthPoints,
                    isEnemy = isEnemy(battleUnit),
                    onTurnStartedEffectCount = battleUnit.ongoingEffects.onTurnStarted.size,
                    onDefeatedEffectCount = battleUnit.ongoingEffects.onDefeatedEffects.size,
                    preview = preview,
                ),
        )
    }

    private fun isEnemy(battleUnit: BattleUnit.Dto): Boolean = playerApi.searchPlayerById(battleUnit.playerId)?.type != Player.Dto.PlayerTypeDto.HUMAN

    private fun centerOnFirstHumanUnit(playerId: String) {
        val player = playerApi.searchPlayerById(playerId) ?: return
        if (player.type != Player.Dto.PlayerTypeDto.HUMAN) return
        val firstUnit = battleUnitApi.searchBattleUnitsByPlayerId(playerId).firstOrNull() ?: return
        val position = battlefieldApi.searchPosition(firstUnit.id) ?: return
        battlefieldView.centerOn(row = position.row, column = position.column)
    }

    private fun clearSelection() {
        clearPreviewMarks()
        battleUnitInfoView.hide()
        battlefieldView.resetTiles()
        battleHudView.hide()
    }

    private fun displayAbilitySelected(event: BattlefieldHudEvent.SelectedBattleUnitAbility) {
        battlefieldView.resetTiles()
        val battleUnit = battleUnitApi.searchBattleUnitById(event.battleUnitId)!!
        val abilityIndex = battleUnit.abilityCooldowns.keys.indexOf(event.abilityId)
        battleUnitInfoView.displayAbilitySelected(abilityIndex)
        abilityApi.searchAbilityById(event.abilityId)?.let { ability ->
            battleUnitInfoView.displayAbilityLine("${ability.name} - ${ability.cost} MP - ${abilitySummary(ability)}")
        }
        battleHudView.displayBattleUnitInfoView()
        displayCastTargets(event.castGroupsWhereCanCast, casterId = event.battleUnitId, abilityId = event.abilityId)
        if (event.castGroupsWhereCanCast.isEmpty()) {
            battleUnitInfoView.displayAbilityLine(NO_VALID_TARGET_MESSAGE)
        }
    }

    private fun displayCastTargets(
        castGroups: List<BattlefieldHud.Dto.CastGroupDto>,
        casterId: String,
        abilityId: String,
    ) {
        val validTiles = castGroups.flatMap { it.tiles }.map { it.row to it.column }.toSet()
        castGroups.forEach { castGroup ->
            val groupTiles = castGroup.tiles.map { it.row to it.column }.toSet()
            val lethalTiles = lethalTiles(casterId, abilityId, castGroup)
            groupTiles.forEach { (row, column) ->
                val isOccupied = battlefieldApi.searchOccupant(row, column) != null
                battlefieldView.displayPotentialCast(
                    row = row,
                    column = column,
                    kind = if (isOccupied) CastTargetKind.TARGET_UNIT else CastTargetKind.TARGET_TILE,
                    lethal = (row to column) in lethalTiles,
                    groupEdges =
                        buildSet {
                            if ((row - 1 to column) in groupTiles) add(CastEdge.TOP)
                            if ((row + 1 to column) in groupTiles) add(CastEdge.BOTTOM)
                            if ((row to column - 1) in groupTiles) add(CastEdge.LEFT)
                            if ((row to column + 1) in groupTiles) add(CastEdge.RIGHT)
                        },
                )
            }
        }
        battlefieldView.dimOutside(validTiles)
    }

    /** Tiles of units this cast group would defeat right now, so the player sees lethal options before choosing. */
    private fun lethalTiles(
        casterId: String,
        abilityId: String,
        castGroup: BattlefieldHud.Dto.CastGroupDto,
    ): Set<Pair<Int, Int>> =
        battleUnitApi
            .previewAbilityCast(casterId, abilityId, castGroup.tiles.map { PositionDto(row = it.row, column = it.column) })
            .targets
            .filter { it.isLethal }
            .mapNotNull { target -> battlefieldApi.searchPosition(target.battleUnitId)?.let { it.row to it.column } }
            .toSet()

    private fun unavailableReason(reason: BattlefieldHudEvent.AbilityUnavailable.Reason): String =
        when (reason) {
            is BattlefieldHudEvent.AbilityUnavailable.Reason.OnCooldown -> "On cooldown (${reason.turnsLeft})"
            is BattlefieldHudEvent.AbilityUnavailable.Reason.NotEnoughMana -> "Needs ${reason.cost} MP"
            BattlefieldHudEvent.AbilityUnavailable.Reason.NoCastsLeft -> "Already acted"
        }

    /** Asks the read-only preview query what the cast would do and draws it. Nothing in the battle changes. */
    private fun displayAbilityCastPreview(event: BattlefieldHudEvent.AbilityCastPreviewed) {
        clearPreviewMarks()
        val casterBattleUnit = battleUnitApi.searchBattleUnitById(event.casterBattleUnitId)!!
        val casterUnit = unitApi.searchUnitById(casterBattleUnit.unitId)!!
        val preview =
            battleUnitApi.previewAbilityCast(
                casterId = event.casterBattleUnitId,
                abilityId = event.abilityId,
                castGroup = event.castGroup.tiles.map { tile -> PositionDto(row = tile.row, column = tile.column) },
            )
        // Other valid cast groups stay highlighted, so tapping one of them to switch the preview is discoverable.
        battlefieldView.resetTiles()
        (battlefieldHudRepository.search() as? BattlefieldHud.DisplayAbilityCastPreview)?.let {
            displayCastTargets(it.castGroupsWhereCanCast, casterId = event.casterBattleUnitId, abilityId = event.abilityId)
        }
        val affectedTiles =
            event.castGroup.tiles
                .map { it.row to it.column }
                .toMutableSet()
        val previewStates = preview.targets.associate { it.battleUnitId to unitPreviewState(it) }.toMutableMap()
        preview.triggered.forEach { triggered -> addPendingSpread(triggered, previewStates) }
        previewStates.forEach { (battleUnitId, state) ->
            val position = battlefieldApi.searchPosition(battleUnitId) ?: return@forEach
            val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return@forEach
            affectedTiles += position.row to position.column
            displayOverlay(position.row, position.column, battleUnit, preview = state)
        }
        previewedBattleUnitIds = previewStates.keys.toSet()
        preview.triggered.forEach(::displaySpread)
        affectedTiles.forEach { (row, column) -> battlefieldView.displayCastPreviewTile(row = row, column = column) }
        val summary = CastPreviewSummary { battleUnitId -> battleUnitApi.searchBattleUnitById(battleUnitId)?.let { unitApi.searchUnitById(it.unitId)?.name } }
        attackPreviewView.display(
            casterBattleUnit = casterBattleUnit,
            casterUnit = casterUnit,
            manaAfter = preview.manaAfter,
            cooldownAfter = preview.cooldownAfter,
            lines = summary(preview),
        )
        displayAttackPreview()
    }

    /** Only a spread that is certain to happen (the cast defeats the target) shows a pending status on the neighbour. */
    private fun addPendingSpread(
        triggered: PreviewAbilityCast.TriggeredPreview,
        previewStates: MutableMap<String, UnitPreviewState>,
    ) {
        if (triggered.condition != PreviewAbilityCast.TriggeredPreview.Condition.ON_LETHAL_HIT) return
        val spread = searchEffectById?.invoke(triggered.effectId)
        val isOverTime = spread?.application?.type == Effect.Dto.ApplicationDto.ApplicationTypeDto.ON_TURN_STARTED
        val isOnDefeat = spread?.application?.type == Effect.Dto.ApplicationDto.ApplicationTypeDto.ON_DEFEATED
        if (!isOverTime && !isOnDefeat) return
        triggered.affectedBattleUnitIds.forEach { neighbourId ->
            val current =
                previewStates[neighbourId]
                    ?: battleUnitApi.searchBattleUnitById(neighbourId)?.let { UnitPreviewState(hpAfter = it.remainingHealthPoints, isLethal = false, pendingOnTurnCount = 0, pendingOnDefeatCount = 0) }
                    ?: return@forEach
            previewStates[neighbourId] =
                current.copy(
                    pendingOnTurnCount = current.pendingOnTurnCount + if (isOverTime) 1 else 0,
                    pendingOnDefeatCount = current.pendingOnDefeatCount + if (isOnDefeat) 1 else 0,
                    pendingTurns = if (isOverTime) spread?.application?.onTurnStarted?.duration else current.pendingTurns,
                )
        }
    }

    /** Dashed outlines on the neighbours and a connector with an arrowhead from the target to each adjacent one. */
    private fun displaySpread(triggered: PreviewAbilityCast.TriggeredPreview) {
        val strong = triggered.condition == PreviewAbilityCast.TriggeredPreview.Condition.ON_LETHAL_HIT
        val source = battlefieldApi.searchPosition(triggered.sourceBattleUnitId) ?: return
        triggered.affectedBattleUnitIds.forEach { neighbourId ->
            val neighbour = battlefieldApi.searchPosition(neighbourId) ?: return@forEach
            battlefieldView.displayConditionalTile(row = neighbour.row, column = neighbour.column, strong = strong)
            val towardNeighbour = edgeToward(source, neighbour) ?: return@forEach
            battlefieldView.displaySpreadConnector(source.row, source.column, towardNeighbour, arrowhead = false, strong = strong)
            battlefieldView.displaySpreadConnector(neighbour.row, neighbour.column, edgeToward(neighbour, source)!!, arrowhead = true, strong = strong)
        }
    }

    private fun edgeToward(
        from: PositionDto,
        to: PositionDto,
    ): CastEdge? =
        when {
            to.row < from.row && to.column == from.column -> CastEdge.TOP
            to.row > from.row && to.column == from.column -> CastEdge.BOTTOM
            to.column < from.column && to.row == from.row -> CastEdge.LEFT
            to.column > from.column && to.row == from.row -> CastEdge.RIGHT
            else -> null
        }

    private fun unitPreviewState(target: PreviewAbilityCast.TargetPreview) =
        UnitPreviewState(
            hpAfter = target.hpAfter,
            isLethal = target.isLethal,
            pendingOnTurnCount = target.appliedEffects.count { it.timing == PreviewAbilityCast.AppliedEffectPreview.Timing.OVER_TIME },
            pendingOnDefeatCount = target.appliedEffects.count { it.timing == PreviewAbilityCast.AppliedEffectPreview.Timing.ON_DEATH },
            pendingTurns = target.appliedEffects.firstOrNull { it.timing == PreviewAbilityCast.AppliedEffectPreview.Timing.OVER_TIME }?.turns,
        )

    /** Redraws the units that showed predicted changes, now without them. */
    private fun clearPreviewMarks() {
        previewedBattleUnitIds.forEach(::refreshOverlay)
        previewedBattleUnitIds = emptySet()
    }

    private fun displayAttackPreview() {
        battleHudView.displayAttackPreviewView()
        playerCallToActionView.displayCancelAndConfirm(
            onCancelled = { cancelCast() },
            onConfirmed = { confirmCast() },
        )
    }

    private companion object {
        const val NO_VALID_TARGET_MESSAGE = "No valid target in reach"
    }

    // Delegates
    override fun tileSelected(
        row: Int,
        column: Int,
    ) {
        if (feedbackQueue.isPlaying) return
        processTileSelected(row = row, column = column)
    }

    override fun abilitySelected(abilityId: String) {
        if (feedbackQueue.isPlaying) return
        processAbilitySelected(abilityId)
    }
}
