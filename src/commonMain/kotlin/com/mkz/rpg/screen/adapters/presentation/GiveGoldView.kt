package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import com.mkz.rpg.shared.adapters.presentation.GameButton
import com.mkz.rpg.shared.adapters.presentation.gameButton
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.input.onMouseDrag
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.Text
import korlibs.korge.view.View
import korlibs.korge.view.circle
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.korge.view.xy
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import kotlin.math.roundToInt

/**
 * Takes the place of the bag to give gold: the giver and the receiver side by side, with what each would carry, and the
 * amount on a slider. Its buttons are in the call to action, so it keeps the height of the battle HUD.
 */
open class GiveGoldView(
    resourcesLoader: BattleSceneResourcesLoader,
    private val texts: Texts,
) : Container() {
    companion object {
        const val WIDTH = 378.0
        private const val HEIGHT = 292.0

        // Centered in the battle HUD.
        const val X = 6.0
        private const val MARGIN = 12.0
        private const val COLUMN_WIDTH = (WIDTH - 2 * MARGIN) / 2
        private const val GIVER_CENTER = MARGIN + COLUMN_WIDTH / 2
        private const val RECEIVER_CENTER = MARGIN + 3 * COLUMN_WIDTH / 2
        private const val PORTRAIT = 104.0
        private const val PORTRAIT_SCALE = 3.0
        private const val PORTRAIT_Y = 32.0
        private const val NAME_Y = PORTRAIT_Y + PORTRAIT + 3.0
        private const val GOLD_Y = NAME_Y + 22.0
        private const val AMOUNT_Y = GOLD_Y + 26.0
        private const val SLIDER_Y = AMOUNT_Y + 30.0
        private const val SLIDER_HEIGHT = 44.0
        private const val STEP_WIDTH = 44.0
        private const val TRACK_THICKNESS = 6.0
        private const val KNOB = 22.0
        private const val TRACK_X = MARGIN + STEP_WIDTH + 2 * 8.0 + KNOB / 2
        private const val TRACK_WIDTH = WIDTH - 2 * TRACK_X
        private const val ARROW_WIDTH = 32.0
        private const val ARROW_HEIGHT = 44.0
        private const val ARROW_SPACING = 4.0
        private const val PREVIOUS_ARROW_X = RECEIVER_CENTER - PORTRAIT / 2 - ARROW_SPACING - ARROW_WIDTH
        private const val GIVER_RIGHT = GIVER_CENTER + PORTRAIT / 2
        private const val RECEIVER_LEFT = RECEIVER_CENTER - PORTRAIT / 2
        private const val TITLE_SIZE = 16.0
        private const val NAME_SIZE = 16.0
        private const val GOLD_SIZE = 14.0
        private const val AMOUNT_SIZE = 20.0
        private const val POINTER_SIZE = 24.0
        private const val POINTER = ">"
    }

    /** A party member of the give: its name, its unit for the portrait, and the gold it carries. */
    data class Member(
        val name: String,
        val unitId: String,
        val gold: Int,
    )

    interface Delegate {
        /** The amount changed, on every whole amount the slider goes through, and with - and +. */
        fun amountChanged(amount: Int)

        /** The amount the player settled on: the finger left the slider, or - or + was tapped. */
        fun amountSettled(amount: Int) = Unit

        /** The receiver was switched to the previous ([step] -1) or next ([step] 1) one. */
        fun receiverStepped(step: Int)
    }

    private var delegate: Delegate? = null
    private var giver = Member("", "", 0)
    private var receiver = Member("", "", 0)
    private var maximum = 1

    internal var amount = 1
        private set
    private val giverPortrait = UnitPortraitView(Size(PORTRAIT, PORTRAIT), resourcesLoader, PORTRAIT_SCALE)
    private val receiverPortrait = UnitPortraitView(Size(PORTRAIT, PORTRAIT), resourcesLoader, PORTRAIT_SCALE)
    internal val giverNameText = Text("", textSize = NAME_SIZE)
    internal val receiverNameText = Text("", textSize = NAME_SIZE)
    internal val giverGoldText = Text("", textSize = GOLD_SIZE, color = HudStyle.MUTED)
    internal val receiverGoldText = Text("", textSize = GOLD_SIZE, color = HudStyle.MUTED)
    internal val amountText = Text("", textSize = AMOUNT_SIZE, color = HudStyle.SELECTION)
    private val pointer = Text(POINTER, textSize = POINTER_SIZE, color = HudStyle.SELECTION)
    internal val previousReceiverButton: GameButton
    internal val nextReceiverButton: GameButton
    internal val minusButton: GameButton
    internal val plusButton: GameButton
    private val fill: SolidRect
    private val knob: View
    internal val slider: SolidRect

    init {
        panel(Size(WIDTH, HEIGHT))
        addChild(Text(texts[UiText.GIVE_GOLD_TITLE], textSize = TITLE_SIZE).xy(MARGIN, MARGIN - 2))
        addChild(giverPortrait.xy(GIVER_CENTER - PORTRAIT / 2, PORTRAIT_Y))
        addChild(receiverPortrait.xy(RECEIVER_CENTER - PORTRAIT / 2, PORTRAIT_Y))
        addChild(pointer.xy(0.0, PORTRAIT_Y + (PORTRAIT - POINTER_SIZE) / 2))
        val arrowY = PORTRAIT_Y + (PORTRAIT - ARROW_HEIGHT) / 2
        previousReceiverButton = button("<", PREVIOUS_ARROW_X, arrowY, ARROW_WIDTH, ARROW_HEIGHT) { delegate?.receiverStepped(-1) }
        nextReceiverButton = button(">", RECEIVER_CENTER + PORTRAIT / 2 + ARROW_SPACING, arrowY, ARROW_WIDTH, ARROW_HEIGHT) { delegate?.receiverStepped(1) }
        addChild(giverNameText.xy(0.0, NAME_Y))
        addChild(receiverNameText.xy(0.0, NAME_Y))
        addChild(giverGoldText.xy(0.0, GOLD_Y))
        addChild(receiverGoldText.xy(0.0, GOLD_Y))
        addChild(amountText.xy(0.0, AMOUNT_Y))
        minusButton = button("-", MARGIN, SLIDER_Y, STEP_WIDTH, SLIDER_HEIGHT) { stepAmount(-1) }
        plusButton = button("+", WIDTH - MARGIN - STEP_WIDTH, SLIDER_Y, STEP_WIDTH, SLIDER_HEIGHT) { stepAmount(1) }
        val trackY = SLIDER_Y + (SLIDER_HEIGHT - TRACK_THICKNESS) / 2
        roundRect(Size(TRACK_WIDTH, TRACK_THICKNESS), RectCorners(TRACK_THICKNESS / 2), fill = HudStyle.TRACK) { xy(TRACK_X, trackY) }
        fill = solidRect(Size(1.0, TRACK_THICKNESS), HudStyle.SELECTION) { xy(TRACK_X, trackY) }
        knob = circle(radius = KNOB / 2, fill = Colors.WHITE) { y = SLIDER_Y + (SLIDER_HEIGHT - KNOB) / 2 }
        slider = solidRect(Size(TRACK_WIDTH + KNOB, SLIDER_HEIGHT), Colors.TRANSPARENT) { xy(TRACK_X - KNOB / 2, SLIDER_Y) }
        // The slider follows the finger on its own: displaying the view again is only needed when something else changes.
        var startX = 0.0
        slider.onMouseDrag { event ->
            if (event.start) startX = xOf(amount)
            changeAmount(amountAt(startX + event.localDXY(this@GiveGoldView).x))
            if (event.end) delegate?.amountSettled(amount)
        }
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Displays the give. The arrows hide with a single receiver, and are disabled at each end of the receivers. */
    open fun display(
        giver: Member,
        receiver: Member,
        amount: Int,
        maximum: Int,
        canStepToPrevious: Boolean,
        canStepToNext: Boolean,
    ) {
        this.giver = giver
        this.receiver = receiver
        this.maximum = maximum
        this.amount = amount
        val hasArrows = canStepToPrevious || canStepToNext
        previousReceiverButton.visible = hasArrows
        nextReceiverButton.visible = hasArrows
        previousReceiverButton.enabled = canStepToPrevious
        nextReceiverButton.enabled = canStepToNext
        // The pointer stays in the middle of what is left between the portrait of the giver and the receiver or its arrow.
        pointer.x = (GIVER_RIGHT + if (hasArrows) PREVIOUS_ARROW_X else RECEIVER_LEFT) / 2 - pointer.width / 2
        giverPortrait.display(giver.unitId, HudStyle.SELECTION)
        receiverPortrait.display(receiver.unitId, HudStyle.ALLY)
        giverNameText.displayCentered(giver.name, GIVER_CENTER, NAME_SIZE)
        receiverNameText.displayCentered(receiver.name, RECEIVER_CENTER, NAME_SIZE)
        refresh()
    }

    private fun stepAmount(step: Int) {
        changeAmount((amount + step).coerceIn(1, maximum))
        delegate?.amountSettled(amount)
    }

    private fun changeAmount(newAmount: Int) {
        if (newAmount == amount) return
        amount = newAmount
        refresh()
        delegate?.amountChanged(newAmount)
    }

    private fun refresh() {
        giverGoldText.displayCentered("${texts.gold(giver.gold)} > ${texts.gold(giver.gold - amount)}", GIVER_CENTER, GOLD_SIZE)
        receiverGoldText.displayCentered("${texts.gold(receiver.gold)} > ${texts.gold(receiver.gold + amount)}", RECEIVER_CENTER, GOLD_SIZE)
        amountText.displayCentered(texts.gold(amount), WIDTH / 2, AMOUNT_SIZE)
        knob.x = TRACK_X + xOf(amount) - KNOB / 2
        fill.scaleX = xOf(amount).coerceAtLeast(1.0)
        minusButton.enabled = amount > 1
        plusButton.enabled = amount < maximum
    }

    private fun xOf(amount: Int): Double = if (maximum <= 1) TRACK_WIDTH else TRACK_WIDTH * (amount - 1) / (maximum - 1)

    private fun amountAt(x: Double): Int = if (maximum <= 1) maximum else (1 + (x / TRACK_WIDTH * (maximum - 1)).roundToInt()).coerceIn(1, maximum)

    // A text too long for its column is drawn smaller, so it stays whole, e.g.: a long name.
    private fun Text.displayCentered(
        value: String,
        centerX: Double,
        size: Double,
    ) {
        textSize = size
        text = value
        if (width > COLUMN_WIDTH) textSize = size * COLUMN_WIDTH / width
        x = centerX - width / 2
    }

    private fun button(
        label: String,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        onSelected: () -> Unit,
    ): GameButton =
        gameButton(label, Size(width, height)) {
            xy(x, y)
            onClick { onSelected() }
        }
}
