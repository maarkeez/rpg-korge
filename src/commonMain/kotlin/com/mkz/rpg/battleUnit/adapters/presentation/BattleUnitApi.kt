package com.mkz.rpg.battleUnit.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battleUnit.adapters.events.OnAbilityCasted
import com.mkz.rpg.battleUnit.adapters.events.OnPlayerTurnStarted
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.commands.ApplyOnTurnStartedEffects
import com.mkz.rpg.battleUnit.usecases.commands.CastAbility
import com.mkz.rpg.battleUnit.usecases.commands.DeployBattleUnit
import com.mkz.rpg.battleUnit.usecases.commands.MoveBattleUnit
import com.mkz.rpg.battleUnit.usecases.commands.ReceiveAbilityEffects
import com.mkz.rpg.battleUnit.usecases.commands.ReplenishMana
import com.mkz.rpg.battleUnit.usecases.commands.ResetBattleUnitActionsAndReduceCooldowns
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.CanMoveTo
import com.mkz.rpg.battleUnit.usecases.queries.HasAllBattleUnitsDefeated
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanMove
import com.mkz.rpg.battleUnit.usecases.services.DistanceService
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.adapters.presentation.UnitApi

class BattleUnitApi(
    effectApi: EffectApi,
    abilityApi: AbilityApi,
    unitApi: UnitApi,
    playerApi: PlayerApi,
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
) {
    // Storage
    private val battleUnitRepository: BattleUnitRepository = InMemoryBattleUnitRepository()

    // Services
    private val distanceService = DistanceService()

    // Queries
    val searchBattleUnitById = SearchBattleUnitById(battleUnitRepository)
    val canMoveTo =
        CanMoveTo(
            battleUnitRepository,
            battlefieldApi.searchPosition,
            distanceService,
        )
    val whereCanMove =
        WhereCanMove(
            battleUnitRepository,
            battlefieldApi.searchTilesThatCanBeOccupied,
            canMoveTo,
        )
    val whereCanCast =
        WhereCanCast(
            battleUnitRepository,
            battlefieldApi.searchPosition,
            abilityApi.searchAbilityById,
            battlefieldApi.searchOccupant,
            distanceService,
            battlefieldApi.canBattlefieldTileBeOccupied,
        )
    val canCastAbility =
        CanCastAbility(
            battleUnitRepository,
            abilityApi.searchAbilityById,
        )

    // Commands
    val deployBattleUnit =
        DeployBattleUnit(
            battleUnitRepository,
            eventBus,
            unitApi.searchUnitById,
            playerApi.searchPlayerById,
            battlefieldApi.canBattlefieldTileBeOccupied,
        )
    val moveBattleUnit =
        MoveBattleUnit(
            battleUnitRepository,
            eventBus,
            battlefieldApi.searchPosition,
            distanceService,
        )
    val resetBattleUnitActionsAndReduceCooldowns = ResetBattleUnitActionsAndReduceCooldowns(battleUnitRepository)
    val castAbility =
        CastAbility(
            whereCanCast,
            abilityApi.searchAbilityById,
            battleUnitRepository,
            eventBus,
        )
    val receiveAbilityEffects =
        ReceiveAbilityEffects(
            abilityApi.searchAbilityById,
            effectApi.searchEffectById,
            battleUnitRepository,
            eventBus,
            battlefieldApi.searchOccupant,
            unitApi.searchUnitById,
            battlefieldApi.searchPosition,
        )
    val hasAllBattleUnitsDefeated = HasAllBattleUnitsDefeated(battleUnitRepository)
    val searchBattleUnitsByPlayerId = SearchBattleUnitsByPlayerId(battleUnitRepository)
    val applyOnTurnStartedEffects =
        ApplyOnTurnStartedEffects(
            effectApi.searchEffectById,
            searchBattleUnitsByPlayerId,
            battleUnitRepository,
            eventBus,
        )
    val replenishMana =
        ReplenishMana(
            battleUnitRepository,
            unitApi.searchUnitById,
        )

    // Events
    private val onPlayerTurnStarted = OnPlayerTurnStarted(resetBattleUnitActionsAndReduceCooldowns, applyOnTurnStartedEffects, replenishMana, eventBus)
    private val onAbilityCasted = OnAbilityCasted(receiveAbilityEffects, eventBus)
}
