package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class ControlsViewTest : ViewsForTesting() {
    private val changedArgs = mutableListOf<Pair<String, ArgDto>>()
    private var resets = 0

    private val controls =
        listOf(
            ControlDto.boolean("equipped"),
            ControlDto.int("remaining", minimum = 0, maximum = 30),
            ControlDto.enum("mode", listOf("BUY", "SELL")),
            ControlDto.text("label"),
            ControlDto.pick("item", listOf("none", "hammer", "health-potion")),
            ControlDto.color("frame"),
        )
    private val args =
        mapOf(
            "equipped" to ArgDto.boolean(false),
            "remaining" to ArgDto.int(15),
            "mode" to ArgDto.text("SELL"),
            "label" to ArgDto.text("Potion"),
            "item" to ArgDto.text("none"),
            "frame" to ArgDto.text("FF375F"),
        )

    private fun newControlsView() =
        ControlsView().apply {
            setDelegate(
                object : ControlsView.Delegate {
                    override fun argChanged(
                        name: String,
                        value: ArgDto,
                    ) {
                        changedArgs += name to value
                    }

                    override fun argsReset() {
                        resets++
                    }
                },
            )
        }

    @Test
    fun `ControlsView should display its title and no control when it is displayed`() =
        sharedViewsTest {
            // When
            val controlsView = newControlsView()
            // Then
            controlsView.titleText() shouldBe "Controls"
            controlsView.controlNames().shouldBeEmpty()
        }

    @Test
    fun `Display should list the controls in their order, each with the value of its arg`() =
        sharedViewsTest {
            // Given
            val controlsView = newControlsView()
            addChild(controlsView)
            // When
            controlsView.display(controls, args)
            // Then
            assertSoftly {
                controlsView.controlNames().shouldContainExactly("equipped", "remaining", "mode", "label", "item", "frame")
                controlsView.displayedValue("equipped") shouldBe "false"
                controlsView.displayedValue("remaining") shouldBe "15"
                controlsView.displayedValue("mode") shouldBe "SELL"
                controlsView.optionNames("mode").shouldContainExactly("BUY", "SELL")
                controlsView.displayedValue("label") shouldBe "Potion"
                controlsView.displayedValue("item") shouldBe "none"
                controlsView.displayedValue("frame") shouldBe "FF375F"
                controlsView.swatchHex("frame") shouldBe "FF375F"
                controlsView.isListedTopDown().shouldBeTrue()
            }
        }

    @Test
    fun `Display should list only the given controls when they are displayed again`() =
        sharedViewsTest {
            // Given
            val controlsView = newControlsView()
            controlsView.display(controls, args)
            // When
            controlsView.display(listOf(ControlDto.int("remaining", minimum = 0, maximum = 30)), mapOf("remaining" to ArgDto.int(3)))
            // Then
            controlsView.controlNames().shouldContainExactly("remaining")
            controlsView.displayedValue("remaining") shouldBe "3"
        }

    @Test
    fun `ClickControlButton should change a boolean arg to the other value when its switch is clicked`() =
        controlsTest { controlsView ->
            // When
            clickControlButton(controlsView, "equipped", "toggle")
            // Then
            changedArgs.shouldContainExactly("equipped" to ArgDto.boolean(true))
        }

    @Test
    fun `ClickControlButton should change an int arg by one when its buttons are clicked`() =
        controlsTest { controlsView ->
            // When
            clickControlButton(controlsView, "remaining", "increase")
            clickControlButton(controlsView, "remaining", "decrease")
            // Then
            changedArgs.shouldContainExactly("remaining" to ArgDto.int(16), "remaining" to ArgDto.int(14))
        }

    @Test
    fun `ClickControlButton should not change an int arg when it is at its maximum`() =
        controlsTest(mapOf("remaining" to ArgDto.int(30))) { controlsView ->
            // When
            clickControlButton(controlsView, "remaining", "increase")
            // Then
            changedArgs.shouldBeEmpty()
        }

    @Test
    fun `ClickControlButton should change an enum arg when another option is clicked`() =
        controlsTest { controlsView ->
            // When
            clickControlButton(controlsView, "mode", "option:BUY")
            clickControlButton(controlsView, "mode", "option:SELL")
            // Then
            changedArgs.shouldContainExactly("mode" to ArgDto.text("BUY"))
        }

    @Test
    fun `ClickControlButton should change a pick arg to the next and the previous option, going round them`() =
        controlsTest { controlsView ->
            // When
            clickControlButton(controlsView, "item", "next")
            clickControlButton(controlsView, "item", "previous")
            // Then
            changedArgs.shouldContainExactly("item" to ArgDto.text("hammer"), "item" to ArgDto.text("health-potion"))
        }

    @Test
    fun `EnterText should change a text arg when a text is entered`() =
        controlsTest { controlsView ->
            // When
            controlsView.enterText("label", "Iron hammer")
            // Then
            changedArgs.shouldContainExactly("label" to ArgDto.text("Iron hammer"))
        }

    @Test
    fun `EnterText should change a color arg to its hex in upper case when a hex is entered`() =
        controlsTest { controlsView ->
            // When
            controlsView.enterText("frame", "#ffbb31")
            // Then
            changedArgs.shouldContainExactly("frame" to ArgDto.text("FFBB31"))
        }

    @Test
    fun `EnterText should undo a color, changing nothing, when what is entered is no hex`() =
        controlsTest { controlsView ->
            // When
            controlsView.enterText("frame", "red")
            // Then
            changedArgs.shouldBeEmpty()
            controlsView.displayedValue("frame") shouldBe "FF375F"
        }

    @Test
    fun `ClickReset should ask for the story's own args when it is clicked`() =
        controlsTest { controlsView ->
            // When
            clickReset(controlsView)
            // Then
            resets shouldBe 1
        }

    private fun controlsTest(
        changedArgs: Map<String, ArgDto> = emptyMap(),
        block: suspend ViewsForTesting.(ControlsView) -> Unit,
    ) = sharedViewsTest {
        // Given
        val controlsView = newControlsView()
        addChild(controlsView)
        controlsView.display(controls, args + changedArgs)
        block(controlsView)
    }
}
