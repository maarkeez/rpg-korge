package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.PreviewBarView
import korlibs.image.color.RGBA
import korlibs.math.geom.Size

class ManaBarPreviewView(
    size: Size,
) : PreviewBarView(
        size = size,
        filledColor = RGBA(0, 145, 255),
        previewColor = RGBA(0x96, 0xCD, 0xFF),
    )
