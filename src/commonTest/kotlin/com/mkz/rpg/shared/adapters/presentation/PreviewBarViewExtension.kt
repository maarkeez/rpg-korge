package com.mkz.rpg.shared.adapters.presentation

// Page object of [PreviewBarView]. Also used by its subclasses (e.g.: `HealthBarPreviewView`, `ManaBarPreviewView`).

fun PreviewBarView.labelText(): String = label.text

fun PreviewBarView.filledWidth(): Double = filled.width * filled.ratio.toDouble()

fun PreviewBarView.previewWidth(): Double = preview.width * preview.ratio.toDouble()
