package com.mkz.rpg.shared.adapters.presentation

import korlibs.image.paint.Paint

// Page object of [PreviewBarView]. Also used by its subclasses (e.g.: `HealthBarPreviewView`, `ManaBarPreviewView`).

/** The value before and after the change, e.g.: "30 → 18". */
fun PreviewBarView.labelText(): String = "${beforeText?.text} → ${afterText?.text}"

/** The share of the bar filled with the value after the change, or before it when it is a gain. */
fun PreviewBarView.filledShare(): Double = filledRatio

/** The share of the bar previewed: up to the value before a loss, or after a gain. */
fun PreviewBarView.previewShare(): Double = previewRatio

/** The colour of the previewed part, or null when nothing is previewed, as the value does not change. */
fun PreviewBarView.previewColor(): Paint? = preview?.takeIf { it.visible }?.fill
