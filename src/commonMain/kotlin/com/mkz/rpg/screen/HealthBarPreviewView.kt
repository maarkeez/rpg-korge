package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.PreviewBarView
import korlibs.image.color.RGBA
import korlibs.math.geom.Size

class HealthBarPreviewView(
    size: Size,
) : PreviewBarView(
        size = size,
        filledColor = RGBA(255, 55, 95),
        previewColor = RGBA.float(205.0, 105.0, 145.0, 0.5),
    )
