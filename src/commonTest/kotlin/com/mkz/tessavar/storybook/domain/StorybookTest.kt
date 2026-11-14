package com.mkz.tessavar.storybook.domain

import com.mkz.tessavar.storybook.domain.Storybook.Dto.ActionDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.LocationDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import com.mkz.tessavar.storybook.domain.StorybookEvent.ActionRecorded
import com.mkz.tessavar.storybook.domain.StorybookEvent.ActionsCleared
import com.mkz.tessavar.storybook.domain.StorybookEvent.ArgChanged
import com.mkz.tessavar.storybook.domain.StorybookEvent.ArgsReset
import com.mkz.tessavar.storybook.domain.StorybookEvent.CatalogueOpened
import com.mkz.tessavar.storybook.domain.StorybookEvent.GlobalChanged
import com.mkz.tessavar.storybook.domain.StorybookEvent.StorySelected
import com.mkz.tessavar.storybook.domain.StorybookMother.LANGUAGE_CONTROL
import com.mkz.tessavar.storybook.domain.StorybookMother.component
import com.mkz.tessavar.storybook.domain.StorybookMother.location
import com.mkz.tessavar.storybook.domain.StorybookMother.story
import com.mkz.tessavar.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class StorybookTest {
    private val full = story(id = "screen-health-bar--full", name = "Full")
    private val empty = story(id = "screen-health-bar--empty", name = "Empty", args = mapOf("remaining" to ValueDto.int(0)))
    private val healthBar = component(id = "screen-health-bar", stories = listOf(full, empty))
    private val inventorySlot =
        component(
            id = "screen-inventory-slot",
            controls =
                listOf(
                    ControlDto.boolean("equipped"),
                    ControlDto.int("size", minimum = 30, maximum = 90),
                    ControlDto.enum("mode", listOf("BUY", "SELL")),
                    ControlDto.text("label"),
                    ControlDto.pick("item", listOf("none", "health-potion")),
                    ControlDto.color("frame"),
                ),
            args =
                mapOf(
                    "equipped" to ValueDto.boolean(false),
                    "size" to ValueDto.int(60),
                    "mode" to ValueDto.text("BUY"),
                    "label" to ValueDto.text("Potion"),
                    "item" to ValueDto.text("health-potion"),
                    "frame" to ValueDto.text("FFBB31"),
                ),
            stories = listOf(story(id = "screen-inventory-slot--stored")),
        )

    private fun create(
        components: List<ComponentDto>,
        location: LocationDto = location(),
    ) = Storybook.create(components, globalControls = listOf(LANGUAGE_CONTROL), defaultGlobals = mapOf("language" to ValueDto.text("en")), location = location)

    private val manaBar = component(id = "screen-mana-bar", title = "Mana bar", stories = listOf(story(id = "screen-mana-bar--full")))

    @Test
    fun `Create should list the components and open the first story when no story is requested`() {
        // When
        val storybook = create(listOf(healthBar, manaBar))
        // Then
        storybook.toDto() shouldBe
            Storybook.Dto(
                components = listOf(healthBar, manaBar),
                globalControls = listOf(LANGUAGE_CONTROL),
                defaultGlobals = mapOf("language" to ValueDto.text("en")),
                selectedStoryId = "screen-health-bar--full",
                args = mapOf("remaining" to ValueDto.int(30)),
                globals = mapOf("language" to ValueDto.text("en")),
                actions = emptyList(),
            )
    }

    @Test
    fun `Create should open the requested story when it is in the catalogue`() {
        // When
        val storybook = create(listOf(healthBar, manaBar), location(storyId = "screen-health-bar--empty"))
        // Then
        storybook.toDto().selectedStoryId shouldBe "screen-health-bar--empty"
    }

    @Test
    fun `Create should open the first story when the requested story is not in the catalogue`() {
        // When
        val storybook = create(listOf(healthBar, manaBar), location(storyId = "screen-health-bar--renamed"))
        // Then
        storybook.toDto().selectedStoryId shouldBe "screen-health-bar--full"
    }

    @Test
    fun `Create should publish the catalogue opened, then the story selected, when the catalogue is created`() {
        // When
        val (events, _) = create(listOf(healthBar), location(storyId = "screen-health-bar--empty")).pullEvents()
        // Then
        events.shouldContainExactly(CatalogueOpened, StorySelected(storyId = "screen-health-bar--empty"))
    }

    @Test
    fun `Create should fail when there is no component`() {
        // When
        val result = runCatching { create(emptyList()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.EmptyCatalogue>()
    }

    @Test
    fun `Create should fail when a component has no story`() {
        // When
        val result = runCatching { create(listOf(component(stories = emptyList()))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.ComponentWithoutStories>()
    }

    @Test
    fun `Create should fail when a component id is not in kebab case`() {
        // When
        val result = runCatching { create(listOf(component(id = "Screen-HealthBar"))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidComponentId>()
    }

    @Test
    fun `Create should fail when a story id does not start with its component id`() {
        // When
        val result = runCatching { create(listOf(component(id = "screen-health-bar", stories = listOf(story(id = "screen-mana-bar--full"))))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidStoryId>()
    }

    @Test
    fun `Create should fail when a story name is not in kebab case`() {
        // When
        val result = runCatching { create(listOf(component(id = "screen-health-bar", stories = listOf(story(id = "screen-health-bar--Full"))))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidStoryId>()
    }

    @Test
    fun `Create should fail when a story id has no name`() {
        // When
        val result = runCatching { create(listOf(component(id = "screen-health-bar", stories = listOf(story(id = "screen-health-bar--"))))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidStoryId>()
    }

    @Test
    fun `Create should fail when a component id is repeated`() {
        // When
        val result = runCatching { create(listOf(healthBar, healthBar.copy(title = "Other"))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.RepeatedComponentId>()
    }

    @Test
    fun `Create should fail when a story id is repeated`() {
        // When
        val result = runCatching { create(listOf(component(stories = listOf(full, full.copy(name = "Other"))))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.RepeatedStoryId>()
    }

    @Test
    fun `SelectStory should show the story and publish the story selected when it is another story of the catalogue`() {
        // Given
        val storybook = storybook(components = listOf(healthBar, manaBar))
        // When
        val (events, selectedStorybook) = storybook.selectStory("screen-mana-bar--full").pullEvents()
        // Then
        selectedStorybook.toDto().selectedStoryId shouldBe "screen-mana-bar--full"
        events.shouldContainExactly(StorySelected(storyId = "screen-mana-bar--full"))
    }

    @Test
    fun `SelectStory should change nothing when the story is already selected`() {
        // Given
        val storybook = storybook(components = listOf(healthBar), requestedStoryId = "screen-health-bar--empty")
        // When
        val (events, selectedStorybook) = storybook.selectStory("screen-health-bar--empty").pullEvents()
        // Then
        selectedStorybook shouldBe storybook
        events.shouldBeEmpty()
    }

    @Test
    fun `SelectStory should fail when the story is not in the catalogue`() {
        // Given
        val storybook = storybook(components = listOf(healthBar))
        // When
        val result = runCatching { storybook.selectStory("screen-mana-bar--full") }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.StoryNotFound>()
    }

    @Test
    fun `Create should display the story with the default args of its component, overridden by its own`() {
        // When
        val storybook = create(listOf(healthBar), location(storyId = "screen-health-bar--empty"))
        // Then
        storybook.toDto().args shouldBe mapOf("remaining" to ValueDto.int(0))
    }

    @Test
    fun `Create should display the story with the args the location asks for when their controls accept them`() {
        // When
        val storybook = create(listOf(healthBar), location(storyId = "screen-health-bar--full", args = mapOf("remaining" to "10")))
        // Then
        storybook.toDto().args shouldBe mapOf("remaining" to ValueDto.int(10))
    }

    @Test
    fun `Create should read the args the location asks for when they are of every control type`() {
        // Given
        val args = mapOf("equipped" to "true", "size" to "45", "mode" to "SELL", "label" to "Iron hammer", "item" to "none", "frame" to "ff375f")
        // When
        val storybook = create(listOf(inventorySlot), location(storyId = "screen-inventory-slot--stored", args = args))
        // Then
        storybook.toDto().args shouldBe
            mapOf(
                "equipped" to ValueDto.boolean(true),
                "size" to ValueDto.int(45),
                "mode" to ValueDto.text("SELL"),
                "label" to ValueDto.text("Iron hammer"),
                "item" to ValueDto.text("none"),
                "frame" to ValueDto.text("FF375F"),
            )
    }

    @Test
    fun `Create should keep the story's own args when the location asks for args their controls do not accept`() {
        // Given
        val args = mapOf("equipped" to "yes", "size" to "91", "mode" to "RENT", "item" to "sword", "frame" to "red", "unknown" to "1")
        // When
        val storybook = create(listOf(inventorySlot), location(storyId = "screen-inventory-slot--stored", args = args))
        // Then
        storybook.toDto().args shouldBe inventorySlot.args
    }

    @Test
    fun `Create should ignore the args the location asks for when its story is not in the catalogue`() {
        // When
        val storybook = create(listOf(healthBar), location(storyId = "screen-health-bar--renamed", args = mapOf("remaining" to "10")))
        // Then
        storybook.toDto().args shouldBe mapOf("remaining" to ValueDto.int(30))
    }

    @Test
    fun `Create should display the globals the location asks for, keeping the default of those not accepted`() {
        // Given
        val globalControls = listOf(LANGUAGE_CONTROL, ControlDto.boolean("paused"))
        val defaultGlobals = mapOf("language" to ValueDto.text("en"), "paused" to ValueDto.boolean(false))
        // When
        val storybook = Storybook.create(listOf(healthBar), globalControls, defaultGlobals, location(globals = mapOf("language" to "es", "paused" to "maybe")))
        // Then
        storybook.toDto().globals shouldBe mapOf("language" to ValueDto.text("es"), "paused" to ValueDto.boolean(false))
    }

    @Test
    fun `Create should fail when a control is repeated`() {
        // When
        val result = runCatching { create(listOf(component(controls = listOf(ControlDto.boolean("remaining"), ControlDto.boolean("remaining"))))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.RepeatedControl>()
    }

    @Test
    fun `Create should fail when an int control has a minimum greater than its maximum`() {
        // When
        val result = runCatching { create(listOf(component(controls = listOf(ControlDto.int("remaining", minimum = 31, maximum = 30))))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidControl>()
    }

    @Test
    fun `Create should fail when an int control has no minimum`() {
        // When
        val result = runCatching { create(listOf(component(controls = listOf(ControlDto.int("remaining", minimum = 0, maximum = 30).copy(minimum = null))))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidControl>()
    }

    @Test
    fun `Create should fail when an enum control has no options`() {
        // When
        val result = runCatching { create(listOf(component(controls = listOf(ControlDto.enum("remaining", emptyList()))))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidControl>()
    }

    @Test
    fun `Create should fail when a pick control has a repeated option`() {
        // When
        val result = runCatching { create(listOf(component(controls = listOf(ControlDto.pick("remaining", listOf("a", "a")))))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidControl>()
    }

    @Test
    fun `Create should fail when a control of a component has no default arg`() {
        // When
        val result = runCatching { create(listOf(component(args = emptyMap()))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.MissingValue>()
    }

    @Test
    fun `Create should fail when a story has an arg with no control`() {
        // When
        val result = runCatching { create(listOf(component(stories = listOf(story(args = mapOf("maximum" to ValueDto.int(30))))))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.ControlNotFound>()
    }

    @Test
    fun `Create should fail when a default arg is not accepted by its control`() {
        // When
        val result = runCatching { create(listOf(component(args = mapOf("remaining" to ValueDto.text("30"))))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidValue>()
    }

    @Test
    fun `Create should fail when a story arg is out of the range of its control`() {
        // When
        val result = runCatching { create(listOf(component(stories = listOf(story(args = mapOf("remaining" to ValueDto.int(31))))))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidValue>()
    }

    @Test
    fun `Create should fail when a default global is not accepted by its control`() {
        // When
        val result = runCatching { Storybook.create(listOf(healthBar), listOf(LANGUAGE_CONTROL), mapOf("language" to ValueDto.text("fr")), location()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidValue>()
    }

    @Test
    fun `SelectStory should display the story with its own args when args of the selected story were changed`() {
        // Given
        val storybook = storybook(components = listOf(healthBar)).changeArg("remaining", ValueDto.int(10))
        // When
        val selectedStorybook = storybook.selectStory("screen-health-bar--empty")
        // Then
        selectedStorybook.toDto().args shouldBe mapOf("remaining" to ValueDto.int(0))
        selectedStorybook.toLocation().args shouldBe emptyMap()
    }

    @Test
    fun `ChangeArg should display the story with the arg and publish the arg changed when its control accepts it`() {
        // Given
        val storybook = storybook(components = listOf(healthBar))
        // When
        val (events, changedStorybook) = storybook.changeArg("remaining", ValueDto.int(10)).pullEvents()
        // Then
        changedStorybook.toDto().args shouldBe mapOf("remaining" to ValueDto.int(10))
        events.shouldContainExactly(ArgChanged(storyId = "screen-health-bar--full", argName = "remaining"))
    }

    @Test
    fun `ChangeArg should accept a value of every control type when its control accepts it`() {
        // Given
        val storybook = storybook(components = listOf(inventorySlot))
        val args =
            mapOf(
                "equipped" to ValueDto.boolean(true),
                "size" to ValueDto.int(90),
                "mode" to ValueDto.text("SELL"),
                "label" to ValueDto.text(""),
                "item" to ValueDto.text("none"),
                "frame" to ValueDto.text("000000"),
            )
        // When
        val changedStorybook = args.entries.fold(storybook) { changed, (name, value) -> changed.changeArg(name, value) }
        // Then
        changedStorybook.toDto().args shouldBe args
    }

    @Test
    fun `ChangeArg should change nothing when the arg has that value`() {
        // Given
        val storybook = storybook(components = listOf(healthBar))
        // When
        val (events, changedStorybook) = storybook.changeArg("remaining", ValueDto.int(30)).pullEvents()
        // Then
        changedStorybook shouldBe storybook
        events.shouldBeEmpty()
    }

    @Test
    fun `ChangeArg should fail when the value is out of the range of its control`() {
        // When
        val result = runCatching { storybook(components = listOf(healthBar)).changeArg("remaining", ValueDto.int(-1)) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidValue>()
    }

    @Test
    fun `ChangeArg should fail when a color is not a hex in upper case`() {
        // When
        val result = runCatching { storybook(components = listOf(inventorySlot)).changeArg("frame", ValueDto.text("ff375f")) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidValue>()
    }

    @Test
    fun `ChangeArg should fail when the value is not an option of its control`() {
        // When
        val result = runCatching { storybook(components = listOf(inventorySlot)).changeArg("mode", ValueDto.text("RENT")) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidValue>()
    }

    @Test
    fun `ChangeArg should fail when the component of the selected story has no such control`() {
        // When
        val result = runCatching { storybook(components = listOf(healthBar)).changeArg("maximum", ValueDto.int(10)) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.ControlNotFound>()
    }

    @Test
    fun `ResetArgs should display the story with its own args and publish the args reset when args were changed`() {
        // Given
        val storybook = storybook(components = listOf(healthBar)).changeArg("remaining", ValueDto.int(10)).pullEvents().second
        // When
        val (events, resetStorybook) = storybook.resetArgs().pullEvents()
        // Then
        resetStorybook.toDto().args shouldBe mapOf("remaining" to ValueDto.int(30))
        events.shouldContainExactly(ArgsReset(storyId = "screen-health-bar--full"))
    }

    @Test
    fun `ResetArgs should change nothing when no arg was changed`() {
        // Given
        val storybook = storybook(components = listOf(healthBar))
        // When
        val (events, resetStorybook) = storybook.resetArgs().pullEvents()
        // Then
        resetStorybook shouldBe storybook
        events.shouldBeEmpty()
    }

    @Test
    fun `ChangeGlobal should display every story with the global and publish the global changed when its control accepts it`() {
        // Given
        val storybook = storybook(components = listOf(healthBar))
        // When
        val (events, changedStorybook) = storybook.changeGlobal("language", ValueDto.text("es")).pullEvents()
        // Then
        changedStorybook.toDto().globals shouldBe mapOf("language" to ValueDto.text("es"))
        events.shouldContainExactly(GlobalChanged(globalName = "language"))
    }

    @Test
    fun `ChangeGlobal should change nothing when the global has that value`() {
        // Given
        val storybook = storybook(components = listOf(healthBar))
        // When
        val (events, changedStorybook) = storybook.changeGlobal("language", ValueDto.text("en")).pullEvents()
        // Then
        changedStorybook shouldBe storybook
        events.shouldBeEmpty()
    }

    @Test
    fun `ChangeGlobal should fail when its control does not accept the value`() {
        // When
        val result = runCatching { storybook(components = listOf(healthBar)).changeGlobal("language", ValueDto.text("fr")) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidValue>()
    }

    @Test
    fun `ToLocation should keep the story, the args changed from its own in the order of their controls, and the globals changed from their defaults`() {
        // Given
        val storybook =
            storybook(components = listOf(inventorySlot))
                .changeArg("frame", ValueDto.text("FF375F"))
                .changeArg("equipped", ValueDto.boolean(true))
                .changeArg("size", ValueDto.int(45))
                .changeArg("size", ValueDto.int(60))
                .changeGlobal("language", ValueDto.text("es"))
        // When
        val location = storybook.toLocation()
        // Then
        location shouldBe location(storyId = "screen-inventory-slot--stored", args = mapOf("equipped" to "true", "frame" to "FF375F"), globals = mapOf("language" to "es"))
    }

    @Test
    fun `ToLocation should keep no global when every global has its default`() {
        // Given
        val storybook = storybook(components = listOf(healthBar)).changeGlobal("language", ValueDto.text("es")).changeGlobal("language", ValueDto.text("en"))
        // When
        val location = storybook.toLocation()
        // Then
        location.globals shouldBe emptyMap()
    }

    @Test
    fun `RecordAction should record the action as the newest one and publish the action recorded`() {
        // Given
        val storybook = storybook(components = listOf(healthBar)).recordAction("shopSlotSelected", listOf("3")).pullEvents().second
        // When
        val (events, recordedStorybook) = storybook.recordAction("shopBuyerStepped", listOf("-1")).pullEvents()
        // Then
        recordedStorybook.toDto().actions.shouldContainExactly(ActionDto(name = "shopBuyerStepped", arguments = listOf("-1")), ActionDto(name = "shopSlotSelected", arguments = listOf("3")))
        events.shouldContainExactly(ActionRecorded(storyId = "screen-health-bar--full", actionName = "shopBuyerStepped"))
    }

    @Test
    fun `RecordAction should keep only the newest actions when the most actions are recorded`() {
        // Given
        val storybook = (1..Storybook.MAXIMUM_ACTIONS).fold(storybook()) { recorded, index -> recorded.recordAction("shopSlotSelected", listOf(index.toString())) }
        // When
        val recordedStorybook = storybook.recordAction("shopBackSelected", emptyList())
        // Then
        val actions = recordedStorybook.toDto().actions
        actions.size shouldBe Storybook.MAXIMUM_ACTIONS
        actions.first() shouldBe ActionDto(name = "shopBackSelected", arguments = emptyList())
        actions.last() shouldBe ActionDto(name = "shopSlotSelected", arguments = listOf("2"))
    }

    @Test
    fun `RecordAction should keep the actions when an arg of the selected story is changed`() {
        // Given
        val storybook = storybook(components = listOf(healthBar)).recordAction("shopSlotSelected", listOf("3"))
        // When
        val changedStorybook = storybook.changeArg("remaining", ValueDto.int(3))
        // Then
        changedStorybook.toDto().actions.shouldContainExactly(ActionDto(name = "shopSlotSelected", arguments = listOf("3")))
    }

    @Test
    fun `SelectStory should show the story with no actions when actions of the selected story were recorded`() {
        // Given
        val storybook = storybook(components = listOf(healthBar)).recordAction("shopSlotSelected", listOf("3"))
        // When
        val selectedStorybook = storybook.selectStory("screen-health-bar--empty")
        // Then
        selectedStorybook.toDto().actions.shouldBeEmpty()
    }

    @Test
    fun `ClearActions should empty the actions recorded and publish the actions cleared when actions were recorded`() {
        // Given
        val storybook = storybook(components = listOf(healthBar)).recordAction("shopSlotSelected", listOf("3")).pullEvents().second
        // When
        val (events, clearedStorybook) = storybook.clearActions().pullEvents()
        // Then
        clearedStorybook.toDto().actions.shouldBeEmpty()
        events.shouldContainExactly(ActionsCleared(storyId = "screen-health-bar--full"))
    }

    @Test
    fun `ClearActions should change nothing when no action was recorded`() {
        // Given
        val storybook = storybook(components = listOf(healthBar))
        // When
        val (events, clearedStorybook) = storybook.clearActions().pullEvents()
        // Then
        clearedStorybook shouldBe storybook
        events.shouldBeEmpty()
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // When
        val (events, _) = storybook().pullEvents()
        // Then
        events.shouldBeEmpty()
    }
}
