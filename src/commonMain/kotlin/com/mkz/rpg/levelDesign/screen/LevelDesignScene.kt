package com.mkz.rpg.levelDesign.screen

import com.mkz.rpg.levelDesign.screen.LevelDesignColors.SystemGray3
import com.mkz.rpg.levelDesign.screen.LevelDesignColors.SystemGray6
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import korlibs.korge.input.onOut
import korlibs.korge.input.onOver
import korlibs.korge.scene.Scene
import korlibs.korge.style.styles
import korlibs.korge.style.textColor
import korlibs.korge.style.textSize
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiContainer
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.SContainer
import korlibs.korge.view.addUpdater
import korlibs.korge.view.align.alignRightToRightOf
import korlibs.korge.view.solidRect
import korlibs.math.geom.RectCorners
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
        val pSize = h1Size * 0.35

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
                    val terrainContainer = this
                    solidRect(size = terrainsSize, color = SystemGray6.DEFAULT)
                    uiVerticalStack(width = terrainsSize.width) {
                        uiSpacing(Size(width = 0, height = smallPaddingHeight))
                        uiHorizontalStack(height = h1Size) {
                            styles {
                                textSize = h1Size
                                textColor = LevelDesignColors.BLACK
                            }
                            uiSpacing(Size(width = smallPaddingWidth, height = 0))
                            uiText("Terrains")
                        }
                        uiSpacing(Size(width = 0, height = smallPaddingHeight))
                        uiHorizontalStack(height = h2Size) {
                            val parent = this
                            styles {
                                textSize = h2Size
                                textColor = LevelDesignColors.BLACK
                            }
                            uiSpacing(Size(width = smallPaddingWidth, height = 0))
                            uiText("Name")
                            uiHorizontalStack {
                                styles {
                                    textSize = h2Size
                                    textColor = LevelDesignColors.WHITE
                                }
                                uiButton("Add", size = Size(width = widthUnit, height = h2Size)) {
                                    background.radius = RectCorners(16f, 16f, 16f, 16f)
                                    bgColorOut = LevelDesignColors.Blue.DEFAULT
                                    bgColorOver = LevelDesignColors.Blue.CONTRAST
                                    textSize = pSize
                                    textColor = SystemGray6.DEFAULT
                                    onOver {
                                        textColor = SystemGray6.CONTRAST
                                    }
                                    onOut {
                                        textColor = SystemGray6.DEFAULT
                                    }
                                }
                                uiSpacing(Size(width = smallPaddingWidth, height = 0))
                                alignRightToRightOf(terrainContainer)
                            }
                        }
                        uiSpacing(Size(width = 0, height = smallPaddingHeight))
                        uiVerticalStack(width = terrainsSize.width, padding = smallPaddingHeight) {
                            repeat(8) { index ->
                                uiHorizontalStack(height = pSize) {
                                    styles {
                                        textSize = pSize
                                        textColor = LevelDesignColors.BLACK
                                        onOver {
                                            textColor = LevelDesignColors.Blue.CONTRAST
                                        }
                                        onOut {
                                            textColor = LevelDesignColors.BLACK
                                        }
                                    }
                                    uiSpacing(Size(width = smallPaddingWidth, height = 0))
                                    uiText("Terrain ${index + 1}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
