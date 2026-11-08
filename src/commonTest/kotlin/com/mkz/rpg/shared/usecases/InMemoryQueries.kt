package com.mkz.rpg.shared.usecases

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.queries.AreAllBattleUnitsDefeated
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.CanLearnAbility
import com.mkz.rpg.battleUnit.usecases.queries.CanWalkTo
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailabilities
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanWalk
import com.mkz.rpg.battleUnit.usecases.services.AdjacencyService
import com.mkz.rpg.battleUnit.usecases.services.CastResolution
import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.usecases.queries.IsTileOccupiable
import com.mkz.rpg.battlefield.usecases.queries.IsTileVacant
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefieldSize
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchShortestPath
import com.mkz.rpg.battlefield.usecases.queries.SearchTerrainId
import com.mkz.rpg.battlefield.usecases.queries.SearchVacantTiles
import com.mkz.rpg.battlefield.usecases.queries.SearchWalkingDistance
import com.mkz.rpg.cpuBattleBrain.adapters.storage.InMemoryCpuBattleBrainRepository
import com.mkz.rpg.cpuBattleBrain.usecases.queries.HealingNeed
import com.mkz.rpg.cpuBattleBrain.usecases.queries.IsOutsideTerritory
import com.mkz.rpg.cpuBattleBrain.usecases.queries.SearchDistancesFromHome
import com.mkz.rpg.cpuBattleBrain.usecases.queries.SearchEnemiesWithinReach
import com.mkz.rpg.cpuBattleBrain.usecases.queries.SearchTerrainHealthEffect
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhatShouldCast
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhereShouldWalk
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhereShouldWander
import com.mkz.rpg.cpuWanderBrain.usecases.queries.IsInSight
import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.lootableItem.usecases.queries.SearchSellPrice
import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.usecases.queries.SearchAdjacentNpc
import com.mkz.rpg.npc.usecases.queries.SearchIsMerchant
import com.mkz.rpg.npc.usecases.queries.SearchNpcById
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.usecases.queries.CanMemberLearnAbility
import com.mkz.rpg.party.usecases.queries.SearchParty
import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.usecases.queries.SearchHumanPlayer
import com.mkz.rpg.player.usecases.queries.SearchOpponent
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.usecases.queries.IsTransitionAllowed
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import com.mkz.rpg.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.rpg.unit.usecases.queries.SearchMaximumAbilities
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
    val cpuBattleBrainRepository = InMemoryCpuBattleBrainRepository()
    val effectRepository = InMemoryEffectRepository()
    val lootableItemRepository = InMemoryLootableItemRepository()
    val npcRepository = InMemoryNpcRepository()
    val partyRepository = InMemoryPartyRepository()
    val playerRepository = InMemoryPlayerRepository()
    val terrainRepository = InMemoryTerrainRepository()
    val unitRepository = InMemoryUnitRepository()

    // Effect, unit, player and terrain
    val searchEffectById = SearchEffectById(effectRepository)
    val searchUnitById = SearchUnitById(unitRepository)
    val searchMaximumAbilities = SearchMaximumAbilities()
    val searchPlayerById = SearchPlayerById(playerRepository)
    val searchOpponent = SearchOpponent(playerRepository)
    val searchHumanPlayer = SearchHumanPlayer(playerRepository)
    val searchTerrainById = SearchTerrainById(terrainRepository)
    val isTransitionAllowed = IsTransitionAllowed(terrainRepository)

    // Lootable item
    val searchLootableItemById = SearchLootableItemById(lootableItemRepository)
    val searchSellPrice = SearchSellPrice(lootableItemRepository)

    // Npc
    val searchNpcById = SearchNpcById(npcRepository)
    val searchAdjacentNpc = SearchAdjacentNpc(npcRepository)
    val searchIsMerchant = SearchIsMerchant(npcRepository)

    // Party
    val searchParty = SearchParty(partyRepository)

    // Ability
    val searchAbilityById = SearchAbilityById(abilityRepository)

    // Battle
    val searchBattle = SearchBattle(battleRepository)

    // Battlefield
    val searchBattlefield = SearchBattlefield(battlefieldRepository)
    val searchBattlefieldSize = SearchBattlefieldSize(battlefieldRepository)
    val searchTerrainId = SearchTerrainId(battlefieldRepository)
    val searchOccupant = SearchOccupant(battlefieldRepository)
    val searchPosition = SearchPosition(battlefieldRepository)
    val isTileVacant = IsTileVacant(battlefieldRepository)
    val isTileOccupiable = IsTileOccupiable(battlefieldRepository)
    val searchVacantTiles = SearchVacantTiles(isTileVacant, battlefieldRepository)
    val searchShortestPath = SearchShortestPath(isTileVacant)
    val searchWalkingDistance = SearchWalkingDistance(isTileVacant)

    // Battle unit
    val adjacencyService = AdjacencyService()
    val searchBattleUnitById = SearchBattleUnitById(battleUnitRepository)
    val searchBattleUnitsByPlayerId = SearchBattleUnitsByPlayerId(battleUnitRepository)
    val areAllBattleUnitsDefeated = AreAllBattleUnitsDefeated(battleUnitRepository)
    val canWalkTo = CanWalkTo(battleUnitRepository, searchPosition, searchShortestPath)
    val whereCanWalk = WhereCanWalk(battleUnitRepository, searchVacantTiles, canWalkTo)
    val canCastAbility = CanCastAbility(battleUnitRepository, searchAbilityById)
    val searchAbilityAvailabilities = SearchAbilityAvailabilities(battleUnitRepository, searchAbilityById)
    val canLearnAbility = CanLearnAbility(battleUnitRepository)
    val canMemberLearnAbility = CanMemberLearnAbility(partyRepository, canLearnAbility)
    val whereCanCast =
        WhereCanCast(
            battleUnitRepository,
            searchPosition,
            searchAbilityById,
            searchOccupant,
            searchVacantTiles,
            adjacencyService,
            isTileVacant,
        )
    val castResolution = CastResolution(battleUnitRepository, searchPosition, searchOccupant, searchEffectById, adjacencyService)

    // CPU battle brain
    val healingNeed = HealingNeed(searchBattleUnitById)
    val searchTerrainHealthEffect = SearchTerrainHealthEffect(searchTerrainId, searchTerrainById, searchEffectById)
    val searchDistancesFromHome = SearchDistancesFromHome(isTileOccupiable, searchTerrainHealthEffect)
    val isOutsideTerritory = IsOutsideTerritory(searchPosition)
    val searchEnemiesWithinReach = SearchEnemiesWithinReach(searchBattleUnitById, searchOpponent, searchBattleUnitsByPlayerId, searchPosition, searchWalkingDistance)
    val whatShouldCast =
        WhatShouldCast(
            searchBattleUnitById,
            searchUnitById,
            canCastAbility,
            whereCanCast,
            searchAbilityById,
            searchEffectById,
            searchOccupant,
            searchPosition,
            searchEnemiesWithinReach,
            healingNeed,
            random,
        )
    val whereShouldWalk =
        WhereShouldWalk(
            searchBattleUnitsByPlayerId,
            whereCanWalk,
            searchBattleUnitById,
            searchPosition,
            searchEnemiesWithinReach,
            healingNeed,
            searchTerrainHealthEffect,
        )
    val whereShouldWander =
        WhereShouldWander(
            searchBattleUnitById,
            searchPosition,
            isTileVacant,
            healingNeed,
            searchTerrainHealthEffect,
            random,
        )

    // CPU wander brain
    val isInSight = IsInSight(searchParty, searchPosition, searchBattlefieldSize)
}
