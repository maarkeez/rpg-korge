import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.sceneContainer
import korlibs.math.geom.Size
import screen.BattleScene

suspend fun main() =
    Korge(windowSize = Size(390, 844), backgroundColor = Colors["#2b2b2b"]) {
        val sceneContainer = sceneContainer()

        sceneContainer.changeTo { BattleScene() }
    }
