package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.Texts
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto

/**
 * What the stories build their views with, as the game does: its images, loaded once, and its texts. The callbacks of
 * the views are handed to [action], which logs them in the Actions.
 */
class StoryContext(
    val resourcesLoader: BattleSceneResourcesLoader,
    val texts: Texts,
) {
    interface Delegate {
        /** The view on show made a callback, which may change an arg, e.g.: `shopSlotSelected(3)` sets `selectedSlot`. */
        fun actionCalled(
            name: String,
            arguments: List<String>,
            setArg: Pair<String, ArgDto>?,
        )
    }

    private companion object {
        const val ITEMS_DIRECTORY = "lootableItem"
        const val UNITS_DIRECTORY = "unit"
        const val PORTRAIT_SUFFIX = "_portrait"
        const val IMAGE_EXTENSION = ".png"
    }

    private var delegate: Delegate? = null

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Every lootable item that has an icon, in alphabetical order, e.g.: the options of a pick control of an item. */
    fun lootableItemIds(): List<String> = imageIds(ITEMS_DIRECTORY)

    /** Every unit that has a portrait, in alphabetical order, e.g.: the options of a pick control of a unit. */
    fun unitIds(): List<String> = imageIds(UNITS_DIRECTORY).filter { id -> id.endsWith(PORTRAIT_SUFFIX) }.map { id -> id.removeSuffix(PORTRAIT_SUFFIX) }

    private fun imageIds(directory: String): List<String> =
        resourcesLoader
            .bitmaps(directory)
            .keys
            .filter { name -> name.endsWith(IMAGE_EXTENSION) }
            .map { name -> name.removeSuffix(IMAGE_EXTENSION) }
            .sorted()

    /**
     * Logs a callback of the view on show, by its name and its arguments, e.g.: `action("shopSlotSelected", 3)`. It may
     * also change an arg, as the game would, e.g.: `setArg = "selectedSlot" to ArgDto.text("3")`, to a value its control
     * accepts.
     */
    fun action(
        name: String,
        vararg arguments: Any?,
        setArg: Pair<String, ArgDto>? = null,
    ) {
        delegate?.actionCalled(name, arguments.map { argument -> argument.toString() }, setArg)
    }
}
