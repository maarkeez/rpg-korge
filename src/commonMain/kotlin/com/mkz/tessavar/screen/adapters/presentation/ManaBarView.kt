package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.BarView
import korlibs.image.color.RGBA
import korlibs.math.geom.Size

class ManaBarView(
    size: Size,
    showValue: Boolean = true,
) : BarView(size, RGBA(0, 145, 255), showValue)
