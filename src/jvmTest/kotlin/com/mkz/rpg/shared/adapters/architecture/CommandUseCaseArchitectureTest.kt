package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CommandUseCaseArchitectureTest {
    @Test
    fun `should expose exactly one public 'operator fun invoke' returning Unit value when is a command use case`() {
        val useCaseClasses =
            Konsist
                .scopeFromProduction()
                .classes()
                .filter { it.resideInPackage("..usecases.commands..") }
                .filter { it.isTopLevel }

        assertSoftly {
            useCaseClasses.forEach { useCase ->
                val publicFunctions =
                    useCase
                        .functions()
                        .filter { it.hasPublicOrDefaultModifier }

                withClue("Use case '${useCase.name}' should declare exactly 1 public method, found ${publicFunctions.size}: ${publicFunctions.map { it.name }}") {
                    publicFunctions.shouldHaveSize(1)
                }

                val publicFunction = publicFunctions.singleOrNull() ?: return@forEach

                val missingOperatorModifier = if (!publicFunction.hasOperatorModifier) " without the 'operator' modifier" else ""
                withClue("Use case '${useCase.name}' public method must be 'operator fun invoke', found '${publicFunction.name}'$missingOperatorModifier") {
                    (publicFunction.name == "invoke" && publicFunction.hasOperatorModifier).shouldBeTrue()
                }

                val returnTypeName = publicFunction.returnType?.name ?: "Unit"

                withClue("Use case '${useCase.name}' invoke() must not Unit/void, but its return type is '$returnTypeName'") {
                    returnTypeName shouldBe "Unit"
                }
            }
        }
    }

    @Test
    fun `commands should not depend on other commands`() {
        val useCaseClasses =
            Konsist
                .scopeFromProduction()
                .classes()
                .filter { it.resideInPackage("..usecases.commands..") }
                .filter { it.isTopLevel }

        val commandNames = useCaseClasses.map { it.name }.toSet()

        assertSoftly {
            useCaseClasses.forEach { useCase ->
                val commandProperties =
                    useCase
                        .properties()
                        .filter { property ->
                            property.type?.name in commandNames
                        }

                val commandDependencies = commandProperties.map { "${it.name}: ${it.type?.name}" }
                withClue("Command '${useCase.name}' must not depend on other commands. Publish and listen to events instead. Found command dependencies: $commandDependencies") {
                    commandProperties.shouldBeEmpty()
                }
            }
        }
    }
}
