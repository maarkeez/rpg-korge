import battle.adapters.presentation.BattleApi
import battle.adapters.presentation.BattleInfoPresenter
import battle.adapters.presentation.BattleInfoView
import battle.adapters.presentation.FinishTurnPresenter
import battle.adapters.presentation.FinishTurnView
import battlefield.adapters.presentation.BattlefieldApi
import battlefield.adapters.presentation.BattlefieldPresenter
import battlefield.adapters.presentation.BattlefieldView
import battlesetup.adapters.presentation.SetupBattle
import korlibs.encoding.Hex
import korlibs.time.*
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.tween.*
import korlibs.korge.view.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.image.text.TextAlignment
import korlibs.image.text.TextAlignment.Companion.MIDDLE_CENTER
import korlibs.io.file.std.*
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textSize
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiBackgroundColor
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiGridFill
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiMaterialLayer
import korlibs.korge.ui.uiProgressBar
import korlibs.korge.ui.uiSelectedColor
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiUnselectedColor
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.align.centerXOn
import korlibs.math.geom.*
import korlibs.math.interpolation.*
import player.adapters.presentation.PlayerApi
import shared.domain.EventBus

suspend fun main() = Korge(windowSize = Size(390, 844), backgroundColor = Colors["#2b2b2b"]) {
	val sceneContainer = sceneContainer()

	sceneContainer.changeTo { MyScene() }
}

class MyScene : Scene() {
	override suspend fun SContainer.sceneMain() {

        // Event bus
        val eventBus = EventBus()
        addUpdater {
            eventBus.dispatch()
        }

        // Backend APIs
        val playerApi = PlayerApi()
        val battleApi = BattleApi(eventBus)
        val battlefieldApi = BattlefieldApi(eventBus)

        // Main scene
        uiVerticalStack(padding = 2.0) {
            uiSpacing(Size(0, 10))
            // Battle info
            val battleInfoView = BattleInfoView(this)
            val battleInfoPresenter = BattleInfoPresenter(
                battleInfoView,
                battleApi,
                playerApi,
                eventBus,
            )
            // Battlefield
            val battlefieldView = BattlefieldView(this)
            val battlefieldPresenter = BattlefieldPresenter(battlefieldView, battlefieldApi, eventBus)

            uiVerticalStack(padding = 5.0) {
                uiSpacing(Size(0, 10))
                uiHorizontalStack {
                    uiButton("\uD83D\uDC79").also { button ->
                        button.size = Size(width= 97.5, height=97.5)
                        button.bgColorOut = Colors.WHITE
                        button.bgColorOver = Colors.LIGHTSKYBLUE
                        button.background.borderColor = Colors.LIGHTGRAY
                    }
                    uiSpacing(Size(10, 0))
                    uiVerticalStack(padding = 1.0) {
                        uiText("Goblin", size = Size(width=281.5, height=14))
                        uiText("Movements left: 2    Remaining casts: 1", size = Size(width=281.5, height=14)) {}
                        uiSpacing(Size(0, 15))
                        container {
                            val healthBar = uiProgressBar(size = Size(281.5, 16), current = 75f, maximum = 100f).also { progressBar ->
                                progressBar.styles.uiSelectedColor = RGBA(255, 55, 95)
                                progressBar.styles.uiBackgroundColor = Colors.DIMGREY
                            }
                            val healthLabel = text(
                                text = "75 / 100",
                                textSize = 14.0,
                                color = Colors.WHITE
                            )
                            healthLabel.centerXOn(healthBar)
                            healthLabel.y = (healthBar.height - healthLabel.height) / 2 + 1
                        }

                        uiSpacing(Size(0, 4))
                        container {
                            val manaBar = uiProgressBar(size = Size(281.5, 16), current = 40f, maximum = 100f).also { progressBar ->
                                progressBar.styles.uiSelectedColor = RGBA(0, 145, 255)
                                progressBar.styles.uiBackgroundColor = Colors.DIMGREY
                            }
                            val manaLabel = text(
                                text = "40 / 100",
                                textSize = 14.0,
                                color = Colors.WHITE
                            )
                            manaLabel.centerXOn(manaBar)
                            manaLabel.y = (manaBar.height - manaLabel.height) / 2 + 1
                        }
                    }
                }
                uiHorizontalStack(padding = 2.0) {
                    repeat(6){
                        uiButton().also { button ->
                            button.size = Size(width= 48.75, height=48.75)
                            button.bgColorOut = Colors.WHITE
                            button.bgColorOver = Colors.LIGHTSKYBLUE
                            button.background.borderColor = Colors.LIGHTGRAY
                        }
                    }
                }
                uiSpacing(Size(0, 5))
                val finishTurnView = FinishTurnView(this)
                val finishTurnPresenter = FinishTurnPresenter(finishTurnView, battleApi)
            }
        }

        // Start game
        val setupBattle = SetupBattle(
            playerApi,
            battleApi,
            battlefieldApi
        )
        setupBattle()

	}
}
