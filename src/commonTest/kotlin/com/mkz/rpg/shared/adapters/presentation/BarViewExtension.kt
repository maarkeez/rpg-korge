package com.mkz.rpg.shared.adapters.presentation

// Page object of [BarView]. Also used by its subclasses (e.g.: `HealthBarView`, `ManaBarView`).

fun BarView.labelText(): String = label.text

fun BarView.isLabelShown(): Boolean = label.isShown()

fun BarView.isProgressBarShown(): Boolean = track?.isShown() == true

fun BarView.progressPercentage(): Double = ratio * 100

/** Whether the value is displayed after the bar, on its right. */
fun BarView.isLabelAfterBar(): Boolean {
    val track = track ?: return false
    return label.x >= track.x + track.width
}
