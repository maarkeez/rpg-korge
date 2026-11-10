package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size

class TileDimView : Container() {
    init {
        solidRect(Size(BattlefieldView.TILE_SIZE, BattlefieldView.TILE_SIZE), Colors.BLACK)
        alpha = BattlefieldView.DIM_ALPHA
    }
}
