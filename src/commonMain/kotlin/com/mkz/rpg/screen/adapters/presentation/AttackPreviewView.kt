package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.animate.Animator
import korlibs.korge.animate.animator
import korlibs.korge.animate.moveTo
import korlibs.korge.input.onMouseDrag
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.korge.view.clipContainer
import korlibs.korge.view.container
import korlibs.math.clamp
import korlibs.math.geom.Size
import korlibs.math.interpolation.EASE_OUT_QUAD
import korlibs.math.interpolation.Easing
import korlibs.time.milliseconds
import kotlin.math.roundToInt

/**
 * The caster of the previewed cast, followed by a row per receiver. Displays two rows at a time, and the player drags
 * the preview vertically to scroll through the others.
 */
open class AttackPreviewView(
    private val resourcesLoader: BattleSceneResourcesLoader,
    private val texts: Texts,
) : Container() {
    companion object {
        private const val WIDTH = 389.0
        private const val ROW_HEIGHT = 97.5
        private const val ROW_SPACING = 20.0
        internal const val ROW_PITCH = ROW_HEIGHT + ROW_SPACING
        private const val VIEWPORT_HEIGHT = 2 * ROW_HEIGHT + ROW_SPACING
        private val SNAP_DURATION = 150.milliseconds
    }

    /** A battle unit receiving the cast, the health it would have left and the effects that would stay on it. */
    data class Receiver(
        val battleUnit: BattleUnitInfo,
        val remainingHealthPoints: Int,
        val newEffectIds: List<String> = emptyList(),
    )

    private lateinit var casterUnitNameView: UnitNameView
    internal lateinit var casterUnitPortraitView: UnitPortraitView
    private lateinit var casterHealthBarView: HealthBarPreviewView
    private lateinit var casterManaBarView: ManaBarPreviewView
    internal lateinit var casterEffectsView: EffectsView

    // Grows to the largest number of receivers displayed, the ones not needed are hidden.
    internal val receiverViews = mutableListOf<ReceiverPreviewView>()

    // A preview shorter than the viewport can not be scrolled.
    private var minScrollY = 0.0

    private val viewport = clipContainer(Size(WIDTH, VIEWPORT_HEIGHT))
    internal val content = viewport.container()
    internal val scrollAnimator: Animator = content.animator(parallel = true)

    init {
        content.uiHorizontalStack {
            casterUnitPortraitView = UnitPortraitView(Size(width = ROW_HEIGHT, height = ROW_HEIGHT), resourcesLoader)
            addChild(casterUnitPortraitView)

            uiSpacing(Size(10, 0))
            uiVerticalStack(padding = 1.0) {
                casterUnitNameView = UnitNameView(size = Size(width = 281.5, height = 14))
                addChild(casterUnitNameView)

                uiSpacing(Size(0, 15))
                casterHealthBarView = HealthBarPreviewView(Size(281.5, 16))
                addChild(casterHealthBarView)

                uiSpacing(Size(0, 4))
                casterManaBarView = ManaBarPreviewView(Size(281.5, 16))
                addChild(casterManaBarView)

                uiSpacing(Size(0, 4))
                casterEffectsView = EffectsView(resourcesLoader)
                addChild(casterEffectsView)
            }
        }
        // The drag offsets are relative to where the drag started.
        var dragStartY = 0.0
        viewport.onMouseDrag { info ->
            if (info.start) {
                scrollAnimator.cancel()
                dragStartY = content.y
            }
            content.y = (dragStartY + info.localDXY(content).y).clamp(minScrollY, 0.0)
            if (info.end) snapToRow()
        }
        visible = false
    }

    open fun display(
        casterBattleUnit: BattleUnitInfo,
        manaCost: Int,
        receivers: List<Receiver>,
        casterRemainingHealthPoints: Int = casterBattleUnit.remainingHealthPoints,
        casterNewEffectIds: List<String> = emptyList(),
    ) {
        casterUnitPortraitView.display(casterBattleUnit.unitId)
        casterUnitNameView.display(unitName = texts.unitName(casterBattleUnit.unitId))
        casterHealthBarView.display(
            remainingBefore = casterBattleUnit.remainingHealthPoints,
            remainingAfter = casterRemainingHealthPoints,
            maximum = casterBattleUnit.maximumHealthPoints,
        )
        casterManaBarView.display(
            remainingBefore = casterBattleUnit.remainingManaPoints,
            remainingAfter = casterBattleUnit.remainingManaPoints - manaCost,
            maximum = casterBattleUnit.maximumManaPoints,
        )
        casterEffectsView.display(
            effectIds = casterBattleUnit.effectIds,
            newEffectIds = casterNewEffectIds,
        )

        while (receiverViews.size < receivers.size) {
            val receiverView = ReceiverPreviewView(resourcesLoader, texts)
            receiverView.y = (receiverViews.size + 1) * ROW_PITCH
            content.addChild(receiverView)
            receiverViews += receiverView
        }
        receiverViews.forEachIndexed { index, receiverView ->
            val receiver = receivers.getOrNull(index)
            if (receiver == null) {
                receiverView.hide()
            } else {
                receiverView.display(
                    battleUnit = receiver.battleUnit,
                    remainingHealthPoints = receiver.remainingHealthPoints,
                    newEffectIds = receiver.newEffectIds,
                    position = index + 1,
                    count = receivers.size,
                )
            }
        }

        // Each preview starts showing the caster and the first receiver.
        minScrollY = minOf(0.0, -(receivers.size - 1) * ROW_PITCH)
        scrollAnimator.cancel()
        content.y = 0.0
        visible = true
    }

    open fun hide() {
        visible = false
    }

    // Stops on the closest row, so the rows are never cut.
    private fun snapToRow() {
        val snappedY = ((content.y / ROW_PITCH).roundToInt() * ROW_PITCH).clamp(minScrollY, 0.0)
        scrollAnimator
            .cancel()
            .moveTo(
                view = content,
                x = { 0.0 },
                y = { snappedY },
                time = SNAP_DURATION,
                easing = Easing.EASE_OUT_QUAD,
            )
    }
}
