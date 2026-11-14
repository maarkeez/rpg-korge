package com.mkz.tessavar.save.adapters.storage

import com.mkz.tessavar.save.adapters.json.SaveFile
import com.mkz.tessavar.save.domain.Save
import com.mkz.tessavar.save.domain.SaveError.IncompatibleSave
import com.mkz.tessavar.save.domain.SaveRepository
import korlibs.korge.service.storage.IStorage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class NativeStorageSaveRepository(
    private val storage: IStorage,
) : SaveRepository {
    override fun save(save: Save) {
        val dto = save.toDto()
        storage[saveKey(dto.slot)] = SaveFile.json.encodeToString(SaveFile.from(dto))
    }

    override fun search(slot: Int): Save? {
        val file = storage.getOrNull(saveKey(slot)) ?: return null
        return decoding(slot) { Save.read(SaveFile.json.decodeFromString<SaveFile>(file).toDto(slot)) }
    }

    override fun delete(slot: Int) {
        storage.remove(saveKey(slot))
        storage.remove(leftoverSummaryKey(slot))
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

    private fun leftoverSummaryKey(slot: Int) = "save.slot$slot.summary"
}
