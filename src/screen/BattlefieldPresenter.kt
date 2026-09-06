package screen

import ability.adapters.presentation.AbilityApi
import battle.adapters.presentation.BattleApi
import battle.domain.BattleEvent
import battlefield.adapters.presentation.BattlefieldApi
import battlefield.domain.BattlefieldEvent
import battlefield.domain.BattlefieldEvent.BattlefieldCreated
import battleunit.adapters.presentation.BattleUnitApi
import battleunit.domain.BattleUnitEvent
import player.adapters.presentation.PlayerApi
import screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import screen.battlefieldHud.domain.BattlefieldHudEvent
import screen.battlefieldHud.usecases.commands.CancelCast
import screen.battlefieldHud.usecases.commands.ConfirmCast
import screen.battlefieldHud.usecases.commands.InitializeBattlefieldHud
import screen.battlefieldHud.usecases.commands.ProcessAbilitySelected
import screen.battlefieldHud.usecases.commands.ProcessTileSelected
import screen.battlefieldHud.usecases.commands.UpdateMovementRange
import screen.battlefieldHud.usecases.services.MovementService
import shared.domain.EventBus
import shared.domain.Subscription
import unit.adapters.presentation.UnitApi

class BattlefieldPresenter(
    private val battlefieldView: BattlefieldView,
    private val battleUnitInfoView: BattleUnitInfoView,
    private val attackPreviewView: AttackPreviewView,
    private val battleHudView: BattleHudView,
    private val battlefieldApi: BattlefieldApi,
    private val battleUnitApi: BattleUnitApi,
    playerApi: PlayerApi,
    private val unitApi: UnitApi,
    private val abilityApi: AbilityApi,
    battleApi: BattleApi,
    eventBus: EventBus,
) : BattlefieldView.Delegate, AbilityButtonView.Delegate, AttackPreviewView.Delegate {

    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val movementService = MovementService(
        battlefieldApi,
        battleUnitApi
    )
    private val initializeBattlefieldHud = InitializeBattlefieldHud(battlefieldHudRepository)
    private val processTileSelected = ProcessTileSelected(
        battlefieldApi,
        battleUnitApi,
        playerApi,
        battleApi,
        battlefieldHudRepository,
        eventBus,
        movementService,
    )
    private val processAbilitySelected = ProcessAbilitySelected(
        battleUnitApi,
        battlefieldHudRepository,
        eventBus,
    )
    private val confirmCast = ConfirmCast(
        battleUnitApi,
        battlefieldHudRepository,
        eventBus
    )
    private val cancelCast = CancelCast(
        battlefieldHudRepository,
        eventBus
    )
    private val updateMovementRange = UpdateMovementRange(
        battlefieldApi,
        battlefieldHudRepository,
        eventBus,
        movementService,
    )

    private val subscriptions = listOf(
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
        eventBus.subscribe<BattleEvent.PlayerTurnStarted> {
            clearSelection()
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
        attackPreviewView.setDelegate(this)
    }

    fun displayBattlefield() {
        val battlefield = battlefieldApi.searchBattlefield()!!
        battlefieldView.displayBattlefield(battlefield)
    }

    fun displayUnit(row: Int, column: Int, battleUnitId: String) {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return
        if(battleUnit.unitId == "knight"){
            battlefieldView.displayKnightBattleUnit(row, column)
        }
        if(battleUnit.unitId == "rat"){
            battlefieldView.displayRatBattleUnit(row, column)
        }
    }

    fun removeUnit(row: Int, column: Int) {
        battlefieldView.removeBattleUnit(row, column)
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    override fun tileSelected(row: Int, column: Int) {
        processTileSelected(row = row, column = column)
    }

    private fun displayMovementRange(selectedBattleUnitEvent: BattlefieldHudEvent.SelectedBattleUnit) {
        val battleUnit = battleUnitApi.searchBattleUnitById(selectedBattleUnitEvent.battleUnitId)!!
        val unit = unitApi.searchUnitById(battleUnit.unitId)!!
        battlefieldView.resetTiles()
        battleUnitInfoView.display(battleUnit, unit)
        battleHudView.displayBattleUnitInfoView()
        selectedBattleUnitEvent.tilesWhereCanBeMoved.forEach { tile ->
            battlefieldView.displayPotentialMovement(row = tile.row, column = tile.column)
        }
        battlefieldView.displayTileSelection(
            row = selectedBattleUnitEvent.tile.row,
            column = selectedBattleUnitEvent.tile.column
        )
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
        event.tilesWhereCanCast.forEach { tilePosition ->
            battlefieldView.displayPotentialCast(
                row = tilePosition.row,
                column = tilePosition.column
            )
        }
    }

    override fun abilitySelected(abilityId: String) {
        processAbilitySelected(abilityId)
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
            damage = null
        )
        battlefieldView.displayTileSelection(
            row = event.castTile.row,
            column = event.castTile.column
        )
        battleHudView.displayAttackPreviewView()
    }

    private fun displayEnemyAbilityCastPreview(event: BattlefieldHudEvent.EnemyAbilityCastPreviewed) {
        val casterBattleUnit = battleUnitApi.searchBattleUnitById(event.casterBattleUnitId)!!
        val casterUnit = unitApi.searchUnitById(casterBattleUnit.unitId)!!
        val ability = abilityApi.searchAbilityById(event.abilityId)!!

        val targetBattleUnit = battleUnitApi.searchBattleUnitById(event.enemyBattleUnitId)!!
        val targetUnit = unitApi.searchUnitById(targetBattleUnit.unitId)!!
        val damage = abilityApi.calculateImmediateDamage(ability.id)
        attackPreviewView.display(
            casterBattleUnit = casterBattleUnit,
            casterUnit = casterUnit,
            manaCost = ability.cost,
            receiverBattleUnit = targetBattleUnit,
            receiverUnit = targetUnit,
            damage = damage
        )
        battlefieldView.displayTileSelection(
            row = event.castTile.row,
            column = event.castTile.column
        )
        battleHudView.displayAttackPreviewView()
    }

    override fun castConfirmed() {
        confirmCast()
    }

    override fun castCancelled() {
        cancelCast()
    }
}
