package com.mkz.rpg.shared.adapters.presentation

// Page object of [BarView]. Also used by its subclasses (e.g.: `HealthBarView`, `ManaBarView`).

fun BarView.labelText(): String = label.text

fun BarView.isLabelShown(): Boolean = label.isShown()

fun BarView.isProgressBarShown(): Boolean = progressBar.isShown()

fun BarView.progressPercentage(): Double = progressBar.current
