package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.GameButton
import com.mkz.tessavar.shared.adapters.presentation.gameButton
import korlibs.image.text.TextAlignment
import korlibs.korge.input.onClick
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiText
import korlibs.korge.view.Container
import korlibs.korge.view.position
import korlibs.math.geom.Size

open class SaveSlotsView(
    messages: Messages,
) : Container() {
    companion object {
        const val ROWS = 3

        private const val WIDTH = 390.0
        private const val PADDING = 10.0
        private const val TITLE_HEIGHT = 30.0
        private const val NUMBER_WIDTH = 24.0
        private const val ROWS_Y = 72.0
        private const val ROW_HEIGHT = 48.0
        private const val ROW_SPACING = 32.0
        private const val DELETE_WIDTH = 80.0
    }

    data class Slot(
        val number: Int,
        val label: String,
        val isEnabled: Boolean,
        val isDeletable: Boolean,
    )

    interface Delegate {
        fun saveSlotSelected(number: Int)

        fun deleteSaveSelected(number: Int)
    }

    private var delegate: Delegate? = null
    internal var slots: List<Slot> = emptyList()
        private set

    internal val titleText: UIText =
        uiText("", size = Size(WIDTH - 2 * PADDING, TITLE_HEIGHT)) {
            styles.textAlignment = TextAlignment.MIDDLE_CENTER
            position(PADDING, PADDING)
        }
    internal val numberTexts: List<UIText> =
        List(ROWS) { index ->
            uiText("${index + 1}", size = Size(NUMBER_WIDTH, ROW_HEIGHT)) {
                styles.textAlignment = TextAlignment.MIDDLE_CENTER
                position(PADDING, rowY(index))
            }
        }
    internal val slotButtons: List<GameButton> =
        List(ROWS) { index ->
            gameButton("", Size(slotWidth(isDeletable = false), ROW_HEIGHT)) {
                position(PADDING + NUMBER_WIDTH + PADDING, rowY(index))
                onClick { if (enabled) delegate?.saveSlotSelected(index + 1) }
            }
        }
    internal val deleteButtons: List<GameButton> =
        List(ROWS) { index ->
            gameButton(messages[UiMessage.SAVE_SLOTS_DELETE], Size(DELETE_WIDTH, ROW_HEIGHT)) {
                position(WIDTH - PADDING - DELETE_WIDTH, rowY(index))
                visible = false
                onClick { delegate?.deleteSaveSelected(index + 1) }
            }
        }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    open fun display(
        title: String,
        slots: List<Slot>,
    ) {
        this.slots = slots
        titleText.text = title
        slotButtons.forEachIndexed { index, button ->
            val slot = slots.getOrNull(index)
            button.visible = slot != null
            numberTexts[index].visible = slot != null
            deleteButtons[index].visible = slot?.isDeletable == true
            if (slot == null) return@forEachIndexed
            button.text = slot.label
            button.enabled = slot.isEnabled
            button.width = slotWidth(slot.isDeletable)
        }
    }

    private fun rowY(index: Int) = ROWS_Y + index * (ROW_HEIGHT + ROW_SPACING)

    private fun slotWidth(isDeletable: Boolean) = WIDTH - 3 * PADDING - NUMBER_WIDTH - if (isDeletable) DELETE_WIDTH + PADDING else 0.0
}
