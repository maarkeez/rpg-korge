package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.BarView
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.math.geom.Size

class HealthBarView(
    size: Size,
) : BarView(size, UiPalette.hp)
