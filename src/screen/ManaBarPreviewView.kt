package screen

import korlibs.image.color.*
import korlibs.korge.style.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.math.geom.*
import shared.adapters.presentation.BarView
import shared.adapters.presentation.PreviewBarView

class ManaBarPreviewView(size: Size) : PreviewBarView(
    size = size,
    filledColor = RGBA(0, 145, 255),
    previewColor = RGBA.float(50.0, 95.0, 205.0, 0.5),
)
