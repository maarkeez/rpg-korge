package com.mkz.tessavar.shared.adapters.presentation

fun BarView.labelText(): String = label.text

fun BarView.isLabelShown(): Boolean = label.isShown()

fun BarView.isProgressBarShown(): Boolean = track?.isShown() == true

fun BarView.progressPercentage(): Double = ratio * 100

fun BarView.isLabelAfterBar(): Boolean {
    val track = track ?: return false
    return label.x >= track.x + track.width
}
