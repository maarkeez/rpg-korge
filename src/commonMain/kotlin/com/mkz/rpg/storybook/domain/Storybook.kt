package com.mkz.rpg.storybook.domain

import com.mkz.rpg.storybook.domain.Storybook.Dto.ActionDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.LocationDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.StorybookError.ComponentWithoutStories
import com.mkz.rpg.storybook.domain.StorybookError.ControlNotFound
import com.mkz.rpg.storybook.domain.StorybookError.EmptyCatalogue
import com.mkz.rpg.storybook.domain.StorybookError.InvalidComponentId
import com.mkz.rpg.storybook.domain.StorybookError.InvalidControl
import com.mkz.rpg.storybook.domain.StorybookError.InvalidStoryId
import com.mkz.rpg.storybook.domain.StorybookError.InvalidValue
import com.mkz.rpg.storybook.domain.StorybookError.MissingValue
import com.mkz.rpg.storybook.domain.StorybookError.RepeatedComponentId
import com.mkz.rpg.storybook.domain.StorybookError.RepeatedControl
import com.mkz.rpg.storybook.domain.StorybookError.RepeatedStoryId
import com.mkz.rpg.storybook.domain.StorybookError.StoryNotFound
import com.mkz.rpg.storybook.domain.StorybookEvent.ActionRecorded
import com.mkz.rpg.storybook.domain.StorybookEvent.ActionsCleared
import com.mkz.rpg.storybook.domain.StorybookEvent.ArgChanged
import com.mkz.rpg.storybook.domain.StorybookEvent.ArgsReset
import com.mkz.rpg.storybook.domain.StorybookEvent.CatalogueInitialised
import com.mkz.rpg.storybook.domain.StorybookEvent.GlobalChanged
import com.mkz.rpg.storybook.domain.StorybookEvent.StorySelected
import kotlin.jvm.JvmInline

/**
 * The views of the game shown in isolation, and the story on show. The catalogue lists the components, each a view of
 * the game (e.g.: the health bar), with its stories, each a state of the view (e.g.: full). Their ids are kept in the
 * links shared, so they are stable: renaming a component or a story keeps its id.
 *
 * The story on show is displayed with its args, one per control of its component (e.g.: `remaining = 15`), which can be
 * changed to any value its control accepts, and with the globals shared by every story (e.g.: `language = es`). The
 * actions log the callbacks the view on show made, e.g.: `shopSlotSelected(3)`, the newest first, until another story
 * is shown.
 */
