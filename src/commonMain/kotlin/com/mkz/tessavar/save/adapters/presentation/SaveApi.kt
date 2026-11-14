package com.mkz.tessavar.save.adapters.presentation

import com.mkz.tessavar.save.adapters.storage.NativeStorageSaveRepository
import com.mkz.tessavar.save.domain.SaveRepository
import com.mkz.tessavar.save.usecases.commands.DeleteSave
import com.mkz.tessavar.save.usecases.commands.LoadGame
import com.mkz.tessavar.save.usecases.commands.SaveGame
import com.mkz.tessavar.save.usecases.queries.SearchSaveSlots
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.Snapshottable
import korlibs.korge.service.storage.IStorage

class SaveApi(
    storage: IStorage,
    eventBus: EventBus,
    snapshottables: List<Snapshottable>,
    canSave: () -> Boolean,
    leaderUnitId: () -> String?,
    partySize: () -> Int,
    now: () -> Double,
) {
    private val saveRepository: SaveRepository = NativeStorageSaveRepository(storage)

    val saveGame = SaveGame(saveRepository, snapshottables, canSave, leaderUnitId, partySize, now, eventBus)
    val loadGame = LoadGame(saveRepository, snapshottables, eventBus)
    val deleteSave = DeleteSave(saveRepository, eventBus)

    val searchSaveSlots = SearchSaveSlots(saveRepository)
}
