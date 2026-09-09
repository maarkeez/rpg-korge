package screen

import korlibs.image.color.RGBA
import korlibs.math.geom.Size
import shared.adapters.presentation.BarView

class HealthBarView(
    size: Size,
) : BarView(size, RGBA(255, 55, 95))
