package com.mkz.tessavar.shared.adapters.presentation

import korlibs.image.paint.Paint

fun PreviewBarView.labelText(): String = "${beforeText?.text} → ${afterText?.text}"

fun PreviewBarView.filledShare(): Double = filledRatio

fun PreviewBarView.previewShare(): Double = previewRatio

fun PreviewBarView.previewColor(): Paint? = preview?.takeIf { it.visible }?.fill
