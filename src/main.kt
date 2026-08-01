import korlibs.encoding.Hex
import korlibs.time.*
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.tween.*
import korlibs.korge.view.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.image.text.TextAlignment
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
import korlibs.math.geom.*
import korlibs.math.interpolation.*

suspend fun main() = Korge(windowSize = Size(390, 844), backgroundColor = Colors["#2b2b2b"]) {
	val sceneContainer = sceneContainer()

	sceneContainer.changeTo { MyScene() }
}

class MyScene : Scene() {
	override suspend fun SContainer.sceneMain() {

        uiVerticalStack(padding = 2.0) {
            uiSpacing(Size(0, 10))
            uiText(
                text = "Human turn - Round 1",
                size = Size(width=390, height=50)
            ){
                this.styles.textAlignment = TextAlignment.MIDDLE_CENTER
                this.styles.textSize = 32.0
            }

            uiGridFill(
                size= Size(width=390, height=390),
                spacing = Spacing(2.0,2.0),
                cols = 8,
                rows = 8) {

                for (n in 0 until cols * rows) {
                    uiButton().also { button ->
                        button.bgColorOut = Colors.WHITE
                        button.bgColorOver = Colors.LIGHTSKYBLUE
                        button.background.borderColor = Colors.LIGHTGRAY
                    }
                }
            }

            uiVerticalStack(padding = 1.0) {
                uiSpacing(Size(0, 10))
                uiHorizontalStack {
                    uiButton().also { button ->
                        button.size = Size(width= 97.5, height=97.5)
                        button.bgColorOut = Colors.WHITE
                        button.bgColorOver = Colors.LIGHTSKYBLUE
                        button.background.borderColor = Colors.LIGHTGRAY
                    }
                    uiSpacing(Size(10, 0))
                    uiVerticalStack(padding = 1.0) {
                        uiText("Goblin", size = Size(width=281.5, height=14))
                        uiText("Movements left: 2    Remaining casts: 1", size = Size(width=281.5, height=14)) {}
                        uiSpacing(Size(0, 30))
                        uiProgressBar(size = Size(281.5, 8), current = 50f, maximum = 100f).also { progressBar ->
                            progressBar.styles.uiSelectedColor = Colors.RED
                            progressBar.styles.uiBackgroundColor = Colors.DIMGREY
                        }
                        uiSpacing(Size(0, 4))
                        uiProgressBar(size = Size(281.5, 8), current = 50f, maximum = 100f).also { progressBar ->
                            progressBar.styles.uiSelectedColor = Colors.BLUE
                            progressBar.styles.uiBackgroundColor = Colors.DIMGREY
                        }
                    }
                }

            }
        }

	}
}
