package com.mkz.rpg.npc.adapters.presentation

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.adapters.events.OnNpcTalkFinished
import com.mkz.rpg.npc.adapters.events.OnRequestInitialiseNpcs
import com.mkz.rpg.npc.adapters.resources.ResourcesNpcLoader
import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.domain.NpcRepository
import com.mkz.rpg.npc.usecases.commands.InitialiseNpcs
import com.mkz.rpg.npc.usecases.commands.RecruitNpc
import com.mkz.rpg.npc.usecases.queries.SearchNpcAt
import com.mkz.rpg.npc.usecases.queries.SearchNpcById
import com.mkz.rpg.npc.usecases.services.NpcLoader
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.adapters.presentation.UnitApi

class NpcApi(
    battlefieldApi: BattlefieldApi,
    unitApi: UnitApi,
    lootableItemApi: LootableItemApi,
    eventBus: EventBus,
    private val npcLoader: NpcLoader = ResourcesNpcLoader(),
) {
    // Storage
    private val npcRepository: NpcRepository = InMemoryNpcRepository()

    // Commands
    val initialiseNpcs = InitialiseNpcs(npcLoader, npcRepository, battlefieldApi.canBattlefieldTileBeOccupied, unitApi.searchUnitById, lootableItemApi.searchLootableItemById, eventBus)
    val recruitNpc = RecruitNpc(npcRepository, eventBus)

    // Queries
    val searchNpcById = SearchNpcById(npcRepository)
    val searchNpcAt = SearchNpcAt(npcRepository)

    // Events
    private val onRequestInitialiseNpcs = OnRequestInitialiseNpcs(initialiseNpcs, eventBus)
    private val onNpcTalkFinished = OnNpcTalkFinished(recruitNpc, eventBus)

    suspend fun init() {
        npcLoader.initResources()
    }
}
