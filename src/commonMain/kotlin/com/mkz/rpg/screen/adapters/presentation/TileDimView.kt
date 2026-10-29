package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size

/**
 * The black mask over a tile of the battlefield that can not be selected, darkening what is on it, e.g.: its battle
 * unit, so the tiles that can be selected stand out.
 */
class TileDimView : Container() {
    init {
        solidRect(Size(BattlefieldView.TILE_SIZE, BattlefieldView.TILE_SIZE), Colors.BLACK)
        alpha = BattlefieldView.DIM_ALPHA
    }
}
