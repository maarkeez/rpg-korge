package com.mkz.rpg.levelDesign.screen

import com.mkz.rpg.levelDesign.screen.LevelDesignColors.SystemGray3
import com.mkz.rpg.levelDesign.screen.LevelDesignColors.SystemGray6
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import korlibs.korge.scene.Scene
import korlibs.korge.style.styles
import korlibs.korge.style.textColor
import korlibs.korge.style.textSize
import korlibs.korge.ui.uiContainer
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.SContainer
import korlibs.korge.view.addUpdater
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size

class LevelDesignScene : Scene() {
    override suspend fun SContainer.sceneInit() {
    }

    override suspend fun SContainer.sceneMain() {
        // Event bus
        val eventBus = InMemoryEventBus()
        addUpdater {
            eventBus.dispatch()
        }

        // Backend APIs

        // Main scene
        val widthUnit = size.width / 16
        val heightUnit = size.height / 9

        val smallPaddingWidth = widthUnit / 2
        val smallPaddingHeight = heightUnit / 2

        val h1Size = heightUnit / 2
        val h2Size = h1Size * 0.60

        uiHorizontalStack(height = heightUnit * 16) {
            // Menu
            val menuSize = Size(width = widthUnit * 2, height = heightUnit * 9)
            uiVerticalStack(width = menuSize.width) {
                uiContainer(menuSize) {
                    solidRect(size = menuSize, color = SystemGray3.DEFAULT)
                }
            }

            // Terrains
            val terrainsSize = Size(width = widthUnit * 14, height = heightUnit * 9)
            uiVerticalStack(width = terrainsSize.width) {
                uiContainer(terrainsSize) {
                    solidRect(size = terrainsSize, color = SystemGray6.DEFAULT)
                    uiVerticalStack(width = terrainsSize.width) {
                        uiSpacing(Size(width = terrainsSize.width, height = smallPaddingHeight))
                        uiHorizontalStack(height = h1Size) {
                            uiSpacing(Size(width = smallPaddingWidth, height = h1Size))
                            uiText("Terrains") {
                                styles {
                                    textSize = h1Size
                                    textColor = LevelDesignColors.BLACK
                                }
                            }
                        }
                        uiSpacing(Size(width = terrainsSize.width, height = smallPaddingHeight))
                        uiHorizontalStack(height = h2Size) {
                            uiSpacing(Size(width = smallPaddingWidth, height = h2Size))
                            uiText("Name") {
                                styles {
                                    textSize = h2Size
                                    textColor = LevelDesignColors.BLACK
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
