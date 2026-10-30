package com.mkz.rpg.save.adapters.presentation

import com.mkz.rpg.save.adapters.storage.NativeStorageSaveRepository
import com.mkz.rpg.save.domain.SaveRepository
import com.mkz.rpg.save.usecases.commands.SaveGame
import com.mkz.rpg.save.usecases.queries.SearchSaveSlots
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Snapshottable
import korlibs.korge.service.storage.IStorage
import korlibs.time.DateTime

/**
 * Saves the game into its slots, kept in the [storage] (e.g.: `views.storage`). It depends on no other subdomain: the
 * scene hands it the snapshottables of the subdomains holding runtime state, in the order they are initialised, and
 * tells it whether the game can be saved, and the summary of the party. The menu builds it with none, only to list and
 * delete the slots.
 */
class SaveApi(
    storage: IStorage,
    eventBus: EventBus,
    snapshottables: List<Snapshottable> = emptyList(),
    canSave: () -> Boolean = { false },
    leaderUnitId: () -> String? = { null },
    partySize: () -> Int = { 0 },
    now: () -> Double = { DateTime.nowUnixMillis() },
) {
    // Storage
    private val saveRepository: SaveRepository = NativeStorageSaveRepository(storage)

    // Commands
    val saveGame = SaveGame(saveRepository, snapshottables, canSave, leaderUnitId, partySize, now, eventBus)

    // Queries
    val searchSaveSlots = SearchSaveSlots(saveRepository)
}
