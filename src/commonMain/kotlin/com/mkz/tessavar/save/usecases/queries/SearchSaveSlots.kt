package com.mkz.tessavar.save.usecases.queries

import com.mkz.tessavar.save.domain.Save
import com.mkz.tessavar.save.domain.SaveError.IncompatibleSave
import com.mkz.tessavar.save.domain.SaveRepository

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
