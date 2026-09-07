package screen

import korlibs.image.color.*
import korlibs.math.geom.*
import shared.adapters.presentation.PreviewBarView

class ManaBarPreviewView(size: Size) : PreviewBarView(
    size = size,
    filledColor = RGBA(0, 145, 255),
    previewColor = RGBA.float(50.0, 95.0, 205.0, 0.5),
)
