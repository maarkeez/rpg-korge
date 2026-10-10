package com.mkz.rpg.screen

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
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
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
    private val updateMovementRange =
        UpdateMovementRange(
            searchPosition = battlefieldApi.searchPosition,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )

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
            eventBus.subscribe<BattlefieldHudEvent.AbilityDeselected> {
                clearSelection()
            },
            eventBus.subscribe<BattlefieldHudEvent.SelfAbilityCastPreviewed> { event ->
                displaySelfAbilityCastPreview(event)
            },
            eventBus.subscribe<BattlefieldHudEvent.EnemyAbilityCastPreviewed> { event ->
                displayEnemyAbilityCastPreview(event)
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
        battleUnitInfoView.display(battleUnit, unit, interactive = !isEnemy(battleUnit))
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
        battleUnitInfoView.hide()
        battlefieldView.resetTiles()
        battleHudView.hide()
    }

    private fun displayAbilitySelected(event: BattlefieldHudEvent.SelectedBattleUnitAbility) {
        battlefieldView.resetTiles()
        val battleUnit = battleUnitApi.searchBattleUnitById(event.battleUnitId)!!
        val abilityIndex = battleUnit.abilityCooldowns.keys.indexOf(event.abilityId)
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
    }

    private fun displaySelfAbilityCastPreview(event: BattlefieldHudEvent.SelfAbilityCastPreviewed) {
        val casterBattleUnit = battleUnitApi.searchBattleUnitById(event.casterBattleUnitId)!!
        val casterUnit = unitApi.searchUnitById(casterBattleUnit.unitId)!!
        val ability = abilityApi.searchAbilityById(event.abilityId)!!
        attackPreviewView.display(
            casterBattleUnit = casterBattleUnit,
            casterUnit = casterUnit,
            manaCost = ability.cost,
            receiverBattleUnit = null,
            receiverUnit = null,
            damage = null,
        )
        event.castGroup.tiles.forEach { tilePosition ->
            battlefieldView.displayTileSelection(
                row = tilePosition.row,
                column = tilePosition.column,
            )
        }
        displayAttackPreview()
    }

    private fun displayEnemyAbilityCastPreview(event: BattlefieldHudEvent.EnemyAbilityCastPreviewed) {
        val casterBattleUnit = battleUnitApi.searchBattleUnitById(event.casterBattleUnitId)!!
        val casterUnit = unitApi.searchUnitById(casterBattleUnit.unitId)!!
        val ability = abilityApi.searchAbilityById(event.abilityId)!!
        val targetBattleUnit = battleUnitApi.searchBattleUnitById(event.enemyBattleUnitId)!!
        val targetUnit = unitApi.searchUnitById(targetBattleUnit.unitId)!!
        // TODO: Preview effect applications and display them instead of only calculating damage
        val damage = abilityApi.calculateImmediateDamage(ability.id)
        attackPreviewView.display(
            casterBattleUnit = casterBattleUnit,
            casterUnit = casterUnit,
            manaCost = ability.cost,
            receiverBattleUnit = targetBattleUnit,
            receiverUnit = targetUnit,
            damage = damage,
        )
        event.castGroup.tiles.forEach { tilePosition ->
            battlefieldView.displayTileSelection(
                row = tilePosition.row,
                column = tilePosition.column,
            )
        }
        displayAttackPreview()
    }

    private fun displayAttackPreview() {
        battleHudView.displayAttackPreviewView()
        playerCallToActionView.displayCancelAndConfirm(
            onCancelled = { cancelCast() },
            onConfirmed = { confirmCast() },
        )
    }

    // Delegates
    override fun tileSelected(
        row: Int,
        column: Int,
    ) = processTileSelected(row = row, column = column)

    override fun abilitySelected(abilityId: String) = processAbilitySelected(abilityId)
}
