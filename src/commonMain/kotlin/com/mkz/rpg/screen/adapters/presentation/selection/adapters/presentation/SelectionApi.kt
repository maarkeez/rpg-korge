package com.mkz.rpg.screen.adapters.presentation.selection.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.screen.adapters.presentation.selection.adapters.storage.InMemorySelectionRepository
import com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands.CancelCast
import com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands.ConfirmCast
import com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands.DeselectBattleUnit
import com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands.InitializeSelection
import com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands.ProcessAbilitySelected
import com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands.ProcessTileSelected
import com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands.RefreshWalkingRange
import com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands.SelectBattleUnit
import com.mkz.rpg.screen.adapters.presentation.selection.usecases.services.WalkingRangeService
import com.mkz.rpg.shared.domain.EventBus

/** What the player has selected on a battlefield: each battlefield presenter builds its own. */
class SelectionApi(
    battlefieldApi: BattlefieldApi,
    battleUnitApi: BattleUnitApi,
    playerApi: PlayerApi,
    battleApi: BattleApi,
    eventBus: EventBus,
) {
    // Storage
    private val selectionRepository = InMemorySelectionRepository()

    // Services
    private val walkingRangeService =
        WalkingRangeService(
            searchBattleUnitById = battleUnitApi.searchBattleUnitById,
            searchVacantTiles = battlefieldApi.searchVacantTiles,
            canWalkTo = battleUnitApi.canWalkTo,
        )

    // Commands
    val initializeSelection = InitializeSelection(selectionRepository)
    val processTileSelected =
        ProcessTileSelected(
            searchOccupant = battlefieldApi.searchOccupant,
            searchBattleUnitById = battleUnitApi.searchBattleUnitById,
            searchPlayerById = playerApi.searchPlayerById,
            searchBattle = battleApi.searchBattle,
            selectionRepository = selectionRepository,
            eventBus = eventBus,
            walkingRangeService = walkingRangeService,
        )
    val processAbilitySelected =
        ProcessAbilitySelected(
            canCastAbility = battleUnitApi.canCastAbility,
            whereCanCast = battleUnitApi.whereCanCast,
            searchBattleUnitById = battleUnitApi.searchBattleUnitById,
            searchPlayerById = playerApi.searchPlayerById,
            searchBattle = battleApi.searchBattle,
            selectionRepository = selectionRepository,
            eventBus = eventBus,
        )
    val confirmCast =
        ConfirmCast(
            selectionRepository = selectionRepository,
            eventBus = eventBus,
        )
    val cancelCast =
        CancelCast(
            selectionRepository = selectionRepository,
            eventBus = eventBus,
        )
    val deselectBattleUnit =
        DeselectBattleUnit(
            selectionRepository = selectionRepository,
            eventBus = eventBus,
        )
    val selectBattleUnit =
        SelectBattleUnit(
            searchPosition = battlefieldApi.searchPosition,
            searchBattle = battleApi.searchBattle,
            selectionRepository = selectionRepository,
            eventBus = eventBus,
            walkingRangeService = walkingRangeService,
        )
    val refreshWalkingRange =
        RefreshWalkingRange(
            searchPosition = battlefieldApi.searchPosition,
            searchBattle = battleApi.searchBattle,
            selectionRepository = selectionRepository,
            eventBus = eventBus,
            walkingRangeService = walkingRangeService,
        )
}
