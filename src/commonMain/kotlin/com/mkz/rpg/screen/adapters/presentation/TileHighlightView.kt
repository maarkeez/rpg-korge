package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image

/**
 * The frame drawn over a tile of the battlefield to point it out, e.g.: a tile the selected battle unit can move to,
 * or a tile its ability can be cast on.
 */
class TileHighlightView(
    resourcesLoader: BattleSceneResourcesLoader,
    val kind: Kind,
) : Container() {
    enum class Kind(
        internal val path: String,
    ) {
        /** A tile the selected battle unit can move to. */
        MOVE_RANGE("battlefield/tile_selection_3.png"),

        /** A tile the selected ability can be cast on. */
        CAST_RANGE("battlefield/tile_selection_2.png"),

        /** The tile selected, e.g.: of the battle unit selected, or a tile of the cast previewed. */
        SELECTED("battlefield/tile_selection_4.png"),
    }

    internal val frameImage: Image =
        resourcesLoader.bitmap(kind.path).let { bitmap ->
            image(bitmap) {
                scale = BattlefieldView.tileFillingScale(bitmap)
                smoothing = false
            }
        }
}
