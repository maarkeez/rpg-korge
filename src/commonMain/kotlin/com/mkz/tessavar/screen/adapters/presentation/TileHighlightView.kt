package com.mkz.tessavar.screen.adapters.presentation

import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image

class TileHighlightView(
    resourcesLoader: GameSceneResourcesLoader,
    val kind: Kind,
) : Container() {
    enum class Kind(
        internal val path: String,
    ) {
        WALKING_RANGE("battlefield/tile_selection_3.png"),

        CAST_RANGE("battlefield/tile_selection_2.png"),

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
