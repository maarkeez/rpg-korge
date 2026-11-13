package com.mkz.rpg.save.usecases.queries

import com.mkz.rpg.save.domain.Save
import com.mkz.rpg.save.domain.SaveError.IncompatibleSave
import com.mkz.rpg.save.domain.SaveRepository

class SearchSaveSlots(
    private val saveRepository: SaveRepository,
) {
    operator fun invoke(): List<Save.Dto.SlotDto> =
        (1..Save.SLOTS).map { slot ->
            try {
                Save.slotOf(slot, saveRepository.search(slot))
            } catch (_: IncompatibleSave) {
                Save.Dto.SlotDto(slot = slot, state = Save.Dto.SlotStateDto.INCOMPATIBLE, summary = null)
            }
        }
}
