package com.mkz.rpg.shared.usecases

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.usecases.queries.CalculateImmediateDamage
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.CanMoveTo
import com.mkz.rpg.battleUnit.usecases.queries.HasAllBattleUnitsDefeated
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanMove
import com.mkz.rpg.battleUnit.usecases.services.AbilityExecution
import com.mkz.rpg.battleUnit.usecases.services.DistanceService
import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchShortestPath
import com.mkz.rpg.battlefield.usecases.queries.SearchTilesThatCanBeOccupied
import com.mkz.rpg.cpuBrain.adapters.storage.InMemoryCpuBrainRepository
import com.mkz.rpg.cpuBrain.usecases.queries.HealingNeed
import com.mkz.rpg.cpuBrain.usecases.queries.WhatShouldCast
import com.mkz.rpg.cpuBrain.usecases.queries.WhereShouldMove
import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.usecases.queries.SearchEnemyPlayer
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.usecases.queries.IsTransitionAllowed
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import com.mkz.rpg.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import kotlin.random.Random

/**
 * The in-memory repositories of every subdomain, where tests store their data with the object mothers, and the real queries
 * reading them, wired as the subdomain APIs do. Tests use these queries instead of stubbing them.
 */
class InMemoryQueries(
    random: Random = Random.Default,
) {
    // Storage
    val abilityRepository = InMemoryAbilityRepository()
    val battleRepository = InMemoryBattleRepository()
    val battlefieldRepository = InMemoryBattlefieldRepository()
    val battleUnitRepository = InMemoryBattleUnitRepository()
    val cpuBrainRepository = InMemoryCpuBrainRepository()
    val effectRepository = InMemoryEffectRepository()
    val playerRepository = InMemoryPlayerRepository()
    val terrainRepository = InMemoryTerrainRepository()
    val unitRepository = InMemoryUnitRepository()

    // Effect, unit, player and terrain
    val searchEffectById = SearchEffectById(effectRepository)
    val searchUnitById = SearchUnitById(unitRepository)
    val searchPlayerById = SearchPlayerById(playerRepository)
    val searchEnemyPlayer = SearchEnemyPlayer(playerRepository)
    val searchTerrainById = SearchTerrainById(terrainRepository)
    val isTransitionAllowed = IsTransitionAllowed(terrainRepository)

    // Ability
    val searchAbilityById = SearchAbilityById(abilityRepository)
    val calculateImmediateDamage = CalculateImmediateDamage(abilityRepository, searchEffectById)

    // Battle
    val searchBattle = SearchBattle(battleRepository)

    // Battlefield
    val searchBattlefield = SearchBattlefield(battlefieldRepository)
    val searchOccupant = SearchOccupant(battlefieldRepository)
    val searchPosition = SearchPosition(battlefieldRepository)
    val canBattlefieldTileBeOccupied = CanBattlefieldTileBeOccupied(battlefieldRepository)
    val searchTilesThatCanBeOccupied = SearchTilesThatCanBeOccupied(canBattlefieldTileBeOccupied, battlefieldRepository)
    val searchShortestPath = SearchShortestPath(canBattlefieldTileBeOccupied)

    // Battle unit
    val distanceService = DistanceService()
    val searchBattleUnitById = SearchBattleUnitById(battleUnitRepository)
    val searchBattleUnitsByPlayerId = SearchBattleUnitsByPlayerId(battleUnitRepository)
    val hasAllBattleUnitsDefeated = HasAllBattleUnitsDefeated(battleUnitRepository)
    val canMoveTo = CanMoveTo(battleUnitRepository, searchPosition, searchShortestPath)
    val whereCanMove = WhereCanMove(battleUnitRepository, searchTilesThatCanBeOccupied, canMoveTo)
    val canCastAbility = CanCastAbility(battleUnitRepository, searchAbilityById)
    val whereCanCast =
        WhereCanCast(
            battleUnitRepository,
            searchPosition,
            searchAbilityById,
            searchOccupant,
            searchTilesThatCanBeOccupied,
            distanceService,
            canBattlefieldTileBeOccupied,
        )
    val abilityExecution = AbilityExecution(battleUnitRepository, searchPosition, searchOccupant, searchEffectById, distanceService)

    // CPU brain
    val healingNeed = HealingNeed(searchBattleUnitById, searchUnitById)
    val whatShouldCast =
        WhatShouldCast(
            searchBattleUnitById,
            searchUnitById,
            canCastAbility,
            whereCanCast,
            searchAbilityById,
            searchEffectById,
            searchOccupant,
            searchEnemyPlayer,
            searchBattleUnitsByPlayerId,
            searchPosition,
            healingNeed,
            random,
        )
    val whereShouldMove =
        WhereShouldMove(
            searchBattleUnitsByPlayerId,
            whereCanMove,
            searchBattleUnitById,
            searchPosition,
            searchEnemyPlayer,
            searchUnitById,
            healingNeed,
            searchBattlefield,
            searchTerrainById,
            searchEffectById,
        )
}
