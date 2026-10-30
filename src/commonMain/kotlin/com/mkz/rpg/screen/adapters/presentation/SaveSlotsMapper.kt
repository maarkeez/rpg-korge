package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.save.domain.Save
import korlibs.time.DateTime
import korlibs.time.format
import korlibs.time.local

/**
 * What each save slot shows: Empty, Incompatible save, or when it was saved, in local time (e.g.: 2026-10-30 18:42), the
 * leader of the party and its size. To save, every slot can be chosen; to load, only a saved one, and every slot that is
 * not empty can be deleted.
 */
object SaveSlotsMapper {
    private const val DATE_FORMAT = "yyyy-MM-dd HH:mm"

    fun slots(
        slots: List<Save.Dto.SlotDto>,
        texts: Texts,
        isLoading: Boolean,
    ): List<SaveSlotsView.Slot> =
        slots.map { slot ->
            SaveSlotsView.Slot(
                number = slot.slot,
                label = label(slot, texts),
                isEnabled = !isLoading || slot.state == Save.Dto.SlotStateDto.SAVED,
                isDeletable = isLoading && slot.state != Save.Dto.SlotStateDto.EMPTY,
            )
        }

    private fun label(
        slot: Save.Dto.SlotDto,
        texts: Texts,
    ): String {
        val summary = slot.summary
        return when {
            slot.state == Save.Dto.SlotStateDto.EMPTY -> texts[UiText.SAVE_SLOTS_EMPTY]
            slot.state == Save.Dto.SlotStateDto.INCOMPATIBLE || summary == null -> texts[UiText.SAVE_SLOTS_INCOMPATIBLE]
            else ->
                texts[
                    UiText.SAVE_SLOTS_SAVED,
                    "date" to DateTime.fromUnixMillis(summary.savedAt).local.format(DATE_FORMAT),
                    "leader" to (summary.leaderUnitId?.let(texts::unitName) ?: "-"),
                    "members" to summary.partySize,
                ]
        }
    }
}
