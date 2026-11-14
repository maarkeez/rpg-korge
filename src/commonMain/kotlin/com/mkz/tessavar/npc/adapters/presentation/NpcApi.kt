package com.mkz.tessavar.npc.adapters.presentation

import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.tessavar.npc.adapters.events.OnNpcTalkFinished
import com.mkz.tessavar.npc.adapters.events.OnRequestPlaceNpcs
import com.mkz.tessavar.npc.adapters.save.NpcSnapshottable
import com.mkz.tessavar.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.tessavar.npc.domain.NpcRepository
import com.mkz.tessavar.npc.usecases.commands.LoadNpcsSnapshot
import com.mkz.tessavar.npc.usecases.commands.PlaceNpcs
import com.mkz.tessavar.npc.usecases.commands.RecruitNpc
import com.mkz.tessavar.npc.usecases.commands.TalkToAdjacentNpc
import com.mkz.tessavar.npc.usecases.queries.SearchAdjacentNpc
import com.mkz.tessavar.npc.usecases.queries.SearchIsMerchant
import com.mkz.tessavar.npc.usecases.queries.SearchNpcById
import com.mkz.tessavar.npc.usecases.queries.SearchNpcsSnapshot
import com.mkz.tessavar.npc.usecases.services.NpcLoader
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.Snapshottable

class NpcApi(
    battlefieldApi: BattlefieldApi,
    lootableItemApi: LootableItemApi,
    partyApi: PartyApi,
    eventBus: EventBus,
    private val npcLoader: NpcLoader,
) {
    private val npcRepository: NpcRepository = InMemoryNpcRepository()

    private val searchNpcsSnapshot = SearchNpcsSnapshot(npcRepository)
    private val loadNpcsSnapshot = LoadNpcsSnapshot(npcRepository, lootableItemApi.searchLootableItemById)
    val snapshottable: Snapshottable = NpcSnapshottable(searchNpcsSnapshot, loadNpcsSnapshot)

    val searchNpcById = SearchNpcById(npcRepository)
    val searchAdjacentNpc = SearchAdjacentNpc(npcRepository)
    val searchIsMerchant = SearchIsMerchant(npcRepository)

    val placeNpcs = PlaceNpcs(npcLoader, npcRepository, battlefieldApi.isTileVacant, lootableItemApi.searchLootableItemById, eventBus)
    val recruitNpc = RecruitNpc(npcRepository, eventBus)
    val talkToAdjacentNpc = TalkToAdjacentNpc(partyApi.searchParty, battlefieldApi.searchPosition, searchAdjacentNpc, eventBus)

    private val onRequestPlaceNpcs = OnRequestPlaceNpcs(placeNpcs, eventBus)
    private val onNpcTalkFinished = OnNpcTalkFinished(recruitNpc, eventBus)

    suspend fun init() {
        npcLoader.initResources()
    }
}
