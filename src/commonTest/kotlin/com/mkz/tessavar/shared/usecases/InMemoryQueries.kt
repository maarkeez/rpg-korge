package com.mkz.tessavar.shared.usecases

import com.mkz.tessavar.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.tessavar.ability.usecases.queries.SearchAbilityById
import com.mkz.tessavar.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.tessavar.battle.usecases.queries.SearchBattle
import com.mkz.tessavar.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.tessavar.battleUnit.usecases.queries.CanCastAbility
import com.mkz.tessavar.battleUnit.usecases.queries.CanLearnAbility
import com.mkz.tessavar.battleUnit.usecases.queries.CanWalkTo
import com.mkz.tessavar.battleUnit.usecases.queries.SearchAbilityAvailabilities
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.tessavar.battleUnit.usecases.queries.WhereCanCast
import com.mkz.tessavar.battleUnit.usecases.queries.WhereCanWalk
import com.mkz.tessavar.battleUnit.usecases.services.AdjacencyService
import com.mkz.tessavar.battleUnit.usecases.services.CastResolution
import com.mkz.tessavar.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.tessavar.battlefield.usecases.queries.IsTileOccupiable
import com.mkz.tessavar.battlefield.usecases.queries.IsTileVacant
import com.mkz.tessavar.battlefield.usecases.queries.SearchBattlefield
import com.mkz.tessavar.battlefield.usecases.queries.SearchBattlefieldSize
import com.mkz.tessavar.battlefield.usecases.queries.SearchOccupant
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.battlefield.usecases.queries.SearchShortestPath
import com.mkz.tessavar.battlefield.usecases.queries.SearchTerrainId
import com.mkz.tessavar.battlefield.usecases.queries.SearchVacantTiles
import com.mkz.tessavar.battlefield.usecases.queries.SearchWalkingDistance
import com.mkz.tessavar.cpuBattleBrain.adapters.storage.InMemoryCpuBattleBrainRepository
import com.mkz.tessavar.cpuBattleBrain.usecases.queries.HealingNeed
import com.mkz.tessavar.cpuBattleBrain.usecases.queries.IsOutsideTerritory
import com.mkz.tessavar.cpuBattleBrain.usecases.queries.SearchDistancesFromHome
import com.mkz.tessavar.cpuBattleBrain.usecases.queries.SearchEnemiesWithinReach
import com.mkz.tessavar.cpuBattleBrain.usecases.queries.SearchTerrainHealthEffect
import com.mkz.tessavar.cpuBattleBrain.usecases.queries.WhatShouldCast
import com.mkz.tessavar.cpuBattleBrain.usecases.queries.WhereShouldWalk
import com.mkz.tessavar.cpuBattleBrain.usecases.queries.WhereShouldWander
import com.mkz.tessavar.cpuWanderBrain.usecases.queries.IsInSight
import com.mkz.tessavar.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.tessavar.effect.usecases.queries.SearchEffectById
import com.mkz.tessavar.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.lootableItem.usecases.queries.SearchSellPrice
import com.mkz.tessavar.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.tessavar.npc.usecases.queries.SearchAdjacentNpc
import com.mkz.tessavar.npc.usecases.queries.SearchIsMerchant
import com.mkz.tessavar.npc.usecases.queries.SearchNpcById
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.usecases.queries.CanMemberLearnAbility
import com.mkz.tessavar.party.usecases.queries.SearchParty
import com.mkz.tessavar.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.tessavar.player.usecases.queries.SearchHumanPlayer
import com.mkz.tessavar.player.usecases.queries.SearchOpponent
import com.mkz.tessavar.player.usecases.queries.SearchPlayerById
import com.mkz.tessavar.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.tessavar.terrain.usecases.queries.HasTerrainTransition
import com.mkz.tessavar.terrain.usecases.queries.SearchTerrainById
import com.mkz.tessavar.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.tessavar.unit.usecases.queries.SearchMaximumAbilities
import com.mkz.tessavar.unit.usecases.queries.SearchUnitById
import kotlin.random.Random

class InMemoryQueries(
    random: Random = Random.Default,
) {
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

    val searchEffectById = SearchEffectById(effectRepository)
    val searchUnitById = SearchUnitById(unitRepository)
    val searchMaximumAbilities = SearchMaximumAbilities()
    val searchPlayerById = SearchPlayerById(playerRepository)
    val searchOpponent = SearchOpponent(playerRepository)
    val searchHumanPlayer = SearchHumanPlayer(playerRepository)
    val searchTerrainById = SearchTerrainById(terrainRepository)
    val hasTerrainTransition = HasTerrainTransition(terrainRepository)

    val searchLootableItemById = SearchLootableItemById(lootableItemRepository)
    val searchSellPrice = SearchSellPrice(lootableItemRepository)

    val searchNpcById = SearchNpcById(npcRepository)
    val searchAdjacentNpc = SearchAdjacentNpc(npcRepository)
    val searchIsMerchant = SearchIsMerchant(npcRepository)

    val searchParty = SearchParty(partyRepository)

    val searchAbilityById = SearchAbilityById(abilityRepository)

    val searchBattle = SearchBattle(battleRepository)

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

    val adjacencyService = AdjacencyService()
    val searchBattleUnitById = SearchBattleUnitById(battleUnitRepository)
    val searchBattleUnitsByPlayerId = SearchBattleUnitsByPlayerId(battleUnitRepository)
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

    val isInSight = IsInSight(searchParty, searchPosition, searchBattlefieldSize)
}
