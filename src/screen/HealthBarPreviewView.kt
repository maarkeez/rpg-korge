package screen

import korlibs.image.color.*
import korlibs.korge.style.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.math.geom.*
import shared.adapters.presentation.BarView
import shared.adapters.presentation.PreviewBarView

class HealthBarPreviewView(size: Size) : PreviewBarView(
    size = size,
    filledColor = RGBA(255, 55, 95),
    previewColor = RGBA.float(205.0, 105.0, 145.0, 0.5),
)
