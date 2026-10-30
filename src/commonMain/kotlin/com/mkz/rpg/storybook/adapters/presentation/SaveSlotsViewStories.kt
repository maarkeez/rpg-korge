package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.SaveSlotsView
import com.mkz.rpg.screen.adapters.presentation.UiText
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [SaveSlotsView], built with its real buttons, with a control per slot for what it holds. A saved slot
 * shows the units that have a portrait, in alphabetical order, as its leader. Tapping a slot, Delete or Back logs it.
 */
object SaveSlotsViewStories : StoryBinding {
    private const val EMPTY = "empty"
    private const val SAVED = "saved"
    private const val INCOMPATIBLE = "incompatible"
    private val STATES = listOf(EMPTY, SAVED, INCOMPATIBLE)

    private val SLOT_ARGS = (1..SaveSlotsView.ROWS).map { number -> "slot$number" }

    private val save =
        StoryDto(
            id = "screen-save-slots--save",
            name = "Save",
            description = "Saving the game from the pause screen: every slot can be chosen, the ones holding a game to overwrite it.",
            args = emptyMap(),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-save-slots",
            group = "Screen",
            title = "Save slots",
            description = "The 3 save slots, each empty, saved with when, its leader and its party size, or incompatible, and Back.",
            controls = SLOT_ARGS.map { slot -> ControlDto.pick(slot, STATES) },
            args = mapOf(SLOT_ARGS[0] to ArgDto.text(EMPTY), SLOT_ARGS[1] to ArgDto.text(SAVED), SLOT_ARGS[2] to ArgDto.text(INCOMPATIBLE)),
            stories = listOf(save),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val unitIds = context.unitIds()
        val slots =
            SLOT_ARGS.mapIndexed { index, slot ->
                val label =
                    when (args.text(slot)) {
                        SAVED ->
                            context.texts[
                                UiText.SAVE_SLOTS_SAVED,
                                "date" to "2026-10-30 18:4$index",
                                "leader" to context.texts.unitName(unitIds[index % unitIds.size]),
                                "members" to index + 2,
                            ]
                        INCOMPATIBLE -> context.texts[UiText.SAVE_SLOTS_INCOMPATIBLE]
                        else -> context.texts[UiText.SAVE_SLOTS_EMPTY]
                    }
                SaveSlotsView.Slot(number = index + 1, label = label, isEnabled = true, isDeletable = false)
            }
        return SaveSlotsView(context.texts).apply {
            setDelegate(
                object : SaveSlotsView.Delegate {
                    override fun saveSlotSelected(number: Int) = context.action("saveSlotSelected", number)

                    override fun saveSlotDeleteSelected(number: Int) = context.action("saveSlotDeleteSelected", number)

                    override fun saveSlotsBackSelected() = context.action("saveSlotsBackSelected")
                },
            )
            display(title = context.texts[UiText.SAVE_SLOTS_SAVE_TITLE], slots = slots)
        }
    }
}
