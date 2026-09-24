package com.mkz.rpg.levelDesign.screen

import korlibs.korge.Korge
import korlibs.korge.scene.sceneContainer
import korlibs.math.geom.Size

suspend fun main() =
    Korge(windowSize = Size(width = 2560, height = 1440), backgroundColor = LevelDesignColors.BLACK) {
        val sceneContainer = sceneContainer()

        sceneContainer.changeTo { LevelDesignScene() }
    }
