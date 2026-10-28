package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto.TypeDto
import korlibs.image.color.Colors
import korlibs.korge.annotations.KorgeExperimental
import korlibs.korge.ui.UITextInput
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.Text
import korlibs.korge.view.addTo
import korlibs.korge.view.container
import korlibs.korge.view.position
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.math.geom.Size

/**
 * The args of the story on show, under its info: one row per control of its component, with its name and its editor,
 * e.g.: `-` and `+` around an int. Editing an arg asks to change it, and `Reset` asks for the story's own args back.
 * An editor only asks for the values its control accepts, e.g.: an int within its minimum and maximum.
 */
@OptIn(KorgeExperimental::class)
open class ControlsView : Container() {
    companion object {
        const val WIDTH = 480.0
        const val TITLE_HEIGHT = 44.0
        const val NAME_WIDTH = 150.0
        const val ROW_GAP = 10.0
        const val BUTTON_GAP = 6.0
        const val INT_VALUE_WIDTH = 64.0
        const val PICK_VALUE_WIDTH = 200.0
        const val TEXT_INPUT_WIDTH = 300.0
        const val COLOR_INPUT_WIDTH = 120.0
        private val HEX_COLOR = Regex("[0-9A-F]{6}")
    }

    interface Delegate {
        fun argChanged(
            name: String,
            value: ArgDto,
        )

        fun argsReset()
    }

    private var delegate: Delegate? = null

    internal val title: Text
    internal val resetButton: StorybookButton
    internal val rows: Container

    // In the order the controls are listed.
    internal var controlRows: List<ControlRow> = emptyList()
        private set

