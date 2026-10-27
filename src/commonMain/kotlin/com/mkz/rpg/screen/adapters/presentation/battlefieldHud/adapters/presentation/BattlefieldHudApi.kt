package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
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

/** The HUD of a battlefield: each battlefield presenter builds its own, with its own selection. */
class BattlefieldHudApi(
    battlefieldApi: BattlefieldApi,
    battleUnitApi: BattleUnitApi,
    playerApi: PlayerApi,
    battleApi: BattleApi,
    eventBus: EventBus,
) {
    // Storage
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()

    // Services
    private val movementService =
        MovementService(
            searchBattleUnitById = battleUnitApi.searchBattleUnitById,
            searchTilesThatCanBeOccupied = battlefieldApi.searchTilesThatCanBeOccupied,
            canMoveTo = battleUnitApi.canMoveTo,
        )

    // Commands
    val initializeBattlefieldHud = InitializeBattlefieldHud(battlefieldHudRepository)
    val processTileSelected =
        ProcessTileSelected(
            searchOccupant = battlefieldApi.searchOccupant,
            searchBattleUnitById = battleUnitApi.searchBattleUnitById,
            searchPlayerById = playerApi.searchPlayerById,
            searchBattle = battleApi.searchBattle,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )
    val processAbilitySelected =
        ProcessAbilitySelected(
            canCastAbility = battleUnitApi.canCastAbility,
            whereCanCast = battleUnitApi.whereCanCast,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )
    val confirmCast =
        ConfirmCast(
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )
    val cancelCast =
        CancelCast(
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )
    val deselectBattleUnit =
        DeselectBattleUnit(
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )
    val selectBattleUnit =
        SelectBattleUnit(
            searchPosition = battlefieldApi.searchPosition,
            searchBattle = battleApi.searchBattle,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )
    val refreshMovementRange =
        RefreshMovementRange(
            searchPosition = battlefieldApi.searchPosition,
            searchBattle = battleApi.searchBattle,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )
}
