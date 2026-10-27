package com.mkz.rpg.storybook.domain

import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.StorybookError.ComponentWithoutStories
import com.mkz.rpg.storybook.domain.StorybookError.EmptyCatalogue
import com.mkz.rpg.storybook.domain.StorybookError.InvalidComponentId
import com.mkz.rpg.storybook.domain.StorybookError.InvalidStoryId
import com.mkz.rpg.storybook.domain.StorybookError.RepeatedComponentId
import com.mkz.rpg.storybook.domain.StorybookError.RepeatedStoryId
import com.mkz.rpg.storybook.domain.StorybookError.StoryNotFound
import com.mkz.rpg.storybook.domain.StorybookEvent.CatalogueInitialised
import com.mkz.rpg.storybook.domain.StorybookEvent.StorySelected
import kotlin.jvm.JvmInline

/**
 * The views of the game shown in isolation, and the story on show. The catalogue lists the components, each a view of
 * the game (e.g.: the health bar), with its stories, each a state of the view (e.g.: full). Their ids are kept in the
 * links shared, so they are stable: renaming a component or a story keeps its id.
 */
@ConsistentCopyVisibility
data class Storybook private constructor(
    private val components: Components,
    private val selectedStoryId: StoryId,
    private val events: Set<StorybookEvent>,
) {
    companion object {
        private val KEBAB_CASE = Regex("[a-z0-9]+(-[a-z0-9]+)*")
        private const val STORY_SEPARATOR = "--"

        /** Opens the catalogue on the requested story, or on its first story when none is requested or it is not in the catalogue. */
        fun create(
            components: List<ComponentDto>,
            requestedStoryId: String?,
        ): Storybook {
            val catalogue = Components.fromDtos(components)
            val selectedStoryId = catalogue.storyIdOrNull(requestedStoryId) ?: catalogue.firstStoryId()
            return Storybook(
                components = catalogue,
                selectedStoryId = selectedStoryId,
                events = setOf(CatalogueInitialised, StorySelected(storyId = selectedStoryId.value)),
            )
        }
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            components = components.toDto(),
            selectedStoryId = selectedStoryId.value,
        )

    /** Shows the story. Nothing changes when it is already on show. Fails when it is not in the catalogue. */
    fun selectStory(storyId: String): Storybook {
        val story = components.storyIdOrNull(storyId) ?: throw StoryNotFound(storyId)
        if (story == selectedStoryId) return this
        return copy(selectedStoryId = story, events = events + StorySelected(storyId = story.value))
    }

    private data class Components(
        val value: List<Component>,
    ) {
        init {
            if (value.isEmpty()) throw EmptyCatalogue()
            value.map(Component::id).firstRepeatedOrNull()?.let { componentId -> throw RepeatedComponentId(componentId.value) }
            stories().map(Story::id).firstRepeatedOrNull()?.let { storyId -> throw RepeatedStoryId(storyId.value) }
        }

        companion object {
            fun fromDtos(dtos: List<ComponentDto>): Components = Components(dtos.map(::Component))
        }

        fun storyIdOrNull(storyId: String?): StoryId? = stories().map(Story::id).firstOrNull { it.value == storyId }

        fun firstStoryId(): StoryId = stories().first().id

        fun toDto(): List<ComponentDto> = value.map(Component::toDto)

        private fun stories(): List<Story> = value.flatMap(Component::stories)

        private fun <T> List<T>.firstRepeatedOrNull(): T? = firstOrNull { item -> count { it == item } > 1 }
    }

    private data class Component(
        val id: ComponentId,
        val group: String,
        val title: String,
        val description: String,
        val stories: List<Story>,
    ) {
        constructor(dto: ComponentDto) : this(
            id = ComponentId(dto.id),
            group = dto.group,
            title = dto.title,
            description = dto.description,
            stories = dto.stories.map(::Story),
        )

        init {
            if (stories.isEmpty()) throw ComponentWithoutStories(id.value)
            stories.firstOrNull { story -> !story.id.belongsTo(id) }?.let { story -> throw InvalidStoryId(story.id.value) }
        }

        fun toDto(): ComponentDto = ComponentDto(id = id.value, group = group, title = title, description = description, stories = stories.map(Story::toDto))
    }

    private data class Story(
        val id: StoryId,
        val name: String,
        val description: String,
    ) {
        constructor(dto: StoryDto) : this(
            id = StoryId(dto.id),
            name = dto.name,
            description = dto.description,
        )

        fun toDto(): StoryDto = StoryDto(id = id.value, name = name, description = description)
    }

    /** In kebab case, e.g.: `screen-health-bar`. */
    @JvmInline private value class ComponentId(
        val value: String,
    ) {
        init {
            if (!KEBAB_CASE.matches(value)) throw InvalidComponentId(value)
        }
    }

    /** Its component id and its name in kebab case, separated by two dashes, e.g.: `screen-health-bar--full`. */
    @JvmInline private value class StoryId(
        val value: String,
    ) {
        fun belongsTo(componentId: ComponentId): Boolean {
            val name = value.removePrefix("${componentId.value}$STORY_SEPARATOR")
            return name != value && KEBAB_CASE.matches(name)
        }
    }

    /**
     * Components: in the order they are listed, each with its stories in order, the first one opened by default.
     * Selected story: the id of the story on show.
     */
    data class Dto(
        val components: List<ComponentDto>,
        val selectedStoryId: String,
    ) {
        /**
         * Id: its group and title in kebab case, e.g.: `screen-health-bar`, kept when they are renamed. Group: the
         * subdomain of its view, which the sidebar lists it under, e.g.: `Screen`. Description: what the view shows, for
         * whoever browses the catalogue.
         */
        data class ComponentDto(
            val id: String,
            val group: String,
            val title: String,
            val description: String,
            val stories: List<StoryDto>,
        )

        /** Id: its component id and its name in kebab case, e.g.: `screen-health-bar--full`, kept when it is renamed. */
        data class StoryDto(
            val id: String,
            val name: String,
            val description: String,
        )
    }
}
