package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.GameSceneResourcesLoader
import com.mkz.tessavar.screen.adapters.presentation.Messages
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto

class StoryContext(
    val resourcesLoader: GameSceneResourcesLoader,
    val messages: Messages,
) {
    interface Delegate {
        fun actionCalled(
            name: String,
            arguments: List<String>,
            setArg: Pair<String, ValueDto>?,
        )
    }

    private companion object {
        const val ITEMS_DIRECTORY = "lootableItem"
        const val UNITS_DIRECTORY = "unit"
        const val NPCS_DIRECTORY = "npc"
        const val EFFECTS_DIRECTORY = "effect"
        const val ABILITIES_DIRECTORY = "ability"
        const val TERRAIN_TRANSITIONS_DIRECTORY = "terrain/transitions"
        const val TRANSITION_NAME_SEPARATOR = "_to_"
        const val PORTRAIT_SUFFIX = "_portrait"
        const val IMAGE_EXTENSION = ".png"
    }

    private var delegate: Delegate? = null

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    fun lootableItemIds(): List<String> = imageIds(ITEMS_DIRECTORY)

    fun unitIds(): List<String> = imageIds(UNITS_DIRECTORY).filter { id -> id.endsWith(PORTRAIT_SUFFIX) }.map { id -> id.removeSuffix(PORTRAIT_SUFFIX) }

    fun npcIds(): List<String> = imageIds(NPCS_DIRECTORY).filter { id -> id.endsWith(PORTRAIT_SUFFIX) }.map { id -> id.removeSuffix(PORTRAIT_SUFFIX) }

    fun effectIds(): List<String> = imageIds(EFFECTS_DIRECTORY)

    fun abilityIds(): List<String> = imageIds(ABILITIES_DIRECTORY)

    fun terrainIds(): List<String> =
        terrainTransitionIds()
            .flatMap { transition -> transition.split(TRANSITION_NAME_SEPARATOR) }
            .distinct()
            .sorted()

    fun terrainTransitionIds(): List<String> = imageIds(TERRAIN_TRANSITIONS_DIRECTORY)

    private fun imageIds(directory: String): List<String> =
        resourcesLoader
            .bitmaps(directory)
            .keys
            .filter { name -> name.endsWith(IMAGE_EXTENSION) }
            .map { name -> name.removeSuffix(IMAGE_EXTENSION) }
            .sorted()

    fun action(
        name: String,
        vararg arguments: Any?,
        setArg: Pair<String, ValueDto>? = null,
    ) {
        delegate?.actionCalled(name, arguments.map { argument -> argument.toString() }, setArg)
    }
}
