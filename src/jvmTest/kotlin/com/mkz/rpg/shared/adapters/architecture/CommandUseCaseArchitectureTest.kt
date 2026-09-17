package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import org.assertj.core.api.SoftAssertions
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

        SoftAssertions.assertSoftly { softly ->
            useCaseClasses.forEach { useCase ->
                val publicFunctions =
                    useCase
                        .functions()
                        .filter { it.hasPublicOrDefaultModifier }

                softly
                    .assertThat(publicFunctions)
                    .withFailMessage(
                        "Use case '%s' should declare exactly 1 public method, found %d: %s",
                        useCase.name,
                        publicFunctions.size,
                        publicFunctions.map { it.name },
                    ).hasSize(1)

                val publicFunction = publicFunctions.singleOrNull() ?: return@forEach

                softly
                    .assertThat(publicFunction.name == "invoke" && publicFunction.hasOperatorModifier)
                    .withFailMessage(
                        "Use case '%s' public method must be 'operator fun invoke', found '%s'%s",
                        useCase.name,
                        publicFunction.name,
                        if (!publicFunction.hasOperatorModifier) " without the 'operator' modifier" else "",
                    ).isTrue()

                val returnTypeName = publicFunction.returnType?.name ?: "Unit"

                softly
                    .assertThat(returnTypeName)
                    .withFailMessage(
                        "Use case '%s' invoke() must not Unit/void, but its return type is '%s'",
                        useCase.name,
                        returnTypeName,
                    ).isEqualTo("Unit")
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

        SoftAssertions.assertSoftly { softly ->
            useCaseClasses.forEach { useCase ->
                val commandProperties =
                    useCase
                        .properties()
                        .filter { property ->
                            property.type?.name in commandNames
                        }

                softly
                    .assertThat(commandProperties)
                    .withFailMessage(
                        "Command '%s' must not depend on other commands. Publish and listen to events instead. Found command dependencies: %s",
                        useCase.name,
                        commandProperties.map {
                            "${it.name}: ${it.type?.name}"
                        },
                    ).isEmpty()
            }
        }
    }
}
