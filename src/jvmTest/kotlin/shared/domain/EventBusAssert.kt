package shared.domain

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat

class EventBusAssert(
    actual: shared.domain.FakeEventBus?,
) : AbstractAssert<shared.domain.EventBusAssert, shared.domain.FakeEventBus>(
        actual,
        _root_ide_package_.shared.domain.EventBusAssert::class.java,
    ) {
    fun hasPublishedEvents(vararg expected: DomainEvent): shared.domain.EventBusAssert {
        isNotNull

        assertThat(actual.publishedEvents)
            .containsExactly(*expected)

        return this
    }
}

fun assertThat(eventBus: shared.domain.FakeEventBus): shared.domain.EventBusAssert = _root_ide_package_.shared.domain.EventBusAssert(eventBus)