@ConsistentCopyVisibility
data class Storybook private constructor(
    private val components: Components,
    private val globalControls: Controls,
    private val defaultGlobals: Values,
    private val selectedStoryId: StoryId,
    private val changedArgs: Values,
    private val globals: Values,
    private val actions: List<Action>,
    private val events: Set<StorybookEvent>,
) {
    companion object {
        /** How many actions the log keeps: the newest ones. */
        const val MAXIMUM_ACTIONS = 50

        private val KEBAB_CASE = Regex("[a-z0-9]+(-[a-z0-9]+)*")
        private val HEX_COLOR = Regex("[0-9A-F]{6}")
        private const val STORY_SEPARATOR = "--"

        private fun <T> List<T>.firstRepeatedOrNull(): T? = firstOrNull { item -> count { it == item } > 1 }

        /**
         * Opens the catalogue on the story the location asks for, with its args and globals. Anything the location asks
         * for that is not valid is ignored, never a failure, as a link may outlive what it asks for: an unknown story
         * opens the first story, with its own args, and an unknown or invalid arg or global keeps its own value.
         */
        fun create(
            components: List<ComponentDto>,
            globalControls: List<ControlDto>,
            defaultGlobals: Map<String, ArgDto>,
            location: LocationDto,
        ): Storybook {
            val catalogue = Components.fromDtos(components)
            val controls = Controls.fromDtos(globalControls)
            val defaults = controls.defaultsOf(defaultGlobals)
            val requestedStoryId = catalogue.storyIdOrNull(location.storyId)
            val selectedStoryId = requestedStoryId ?: catalogue.firstStoryId()
            val component = catalogue.componentOf(selectedStoryId)
            val changedArgs = if (requestedStoryId == null) Values.NONE else component.controls.parse(location.args).changedFrom(component.argsOf(selectedStoryId))
            return Storybook(
                components = catalogue,
                globalControls = controls,
                defaultGlobals = defaults,
                selectedStoryId = selectedStoryId,
                changedArgs = changedArgs,
                globals = defaults + controls.parse(location.globals),
                actions = emptyList(),
                events = setOf(CatalogueInitialised, StorySelected(storyId = selectedStoryId.value)),
            )
        }
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            components = components.toDto(),
            globalControls = globalControls.toDto(),
            defaultGlobals = defaultGlobals.toDto(),
            selectedStoryId = selectedStoryId.value,
            args = args().toDto(),
            globals = globals.toDto(),
            actions = actions.map(Action::toDto),
        )

    /** What a link keeps to open the same: the story on show, the args changed from its own, and the globals changed from their defaults. */
    fun toLocation() =
        LocationDto(
            storyId = selectedStoryId.value,
            args = selectedComponent().controls.asText(changedArgs),
            globals = globalControls.asText(globals.changedFrom(defaultGlobals)),
        )

    /**
     * Shows the story, with its own args and no actions, as those were of the story on show. Nothing changes when it is
     * already on show. Fails when it is not in the catalogue.
     */
    fun selectStory(storyId: String): Storybook {
        val story = components.storyIdOrNull(storyId) ?: throw StoryNotFound(storyId)
        if (story == selectedStoryId) return this
        return copy(selectedStoryId = story, changedArgs = Values.NONE, actions = emptyList(), events = events + StorySelected(storyId = story.value))
    }

    /** Changes an arg of the story on show. Nothing changes when it has that value. Fails when its control does not accept it. */
    fun changeArg(
        name: String,
        value: ArgDto,
    ): Storybook {
        val arg = selectedComponent().controls.valueOf(name, value)
        if (args()[name] == arg) return this
        val storyArgs = selectedComponent().argsOf(selectedStoryId)
        return copy(
            changedArgs = (changedArgs + Values(mapOf(name to arg))).changedFrom(storyArgs),
            events = events + ArgChanged(storyId = selectedStoryId.value, argName = name),
        )
    }

    /** Gives the story on show its own args back. Nothing changes when none was changed. */
    fun resetArgs(): Storybook {
        if (changedArgs == Values.NONE) return this
        return copy(changedArgs = Values.NONE, events = events + ArgsReset(storyId = selectedStoryId.value))
    }

    /** Changes a global of every story. Nothing changes when it has that value. Fails when its control does not accept it. */
    fun changeGlobal(
        name: String,
        value: ArgDto,
    ): Storybook {
        val global = globalControls.valueOf(name, value)
        if (globals[name] == global) return this
        return copy(globals = globals + Values(mapOf(name to global)), events = events + GlobalChanged(globalName = name))
    }

    /**
     * Logs an action of the view on show, e.g.: `shopSlotSelected` with `3`, as the newest one, keeping the last
     * [MAXIMUM_ACTIONS].
     */
    fun recordAction(
        name: String,
        arguments: List<String>,
    ): Storybook =
        copy(
            actions = (listOf(Action(name, arguments)) + actions).take(MAXIMUM_ACTIONS),
            events = events + ActionRecorded(storyId = selectedStoryId.value, actionName = name),
        )

    /** Empties the log of actions. Nothing changes when it is empty. */
    fun clearActions(): Storybook {
        if (actions.isEmpty()) return this
        return copy(actions = emptyList(), events = events + ActionsCleared(storyId = selectedStoryId.value))
    }

    private fun selectedComponent(): Component = components.componentOf(selectedStoryId)

    private fun args(): Values = selectedComponent().argsOf(selectedStoryId) + changedArgs

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

        fun componentOf(storyId: StoryId): Component = value.first { component -> component.stories.any { story -> story.id == storyId } }

        fun toDto(): List<ComponentDto> = value.map(Component::toDto)

        private fun stories(): List<Story> = value.flatMap(Component::stories)
    }

    private data class Component(
        val id: ComponentId,
        val group: String,
        val title: String,
        val description: String,
        val controls: Controls,
        val args: Values,
        val stories: List<Story>,
    ) {
        constructor(dto: ComponentDto) : this(Controls.fromDtos(dto.controls), dto)

        private constructor(controls: Controls, dto: ComponentDto) : this(
            id = ComponentId(dto.id),
            group = dto.group,
            title = dto.title,
            description = dto.description,
            controls = controls,
            args = controls.defaultsOf(dto.args),
            stories = dto.stories.map { story -> Story(story, controls) },
        )

        init {
            if (stories.isEmpty()) throw ComponentWithoutStories(id.value)
            stories.firstOrNull { story -> !story.id.belongsTo(id) }?.let { story -> throw InvalidStoryId(story.id.value) }
        }

        /** The args the story is displayed with: the default args of the component, overridden by the story's own. */
        fun argsOf(storyId: StoryId): Values = args + stories.first { story -> story.id == storyId }.args

        fun toDto(): ComponentDto =
            ComponentDto(
                id = id.value,
                group = group,
                title = title,
                description = description,
                controls = controls.toDto(),
                args = args.toDto(),
                stories = stories.map(Story::toDto),
            )
    }

    private data class Story(
        val id: StoryId,
        val name: String,
        val description: String,
        val args: Values,
    ) {
        constructor(dto: StoryDto, controls: Controls) : this(
            id = StoryId(dto.id),
            name = dto.name,
            description = dto.description,
            args = controls.valuesOf(dto.args),
        )

        fun toDto(): StoryDto = StoryDto(id = id.value, name = name, description = description, args = args.toDto())
    }

    /** The controls of a component, or the globals, in the order they are listed, each with its own name. */
    private data class Controls(
        val value: List<Control>,
    ) {
        init {
            value.map(Control::name).firstRepeatedOrNull()?.let { name -> throw RepeatedControl(name) }
        }

        companion object {
            fun fromDtos(dtos: List<ControlDto>): Controls = Controls(dtos.map(Control::of))
        }

        /** A value for every control, as the defaults. Fails when one is missing, has no control or is not accepted. */
        fun defaultsOf(dtos: Map<String, ArgDto>): Values {
            value.firstOrNull { control -> control.name !in dtos }?.let { control -> throw MissingValue(control.name) }
            return valuesOf(dtos)
        }

        /** Values for some controls. Fails when one has no control or is not accepted. */
        fun valuesOf(dtos: Map<String, ArgDto>): Values = Values(dtos.mapValues { (name, dto) -> valueOf(name, dto) })

        fun valueOf(
            name: String,
            dto: ArgDto,
        ): Value {
            val control = controlOf(name) ?: throw ControlNotFound(name)
            return control.valueOrNull(dto) ?: throw InvalidValue(name, dto.text ?: dto.int ?: dto.boolean)
        }

        /** The values written as text, e.g.: in a link, leaving out those that have no control or are not accepted. */
        fun parse(texts: Map<String, String>): Values = Values(texts.mapNotNull { (name, text) -> controlOf(name)?.parseOrNull(text)?.let { value -> name to value } }.toMap())

        /** The values as text, in the order of the controls, e.g.: `remaining` to `15`. */
        fun asText(values: Values): Map<String, String> = value.mapNotNull { control -> values[control.name]?.let { value -> control.name to value.asText() } }.toMap()

        fun toDto(): List<ControlDto> = value.map(Control::toDto)

        private fun controlOf(name: String): Control? = value.firstOrNull { control -> control.name == name }
    }

    /** An editor of a value, which defines the values it accepts. */
    private sealed interface Control {
        val name: String

        /** The value, when the control accepts it, or `null`. */
        fun valueOrNull(dto: ArgDto): Value?

        /** The value written as text, e.g.: `15`, when the control accepts it, or `null`. */
        fun parseOrNull(text: String): Value?

        fun toDto(): ControlDto

        companion object {
            fun of(dto: ControlDto): Control =
                when (dto.type) {
                    ControlDto.TypeDto.BOOLEAN -> BooleanControl(dto.name)
                    ControlDto.TypeDto.INT ->
                        IntControl(
                            dto.name,
                            minimum = dto.minimum ?: throw InvalidControl(dto.name, "it has no minimum"),
                            maximum =
                                dto.maximum ?: throw InvalidControl(dto.name, "it has no maximum"),
                        )
                    ControlDto.TypeDto.ENUM -> EnumControl(dto.name, OptionList(dto.name, dto.options))
                    ControlDto.TypeDto.TEXT -> TextControl(dto.name)
                    ControlDto.TypeDto.PICK -> PickControl(dto.name, OptionList(dto.name, dto.options))
                    ControlDto.TypeDto.COLOR -> ColorControl(dto.name)
                }
        }
    }

    private data class BooleanControl(
        override val name: String,
    ) : Control {
        override fun valueOrNull(dto: ArgDto): Value? = dto.boolean?.takeIf { dto.type == ArgDto.TypeDto.BOOLEAN }?.let(::BooleanValue)

        override fun parseOrNull(text: String): Value? = text.toBooleanStrictOrNull()?.let(::BooleanValue)

        override fun toDto() = ControlDto.boolean(name)
    }

    private data class IntControl(
        override val name: String,
        val minimum: Int,
        val maximum: Int,
    ) : Control {
        init {
            if (minimum > maximum) throw InvalidControl(name, "its minimum $minimum is greater than its maximum $maximum")
        }

        override fun valueOrNull(dto: ArgDto): Value? = dto.int?.takeIf { dto.type == ArgDto.TypeDto.INT && it in minimum..maximum }?.let(::IntValue)

        override fun parseOrNull(text: String): Value? = text.toIntOrNull()?.let { int -> valueOrNull(ArgDto.int(int)) }

        override fun toDto() = ControlDto.int(name, minimum = minimum, maximum = maximum)
    }

    private data class EnumControl(
        override val name: String,
        val options: OptionList,
    ) : Control {
        override fun valueOrNull(dto: ArgDto): Value? = options.valueOrNull(dto)

        override fun parseOrNull(text: String): Value? = options.valueOrNull(ArgDto.text(text))

        override fun toDto() = ControlDto.enum(name, options.value)
    }

    private data class TextControl(
        override val name: String,
    ) : Control {
        override fun valueOrNull(dto: ArgDto): Value? = dto.text?.takeIf { dto.type == ArgDto.TypeDto.TEXT }?.let(::TextValue)

        override fun parseOrNull(text: String): Value = TextValue(text)

        override fun toDto() = ControlDto.text(name)
    }

    private data class PickControl(
        override val name: String,
        val options: OptionList,
    ) : Control {
        override fun valueOrNull(dto: ArgDto): Value? = options.valueOrNull(dto)

        override fun parseOrNull(text: String): Value? = options.valueOrNull(ArgDto.text(text))

        override fun toDto() = ControlDto.pick(name, options.value)
    }

    /** A color as its hex in upper case, without `#`, e.g.: `FF375F`. */
    private data class ColorControl(
        override val name: String,
    ) : Control {
        override fun valueOrNull(dto: ArgDto): Value? = dto.text?.takeIf { dto.type == ArgDto.TypeDto.TEXT && HEX_COLOR.matches(it) }?.let(::TextValue)

        // A link may write it in lower case, e.g.: `ff375f`.
        override fun parseOrNull(text: String): Value? = valueOrNull(ArgDto.text(text.uppercase()))

        override fun toDto() = ControlDto.color(name)
    }

    /** The options an enum or a pick control accepts, at least one, each once. */
    private data class OptionList(
        val value: List<String>,
    ) {
        constructor(name: String, options: List<String>?) : this(options ?: throw InvalidControl(name, "it has no options")) {
            if (value.isEmpty()) throw InvalidControl(name, "it has no options")
            value.firstRepeatedOrNull()?.let { option -> throw InvalidControl(name, "its option '$option' is repeated") }
        }

        fun valueOrNull(dto: ArgDto): Value? = dto.text?.takeIf { dto.type == ArgDto.TypeDto.TEXT && it in value }?.let(::TextValue)
    }

    private sealed interface Value {
        fun asText(): String

        fun toDto(): ArgDto
    }

    private data class BooleanValue(
        val value: Boolean,
    ) : Value {
        override fun asText() = value.toString()

        override fun toDto() = ArgDto.boolean(value)
    }

    private data class IntValue(
        val value: Int,
    ) : Value {
        override fun asText() = value.toString()

        override fun toDto() = ArgDto.int(value)
    }

    private data class TextValue(
        val value: String,
    ) : Value {
        override fun asText() = value

        override fun toDto() = ArgDto.text(value)
    }

    /** The values of some controls, by the name of their control. */
    private data class Values(
        val value: Map<String, Value>,
    ) {
        companion object {
            val NONE = Values(emptyMap())
        }

        operator fun get(name: String): Value? = value[name]

        /** These values, overridden by the [other] ones. */
        operator fun plus(other: Values): Values = Values(value + other.value)

        /** Only the values that differ from the [original] ones. */
        fun changedFrom(original: Values): Values = Values(value.filter { (name, value) -> original[name] != value })

        fun toDto(): Map<String, ArgDto> = value.mapValues { (_, value) -> value.toDto() }
    }

    /** A callback the view on show made, e.g.: `shopSlotSelected` with `3`. */
    private data class Action(
        val name: String,
        val arguments: List<String>,
    ) {
        fun toDto(): ActionDto = ActionDto(name = name, arguments = arguments)
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
     * Global controls: the settings shared by every story, e.g.: the language, with their default values.
     * Selected story: the id of the story on show. Args: the values the story on show is displayed with, one per
     * control of its component. Globals: the value of each global control. Actions: the callbacks the view on show made,
     * the newest first.
     */
    data class Dto(
        val components: List<ComponentDto>,
        val globalControls: List<ControlDto>,
        val defaultGlobals: Map<String, ArgDto>,
        val selectedStoryId: String,
        val args: Map<String, ArgDto>,
        val globals: Map<String, ArgDto>,
        val actions: List<ActionDto>,
    ) {
        /**
         * Id: its group and title in kebab case, e.g.: `screen-health-bar`, kept when they are renamed. Group: the
         * subdomain of its view, which the sidebar lists it under, e.g.: `Screen`. Description: what the view shows, for
         * whoever browses the catalogue. Controls: in the order they are listed. Args: the default value of each control.
         */
        data class ComponentDto(
            val id: String,
            val group: String,
            val title: String,
            val description: String,
            val controls: List<ControlDto>,
            val args: Map<String, ArgDto>,
            val stories: List<StoryDto>,
        )

        /**
         * Id: its component id and its name in kebab case, e.g.: `screen-health-bar--full`, kept when it is renamed.
         * Args: the values it overrides from the default args of its component, e.g.: `remaining = 0`.
         */
        data class StoryDto(
            val id: String,
            val name: String,
            val description: String,
            val args: Map<String, ArgDto>,
        )

        /**
         * The editor of an arg or a global, by its name, e.g.: `remaining`. Minimum and maximum: of an int control.
         * Options: of an enum control (e.g.: the values of a Kotlin enum) or a pick control (e.g.: the lootable item ids).
         */
        data class ControlDto(
            val name: String,
            val type: TypeDto,
            val minimum: Int?,
            val maximum: Int?,
            val options: List<String>?,
        ) {
            companion object {
                fun boolean(name: String) = withoutDetails(name, TypeDto.BOOLEAN)

                fun int(
                    name: String,
                    minimum: Int,
                    maximum: Int,
                ) = withoutDetails(name, TypeDto.INT).copy(minimum = minimum, maximum = maximum)

                fun enum(
                    name: String,
                    options: List<String>,
                ) = withoutDetails(name, TypeDto.ENUM).copy(options = options)

                fun text(name: String) = withoutDetails(name, TypeDto.TEXT)

                fun pick(
                    name: String,
                    options: List<String>,
                ) = withoutDetails(name, TypeDto.PICK).copy(options = options)

                /** A color, as its hex in upper case, without `#`, e.g.: `FF375F`. */
                fun color(name: String) = withoutDetails(name, TypeDto.COLOR)

                private fun withoutDetails(
                    name: String,
                    type: TypeDto,
                ) = ControlDto(name = name, type = type, minimum = null, maximum = null, options = null)
            }

            enum class TypeDto {
                BOOLEAN,
                INT,
                ENUM,
                TEXT,
                PICK,
                COLOR,
            }
        }

        /** The value of an arg or a global: a boolean, an int, or a text (also the option of an enum or a pick, and a color). */
        data class ArgDto(
            val type: TypeDto,
            val boolean: Boolean?,
            val int: Int?,
            val text: String?,
        ) {
            companion object {
                fun boolean(value: Boolean) = ArgDto(type = TypeDto.BOOLEAN, boolean = value, int = null, text = null)

                fun int(value: Int) = ArgDto(type = TypeDto.INT, boolean = null, int = value, text = null)

                fun text(value: String) = ArgDto(type = TypeDto.TEXT, boolean = null, int = null, text = value)
            }

            enum class TypeDto {
                BOOLEAN,
                INT,
                TEXT,
            }
        }

        /**
         * A callback the view on show made, by its name, e.g.: `shopSlotSelected`. Arguments: what it was made with, as
         * text, e.g.: `3`.
         */
        data class ActionDto(
            val name: String,
            val arguments: List<String>,
        )

        /**
         * Where a link keeps what is on show, as text, e.g.: the page URL `?story=screen-health-bar--full&args=remaining:3`.
         * Story: the id of the story, or `null` when none is asked for. Args: those changed from the story's own. Globals:
         * those changed from their defaults.
         */
        data class LocationDto(
            val storyId: String?,
            val args: Map<String, String>,
            val globals: Map<String, String>,
        )
    }
}
