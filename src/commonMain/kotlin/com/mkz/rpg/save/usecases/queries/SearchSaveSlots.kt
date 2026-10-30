package com.mkz.rpg.save.usecases.queries

import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.SaveError.IncompatibleSave
import com.mkz.rpg.save.domain.SaveRepository

/**
 * Every save slot, from the first: empty, saved with its summary, or incompatible. Only the summaries are read, so a save
 * naming what the game no longer has shows as saved, until it is loaded.
 */
class SearchSaveSlots(
    private val saveRepository: SaveRepository,
) {
    operator fun invoke(): List<Save.Dto.SlotDto> =
        (1..Save.SLOTS).map { slot ->
            try {
                Save.slotOf(slot, saveRepository.searchSummary(slot))
            } catch (_: IncompatibleSave) {
                Save.Dto.SlotDto(slot = slot, state = Save.Dto.SlotStateDto.INCOMPATIBLE, summary = null)
            }
        }
}
