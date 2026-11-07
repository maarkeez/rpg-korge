package com.mkz.rpg.npc.adapters.presentation

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.adapters.events.OnNpcTalkFinished
import com.mkz.rpg.npc.adapters.events.OnRequestPlaceNpcs
import com.mkz.rpg.npc.adapters.save.NpcSnapshottable
import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.domain.NpcRepository
import com.mkz.rpg.npc.usecases.commands.PlaceNpcs
import com.mkz.rpg.npc.usecases.commands.RecruitNpc
import com.mkz.rpg.npc.usecases.commands.RestoreNpcs
import com.mkz.rpg.npc.usecases.commands.TalkToAdjacentNpc
import com.mkz.rpg.npc.usecases.queries.SearchAdjacentNpc
import com.mkz.rpg.npc.usecases.queries.SearchIsMerchant
import com.mkz.rpg.npc.usecases.queries.SearchNpcById
import com.mkz.rpg.npc.usecases.queries.SearchNpcsSnapshot
import com.mkz.rpg.npc.usecases.services.NpcLoader
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Snapshottable

class NpcApi(
    battlefieldApi: BattlefieldApi,
    lootableItemApi: LootableItemApi,
    partyApi: PartyApi,
    eventBus: EventBus,
    private val npcLoader: NpcLoader,
) {
    // Storage
    private val npcRepository: NpcRepository = InMemoryNpcRepository()

    // Save
    private val searchNpcsSnapshot = SearchNpcsSnapshot(npcRepository)
    private val restoreNpcs = RestoreNpcs(npcRepository, lootableItemApi.searchLootableItemById)
    val snapshottable: Snapshottable = NpcSnapshottable(searchNpcsSnapshot, restoreNpcs)

    // Queries
    val searchNpcById = SearchNpcById(npcRepository)
    val searchAdjacentNpc = SearchAdjacentNpc(npcRepository)
    val searchIsMerchant = SearchIsMerchant(npcRepository)

    // Commands
    val placeNpcs = PlaceNpcs(npcLoader, npcRepository, battlefieldApi.isTileVacant, lootableItemApi.searchLootableItemById, eventBus)
    val recruitNpc = RecruitNpc(npcRepository, eventBus)
    val talkToAdjacentNpc = TalkToAdjacentNpc(partyApi.searchParty, battlefieldApi.searchPosition, searchAdjacentNpc, eventBus)

    // Events
    private val onRequestPlaceNpcs = OnRequestPlaceNpcs(placeNpcs, eventBus)
    private val onNpcTalkFinished = OnNpcTalkFinished(recruitNpc, eventBus)

    suspend fun init() {
        npcLoader.initResources()
    }
}
