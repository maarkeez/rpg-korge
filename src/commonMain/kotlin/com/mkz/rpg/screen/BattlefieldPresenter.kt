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

    private val subscriptions =
        listOf(
            eventBus.subscribe<BattlefieldCreated> { displayBattlefield() },
            eventBus.subscribe<BattleUnitEvent.BattleUnitDeployed> { event ->
                displayUnit(event.row, event.column, event.battleUnitId)
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitMoved> { event ->
                battlefieldView.resetTiles()
                removeUnit(event.fromRow, event.fromColumn)
                displayUnit(event.toRow, event.toColumn, event.battleUnitId)
                updateMovementRange(event.battleUnitId)
            },
            eventBus.subscribe<BattlefieldEvent.OccupantRemoved> { event ->
                removeUnit(event.row, event.column)
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitDamaged> { event ->
                refreshOverlay(event.battleUnitId)
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitHealed> { event ->
                refreshOverlay(event.battleUnitId)
            },
            eventBus.subscribe<BattleUnitEvent.EffectReceived> { event ->
                refreshOverlay(event.battleUnitId)
            },
            eventBus.subscribe<BattleEvent.PlayerTurnStarted> { event ->
                clearSelection()
                refreshAllOverlays(event.playerId)
                centerOnFirstHumanUnit(event.playerId)
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

    private fun refreshOverlay(battleUnitId: String) {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return
        val position = battlefieldApi.searchPosition(battleUnitId) ?: return
        displayOverlay(position.row, position.column, battleUnit)
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
    ) {
        val unit = unitApi.searchUnitById(battleUnit.unitId) ?: return
        battlefieldView.displayUnitOverlay(
            row = row,
            column = column,
            state =
                UnitOverlayState(
                    remainingHealthPoints = battleUnit.remainingHealthPoints,
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
    ) = processTileSelected(row = row, column = column)

    override fun abilitySelected(abilityId: String) = processAbilitySelected(abilityId)
}
