package com.mkz.rpg.save.adapters.storage

import com.mkz.rpg.save.adapters.json.SaveFile
import com.mkz.rpg.save.adapters.json.SaveSummaryFile
import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.SaveError.IncompatibleSave
import com.mkz.rpg.save.domain.SaveRepository
import korlibs.korge.service.storage.IStorage

/**
 * The saves in KorGE's storage, which keeps them across sessions on every target: the game gives it `views.storage`, a
 * `game.storage` file in the settings folder on the JVM and the `localStorage` of the browser, and the tests an
 * `InmemoryStorage`. The save of slot N is under `save.slot<N>`, and its summary under `save.slot<N>.summary`.
 */
class NativeStorageSaveRepository(
    private val storage: IStorage,
) : SaveRepository {
    override fun save(save: Save) {
        val dto = save.toDto()
        storage[saveKey(dto.slot)] = SaveFile.json.encodeToString(SaveFile.from(dto))
        storage[summaryKey(dto.slot)] = SaveFile.json.encodeToString(SaveSummaryFile.from(dto.summary))
    }

    override fun search(slot: Int): Save? {
        val file = storage.getOrNull(saveKey(slot)) ?: return null
        return decoding(slot) { Save.read(SaveFile.json.decodeFromString<SaveFile>(file).toDto(slot)) }
    }

    override fun searchSummary(slot: Int): Save.Dto.SummaryDto? {
        val file = storage.getOrNull(summaryKey(slot)) ?: return null
        return decoding(slot) { SaveFile.json.decodeFromString<SaveSummaryFile>(file).toDto() }
    }

    override fun delete(slot: Int) {
        storage.remove(saveKey(slot))
        storage.remove(summaryKey(slot))
    }

    private fun <T> decoding(
        slot: Int,
        decode: () -> T,
    ): T =
        try {
            decode()
        } catch (exception: IllegalArgumentException) {
            throw IncompatibleSave(slot, exception)
        }

    private fun saveKey(slot: Int) = "save.slot$slot"

    private fun summaryKey(slot: Int) = "save.slot$slot.summary"
}
