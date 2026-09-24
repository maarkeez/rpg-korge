package com.mkz.rpg.levelDesign.screen

import korlibs.image.color.Colors
import korlibs.image.color.RGBA

object LevelDesignColors {
    val WHITE = Colors["#ffffff"]
    val BLACK = Colors["#1d1d1f"]

    object Red {
        val DEFAULT = RGBA(255, 56, 60)
        val CONTRAST = RGBA(233, 21, 45)
    }

    object Blue {
        val DEFAULT = RGBA(0, 136, 255)
        val CONTRAST = RGBA(30, 110, 244)
    }

    object SystemGray1 {
        val DEFAULT = RGBA(142, 142, 147)
        val CONTRAST = RGBA(108, 108, 112)
    }

    object SystemGray3 {
        val DEFAULT = RGBA(199, 199, 204)
        val CONTRAST = RGBA(174, 174, 178)
    }

    object SystemGray6 {
        val DEFAULT = RGBA(242, 242, 247)
        val CONTRAST = RGBA(235, 235, 240)
    }
}
