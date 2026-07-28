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
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiGridFill
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiMaterialLayer
import korlibs.korge.ui.uiText
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

            uiHorizontalStack(padding = 1.0) {
                uiMaterialLayer().also {
                    it.size = Size(width= 195, height=200)
                    it.radius = RectCorners(16f,16f,16f, 8f)
                    it.bgColor = Colors.WHITE
                    it.borderColor = Colors.DIMGREY
                    it.borderSize = 2.0
                }
                uiMaterialLayer().also {
                    it.size = Size(width= 195, height=200)
                    it.radius = RectCorners(16f,16f,16f, 8f)
                    it.bgColor = Colors.WHITE
                    it.borderColor = Colors.DIMGREY
                    it.borderSize = 2.0
                }
            }
        }

	}
}
