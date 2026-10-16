package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldCreated
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.CancelCast
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.ConfirmCast
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.DeselectBattleUnit
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.InitializeBattlefieldHud
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.ProcessAbilitySelected
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.ProcessTileSelected
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands.RefreshMovementRange
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
    playerApi: PlayerApi,
    private val unitApi: UnitApi,
    private val abilityApi: AbilityApi,
    battleApi: BattleApi,
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
    private val refreshMovementRange =
        RefreshMovementRange(
            searchPosition = battlefieldApi.searchPosition,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )

    // The items displayed on each tile. Changed in timeline order, so each step draws the loot the battle had then.
    private val displayedLoot = mutableMapOf<PositionDto, List<String>>()

    private val subscriptions =
        listOf(
            eventBus.subscribe<BattlefieldCreated> { displayBattlefield() },
            eventBus.subscribe<BattleUnitEvent.BattleUnitDeployed> { event ->
                displayUnit(event.row, event.column, event.battleUnitId)
            },
            eventBus.subscribe<BattleUnitEvent.BattleUnitMoved> { event ->
                moveUnit(event)
            },
            // The selected caster is displayed again once its cast has been resolved.
            eventBus.subscribe<BattleUnitEvent.AbilityCastResolved> {
                refreshMovementRange()
            },
            // An occupied tile (e.g.: a battle unit moved or deployed) may close paths of the selected battle unit.
            eventBus.subscribe<BattlefieldEvent.BattlefieldTileOccupied> {
                refreshMovementRange()
            },
            eventBus.subscribe<BattleUnitEvent.EffectReceived> { event ->
                flashUnit(event.battleUnitId)
            },
            eventBus.subscribe<BattleUnitEvent.OnTurnStartedEffectApplied> { event ->
                flashUnit(event.battleUnitId)
            },
            // A vacated tile (e.g.: a battle unit defeated) may open paths of the selected battle unit.
            eventBus.subscribe<BattlefieldEvent.OccupantRemoved> { event ->
                removeUnit(event.row, event.column)
                refreshMovementRange()
            },
            eventBus.subscribe<BattlefieldEvent.DroppedItemPlaced> { event ->
                timeline.play { changeLoot(event.row, event.column) { lootableItemIds -> lootableItemIds + event.lootableItemId } }
            },
            eventBus.subscribe<BattlefieldEvent.DroppedItemPickedUp> { event ->
                timeline.play { changeLoot(event.row, event.column) { lootableItemIds -> lootableItemIds - event.lootableItemId } }
            },
            // The selection is cleared before the turn is announced, and can not be acted on during the new turn.
            eventBus.subscribe<BattleEvent.PlayerTurnStarted> {
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

    private fun moveUnit(event: BattleUnitEvent.BattleUnitMoved) {
        val battleUnit = battleUnitApi.searchBattleUnitById(event.battleUnitId) ?: return
        timeline.play {
            battlefieldView.resetTiles()
            battlefieldView.moveBattleUnit(
                fromRow = event.fromRow,
                fromColumn = event.fromColumn,
                path = event.path,
                unitId = battleUnit.unitId,
            )
        }
    }

    private fun changeLoot(
        row: Int,
        column: Int,
        change: (List<String>) -> List<String>,
    ) {
        val position = PositionDto(row, column)
        val lootableItemIds = change(displayedLoot[position].orEmpty())
        displayedLoot[position] = lootableItemIds
        battlefieldView.displayLoot(row = row, column = column, lootableItemIds = lootableItemIds)
    }

    private fun flashUnit(battleUnitId: String) {
        val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return
        val position = battlefieldApi.searchPosition(battleUnitId) ?: return
        timeline.play { battlefieldView.flashBattleUnit(row = position.row, column = position.column, unitId = battleUnit.unitId) }
    }

    private fun displayMovementRange(selectedBattleUnitEvent: BattlefieldHudEvent.SelectedBattleUnit): suspend () -> Unit {
        val battleUnit = battleUnitApi.searchBattleUnitById(selectedBattleUnitEvent.battleUnitId)!!
        val unit = unitApi.searchUnitById(battleUnit.unitId)!!
        return {
            battlefieldView.resetTiles()
            battleUnitInfoView.display(battleUnit, unit)
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
        return {
            attackPreviewView.display(
                casterBattleUnit = casterBattleUnit,
                casterUnit = casterUnit,
                manaCost = ability.cost,
                receivers = emptyList(),
            )
            event.castGroup.tiles.forEach { tilePosition ->
                battlefieldView.displayTileSelection(
                    row = tilePosition.row,
                    column = tilePosition.column,
                )
            }
            displayAttackPreview()
        }
    }

    private fun displayEnemyAbilityCastPreview(event: BattlefieldHudEvent.EnemyAbilityCastPreviewed): suspend () -> Unit {
        val casterBattleUnit = battleUnitApi.searchBattleUnitById(event.casterBattleUnitId)!!
        val casterUnit = unitApi.searchUnitById(casterBattleUnit.unitId)!!
        val ability = abilityApi.searchAbilityById(event.abilityId)!!
        // TODO: Preview effect applications and display them instead of only calculating damage
        val damage = abilityApi.calculateImmediateDamage(ability.id)
        val receivers =
            event.enemyBattleUnitIds.map { enemyBattleUnitId ->
                val enemyBattleUnit = battleUnitApi.searchBattleUnitById(enemyBattleUnitId)!!
                val enemyUnit = unitApi.searchUnitById(enemyBattleUnit.unitId)!!
                AttackPreviewView.Receiver(battleUnit = enemyBattleUnit, unit = enemyUnit, damage = damage)
            }
        return {
            attackPreviewView.display(
                casterBattleUnit = casterBattleUnit,
                casterUnit = casterUnit,
                manaCost = ability.cost,
                receivers = receivers,
            )
            event.castGroup.tiles.forEach { tilePosition ->
                battlefieldView.displayTileSelection(
                    row = tilePosition.row,
                    column = tilePosition.column,
                )
            }
            displayAttackPreview()
        }
    }

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

    // Delegates
    override fun tileSelected(
        row: Int,
        column: Int,
    ) = whenIdle { processTileSelected(row = row, column = column) }

    override fun abilitySelected(abilityId: String) = whenIdle { processAbilitySelected(abilityId) }
}