    init {
        title = text("Controls", textSize = 22.0, color = Colors.WHITE)
        val resetSize = StorybookButton.sizeFor("Reset")
        resetButton = StorybookButton("Reset", resetSize) { delegate?.argsReset() }.addTo(this).position(WIDTH - resetSize.width, 0.0)
        rows = container().position(0.0, TITLE_HEIGHT)
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Lists the controls, in their order, each with the value of its arg. */
    open fun display(
        controls: List<ControlDto>,
        args: Map<String, ArgDto>,
    ) {
        rows.removeChildren()
        var y = 0.0
        controlRows =
            controls.map { control ->
                ControlRow(control, args.getValue(control.name)).addTo(rows).position(0.0, y).also { row -> y += row.height + ROW_GAP }
            }
    }

    /** The name of a control and the editor of its arg. */
    internal inner class ControlRow(
        val control: ControlDto,
        val arg: ArgDto,
    ) : Container() {
        internal val nameText: Text = text(control.name, textSize = 16.0, color = StoryInfoView.SECONDARY_COLOR).position(0.0, (StorybookButton.HEIGHT - 16.0) / 2)

        // By what they do, e.g.: `increase`, or by their option, e.g.: `option:SELL`.
        internal val buttons = mutableMapOf<String, StorybookButton>()
        internal var valueText: Text? = null
            private set
        internal var textInput: UITextInput? = null
            private set
        internal var colorSwatch: SolidRect? = null
            private set

        private val editor = container().position(NAME_WIDTH, 0.0)

        init {
            when (control.type) {
                TypeDto.BOOLEAN -> booleanEditor()
                TypeDto.INT -> intEditor()
                TypeDto.ENUM -> enumEditor()
                TypeDto.TEXT -> textEditor()
                TypeDto.PICK -> pickEditor()
                TypeDto.COLOR -> colorEditor()
            }
        }

        /** The value of the arg as displayed, e.g.: `15`, `true` or `SELL`. */
        fun displayedValue(): String =
            textInput?.text
                ?: valueText?.text
                ?: buttons.values
                    .firstOrNull { button -> button.isSelected }
                    ?.label
                    ?.text
                ?: buttons.values
                    .single()
                    .label.text

        // A switch, highlighted while it is on.
        private fun booleanEditor() {
            val value = requireNotNull(arg.boolean)
            button("toggle", value.toString(), x = 0.0, size = StorybookButton.sizeFor(false.toString())) { change(ArgDto.boolean(!value)) }.isSelected = value
        }

        // `-` and `+` around the value, which stop at the minimum and the maximum, followed by them.
        private fun intEditor() {
            val value = requireNotNull(arg.int)
            val minimum = requireNotNull(control.minimum)
            val maximum = requireNotNull(control.maximum)
            button("decrease", "-", x = 0.0) { if (value > minimum) change(ArgDto.int(value - 1)) }
            valueText = centeredText(value.toString(), x = StorybookButton.HEIGHT, width = INT_VALUE_WIDTH)
            button("increase", "+", x = StorybookButton.HEIGHT + INT_VALUE_WIDTH) { if (value < maximum) change(ArgDto.int(value + 1)) }
            editor.text("$minimum – $maximum", textSize = 14.0, color = StoryInfoView.SECONDARY_COLOR).position(2 * StorybookButton.HEIGHT + INT_VALUE_WIDTH + BUTTON_GAP * 2, 6.0)
        }

        // Every option, the value highlighted, on as many lines as they need.
        private fun enumEditor() {
            var x = 0.0
            var y = 0.0
            requireNotNull(control.options).forEach { option ->
                val size = StorybookButton.sizeFor(option)
                if (x > 0 && x + size.width > WIDTH - NAME_WIDTH) {
                    x = 0.0
                    y += StorybookButton.HEIGHT + BUTTON_GAP
                }
                button("option:$option", option, x = x, y = y, size = size) { change(ArgDto.text(option)) }.isSelected = option == arg.text
                x += size.width + BUTTON_GAP
            }
        }

        // `<` and `>` around the value, going round the options, as a pick may have many.
        private fun pickEditor() {
            val options = requireNotNull(control.options)
            val index = options.indexOf(arg.text)
            button("previous", "<", x = 0.0) { change(ArgDto.text(options[(index - 1 + options.size) % options.size])) }
            valueText = centeredText(requireNotNull(arg.text), x = StorybookButton.HEIGHT, width = PICK_VALUE_WIDTH)
            button("next", ">", x = StorybookButton.HEIGHT + PICK_VALUE_WIDTH) { change(ArgDto.text(options[(index + 1) % options.size])) }
        }

        // Changed once it is entered, or left.
        private fun textEditor() {
            input(requireNotNull(arg.text), x = 0.0, width = TEXT_INPUT_WIDTH) { text -> change(ArgDto.text(text)) }
        }

        // A swatch of the color and its hex, changed once it is entered, or left. A hex that is no color is undone.
        private fun colorEditor() {
            val hex = requireNotNull(arg.text)
            colorSwatch = editor.solidRect(Size(StorybookButton.HEIGHT, StorybookButton.HEIGHT), Colors["#$hex"])
            input(hex, x = StorybookButton.HEIGHT + BUTTON_GAP, width = COLOR_INPUT_WIDTH) { text ->
                val color = text.removePrefix("#").uppercase()
                if (HEX_COLOR.matches(color)) change(ArgDto.text(color)) else textInput?.text = hex
            }
        }

        private fun change(value: ArgDto) {
            if (value != arg) delegate?.argChanged(control.name, value)
        }

        private fun button(
            key: String,
            label: String,
            x: Double,
            y: Double = 0.0,
            size: Size = Size(StorybookButton.HEIGHT, StorybookButton.HEIGHT),
            onTapped: () -> Unit,
        ): StorybookButton = StorybookButton(label, size, onTapped).addTo(editor).position(x, y).also { button -> buttons[key] = button }

        private fun centeredText(
            value: String,
            x: Double,
            width: Double,
        ): Text =
            editor.text(value, textSize = 16.0, color = Colors.WHITE).also { text ->
                text.position(x + (width - text.width) / 2, (StorybookButton.HEIGHT - 16.0) / 2)
            }

        private fun input(
            value: String,
            x: Double,
            width: Double,
            onEntered: (String) -> Unit,
        ) {
            val input = UITextInput(value, Size(width, StorybookButton.HEIGHT)).addTo(editor).position(x, 0.0)
            input.onReturnPressed { onEntered(input.text) }
            input.onFocusLost { onEntered(input.text) }
            textInput = input
        }
    }
}
