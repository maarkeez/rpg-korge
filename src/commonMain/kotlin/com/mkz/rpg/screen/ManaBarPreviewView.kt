package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.PreviewBarView
import korlibs.image.color.RGBA
import korlibs.math.geom.Size

class ManaBarPreviewView(
    size: Size,
) : PreviewBarView(
        size = size,
        filledColor = RGBA(0, 145, 255),
        previewColor = RGBA.float(50.0, 95.0, 205.0, 0.5),
    )
