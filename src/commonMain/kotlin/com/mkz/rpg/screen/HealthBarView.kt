package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.BarView
import korlibs.image.color.RGBA
import korlibs.math.geom.Size

class HealthBarView(
    size: Size,
) : BarView(size, RGBA(255, 55, 95))
