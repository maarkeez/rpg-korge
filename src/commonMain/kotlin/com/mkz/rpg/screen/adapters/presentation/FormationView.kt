package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.text.TextAlignment
import korlibs.korge.input.onClick
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiText
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.SolidRect
import korlibs.korge.view.image
import korlibs.korge.view.position
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size

/**
 * The party formation, displayed in place of the pad while the player sets the party order. A row per party member, in
 * party order: the crown on the leader, the portrait, the name, and the health and mana bars. At the bottom, a hint and
 * Close. Tapping a row is handed to the delegate, which selects it, or swaps it with the selected one.
 */
open class FormationView(
    private val resourcesLoader: BattleSceneResourcesLoader,
    private val texts: Texts,
) : Container() {
    companion object {
        /** The party members displayed: the rows that fit in place of the pad. */
        const val ROWS = 5
        const val CROWN = "battlefield/crown.png"

        private const val WIDTH = 390.0
        private const val HEIGHT = 300.0
        private const val PADDING = 10.0
        private const val ROW_HEIGHT = 44.0
        private const val ROW_SPACING = 4.0
        private const val BAR_HEIGHT = 32.0
        private const val CLOSE_WIDTH = 90.0
        private const val SPACING = 8.0
    }

    /** A party member, from the leader, with its health and mana left out of their maximum. */
    data class Row(
        val unitId: String,
        val name: String,
        val remainingHealth: Int,
        val maximumHealth: Int,
        val remainingMana: Int,
        val maximumMana: Int,
        val isLeader: Boolean,
    )

    interface Delegate {
        /** The row of the party member at [index] in the party order was tapped. */
        fun formationRowSelected(index: Int)

        fun formationCloseSelected()
    }

    private var delegate: Delegate? = null
    internal val rowViews: List<FormationRowView> = List(ROWS) { FormationRowView(Size(WIDTH - 2 * PADDING, ROW_HEIGHT), resourcesLoader) }
    internal lateinit var hintText: UIText
    internal lateinit var closeButton: UIButton

    init {
        rowViews.forEachIndexed { index, rowView ->
            rowView.position(PADDING, PADDING + index * (ROW_HEIGHT + ROW_SPACING))
            rowView.setOnSelected { delegate?.formationRowSelected(index) }
            addChild(rowView)
        }
        val footerY = HEIGHT - PADDING - BAR_HEIGHT
        hintText =
            uiText("", size = Size(WIDTH - 2 * PADDING - CLOSE_WIDTH - SPACING, BAR_HEIGHT)) {
                styles.textAlignment = TextAlignment.MIDDLE_LEFT
                position(PADDING, footerY)
            }
        closeButton =
            uiButton(texts[UiText.FORMATION_CLOSE], size = Size(CLOSE_WIDTH, BAR_HEIGHT)) {
                bgColorOut = Colors.DIMGRAY
                bgColorOver = RGBA(85, 85, 85)
                background.borderColor = Colors.LIGHTGRAY
                position(WIDTH - PADDING - CLOSE_WIDTH, footerY)
                onClick { delegate?.formationCloseSelected() }
            }
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Displays a row per party member, in party order, the crown on the leader, and hides the rest. Clears the selected row. */
    open fun display(rows: List<Row>) {
        rowViews.forEachIndexed { index, rowView ->
            val row = rows.getOrNull(index)
            if (row != null) rowView.display(row) else rowView.visible = false
        }
        selectRow(null)
    }

    /** Frames the selected row, or none when [index] is `null`. */
    open fun selectRow(index: Int?) {
        rowViews.forEachIndexed { rowIndex, rowView -> rowView.isSelected = rowIndex == index }
    }

    /** Displays the [hint] next to Close, e.g.: Tap a member, then another one, to swap them. */
    open fun displayHint(hint: String) {
        hintText.text = hint
    }
}

/** A party member in the formation: the crown when it leads, its portrait, its name, and its health and mana bars. */
class FormationRowView(
    size: Size,
    private val resourcesLoader: BattleSceneResourcesLoader,
) : Container() {
    companion object {
        // FamiCube palette colours: grey, and the blue of the selected row.
        val ROW_COLOR = RGBA(0x34, 0x34, 0x34)
        val SELECTED_ROW_COLOR = RGBA(0x00, 0x52, 0x80)

        private const val CROWN_SIZE = 32.0
        private const val CROWN_SCALE = 2.0
        private const val PORTRAIT_SCALE = 1.0
        private const val SPACING = 6.0
        private const val NAME_WIDTH = 110.0
        private const val BAR_HEIGHT = 16.0
    }

    private var onSelected: (() -> Unit)? = null
    private val background: SolidRect = solidRect(size, ROW_COLOR)
    internal var crownImage: Image? = null
        private set

    /** Whether the row is crowned, as its party member leads. */
    var isCrowned = false
        private set
    internal val portraitView = UnitPortraitView(Size(size.height, size.height), resourcesLoader, PORTRAIT_SCALE)
    internal lateinit var nameText: UIText
    internal val healthBarView: HealthBarView
    internal val manaBarView: ManaBarView

    /** Whether the row is selected, to be swapped with the next row tapped. */
    var isSelected = false
        set(value) {
            field = value
            background.color = if (value) SELECTED_ROW_COLOR else ROW_COLOR
        }

    init {
        val crownX = SPACING
        val portraitX = crownX + CROWN_SIZE + SPACING
        val nameX = portraitX + size.height + SPACING
        val barsX = nameX + NAME_WIDTH + SPACING
        val barsWidth = size.width - barsX - SPACING
        portraitView.position(portraitX, 0.0)
        addChild(portraitView)
        nameText =
            uiText("", size = Size(NAME_WIDTH, size.height)) {
                styles.textAlignment = TextAlignment.MIDDLE_LEFT
                position(nameX, 0.0)
            }
        val barsY = (size.height - 2 * BAR_HEIGHT - SPACING / 2) / 2
        healthBarView = HealthBarView(Size(barsWidth, BAR_HEIGHT)).also { bar -> bar.position(barsX, barsY) }
        manaBarView = ManaBarView(Size(barsWidth, BAR_HEIGHT)).also { bar -> bar.position(barsX, barsY + BAR_HEIGHT + SPACING / 2) }
        addChild(healthBarView)
        addChild(manaBarView)
        // The whole row can be tapped, also on its portrait and its bars.
        onClick { onSelected?.invoke() }
    }

    fun setOnSelected(onSelected: () -> Unit) {
        this.onSelected = onSelected
    }

    fun display(row: FormationView.Row) {
        crownImage?.removeFromParent()
        isCrowned = row.isLeader
        crownImage =
            resourcesLoader.bitmapOrNull(FormationView.CROWN)?.takeIf { row.isLeader }?.let { crownBitmap ->
                image(crownBitmap) {
                    smoothing = false
                    scale = CROWN_SCALE
                    position(SPACING, (this@FormationRowView.background.height - CROWN_SIZE) / 2)
                }
            }
        portraitView.display(row.unitId)
        nameText.text = row.name
        healthBarView.display(remaining = row.remainingHealth, maximum = row.maximumHealth)
        manaBarView.display(remaining = row.remainingMana, maximum = row.maximumMana)
        visible = true
    }
}
