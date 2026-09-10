package shared.adapters.architecture

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
}
