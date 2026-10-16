package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.tests.ViewsForTesting

// Page object of [AttackPreviewView].

fun AttackPreviewView.isCasterPortraitVisible(): Boolean = casterUnitPortraitView.visible

/** The receivers displayed, scrolled into view or not. */
fun AttackPreviewView.receiversCount(): Int = receiverViews.count { receiverView -> receiverView.visible }

fun AttackPreviewView.isReceiverPortraitVisible(index: Int): Boolean = receiverViews.getOrNull(index)?.let { it.visible && it.unitPortraitView.visible } ?: false

/** The counter of the receiver, e.g.: "2/3", or null when it is not displayed. */
fun AttackPreviewView.receiverCounter(index: Int): String? = receiverViews[index].counterLabel.takeIf { it.visible }?.text

/** The first row scrolled into view, being the caster row 0 and the receiver rows the next ones. */
fun AttackPreviewView.firstVisibleRow(): Double = -content.y / AttackPreviewView.ROW_PITCH

/** Drags the preview vertically by [dy], from its center, and waits until it stops on a row. The preview must be on the stage. */
suspend fun ViewsForTesting.dragVertically(
    attackPreviewView: AttackPreviewView,
    dy: Double,
) {
    val center = attackPreviewView.getGlobalBounds().center
    mouseMoveTo(center.x, center.y)
    mouseDown()
    mouseMoveTo(center.x, center.y + dy)
    mouseUp()
    attackPreviewView.scrollAnimator.awaitComplete()
}
