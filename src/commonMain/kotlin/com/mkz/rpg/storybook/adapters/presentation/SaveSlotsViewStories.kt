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
    private const val LOADING = "loading"

    private val save =
        StoryDto(
            id = "screen-save-slots--save",
            name = "Save",
            description = "Saving the game from the pause screen: every slot can be chosen, the ones holding a game to overwrite it.",
            args = emptyMap(),
        )
    private val load =
        StoryDto(
            id = "screen-save-slots--load",
            name = "Load",
            description = "Loading a game from the menu: only a saved slot can be chosen, faded otherwise, and every slot that is not empty has Delete.",
            args = mapOf(LOADING to ArgDto.boolean(true)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-save-slots",
            group = "Screen",
            title = "Save slots",
            description = "The 3 save slots, each empty, saved with when, its leader and its party size, or incompatible, and Back.",
            controls = SLOT_ARGS.map { slot -> ControlDto.pick(slot, STATES) } + ControlDto.boolean(LOADING),
            args = mapOf(SLOT_ARGS[0] to ArgDto.text(EMPTY), SLOT_ARGS[1] to ArgDto.text(SAVED), SLOT_ARGS[2] to ArgDto.text(INCOMPATIBLE), LOADING to ArgDto.boolean(false)),
            stories = listOf(save, load),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val unitIds = context.unitIds()
        val isLoading = args.boolean(LOADING)
        val slots =
            SLOT_ARGS.mapIndexed { index, slot ->
                val state = args.text(slot)
                val label =
                    when (state) {
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
                SaveSlotsView.Slot(number = index + 1, label = label, isEnabled = !isLoading || state == SAVED, isDeletable = isLoading && state != EMPTY)
            }
        return SaveSlotsView(context.texts).apply {
            setDelegate(
                object : SaveSlotsView.Delegate {
                    override fun saveSlotSelected(number: Int) = context.action("saveSlotSelected", number)

                    override fun saveSlotDeleteSelected(number: Int) = context.action("saveSlotDeleteSelected", number)

                    override fun saveSlotsBackSelected() = context.action("saveSlotsBackSelected")
                },
            )
            display(title = context.texts[if (isLoading) UiText.SAVE_SLOTS_LOAD_TITLE else UiText.SAVE_SLOTS_SAVE_TITLE], slots = slots)
        }
    }
}
