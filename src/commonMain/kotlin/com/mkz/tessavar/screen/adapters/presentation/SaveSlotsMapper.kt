package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.save.domain.Save
import korlibs.time.DateTime
import korlibs.time.format
import korlibs.time.local

object SaveSlotsMapper {
    private const val DATE_FORMAT = "yyyy-MM-dd HH:mm"

    fun slots(
        slots: List<Save.Dto.SlotDto>,
        messages: Messages,
        isLoading: Boolean,
    ): List<SaveSlotsView.Slot> =
        slots.map { slot ->
            SaveSlotsView.Slot(
                number = slot.slot,
                label = label(slot, messages),
                isEnabled = !isLoading || slot.state == Save.Dto.SlotStateDto.SAVED,
                isDeletable = isLoading && slot.state != Save.Dto.SlotStateDto.EMPTY,
            )
        }

    private fun label(
        slot: Save.Dto.SlotDto,
        messages: Messages,
    ): String {
        val summary = slot.summary
        return when {
            slot.state == Save.Dto.SlotStateDto.EMPTY -> messages[UiMessage.SAVE_SLOTS_EMPTY]
            slot.state == Save.Dto.SlotStateDto.INCOMPATIBLE || summary == null -> messages[UiMessage.SAVE_SLOTS_INCOMPATIBLE]
            else ->
                messages[
                    UiMessage.SAVE_SLOTS_SUMMARY,
                    "date" to DateTime.fromUnixMillis(summary.savedAt).local.format(DATE_FORMAT),
                    "leader" to (summary.leaderUnitId?.let(messages::unitName) ?: "-"),
                    "members" to summary.partySize,
                ]
        }
    }
}
