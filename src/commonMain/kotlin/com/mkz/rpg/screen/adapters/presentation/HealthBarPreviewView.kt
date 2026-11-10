package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.PreviewBarView
import korlibs.image.color.RGBA
import korlibs.math.geom.Size

class HealthBarPreviewView(
    size: Size,
) : PreviewBarView(
        size = size,
        filledColor = RGBA(255, 55, 95),
        previewColor = RGBA(0xFF, 0xC4, 0xCE),
    )
