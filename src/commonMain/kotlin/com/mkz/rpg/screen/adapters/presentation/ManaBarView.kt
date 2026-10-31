package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.BarView
import korlibs.image.color.RGBA
import korlibs.math.geom.Size

class ManaBarView(
    size: Size,
    showValue: Boolean = true,
) : BarView(size, RGBA(0, 145, 255), showValue)
