package com.mkz.rpg

import com.mkz.rpg.screen.BattleScene
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.korge.Korge
import korlibs.korge.scene.sceneContainer
import korlibs.math.geom.Size

suspend fun main(args: Array<String>) =
    Korge(windowSize = Size(390, 844), backgroundColor = UiPalette.background, args = args) {
        val sceneContainer = sceneContainer()
        val scenarioPath = args.firstOrNull { it.endsWith(".json") }
        sceneContainer.changeTo {
            BattleScene(
                scenarioPath = scenarioPath,
                debugEnabled = true,
            )
        }
    }
