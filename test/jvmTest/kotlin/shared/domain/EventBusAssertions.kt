package shared.domain

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat

class EventBusAssert(
    actual: FakeEventBus?
) : AbstractAssert<EventBusAssert, FakeEventBus>(
    actual,
    EventBusAssert::class.java
) {

    fun hasPublishedEvents(
        vararg expected: DomainEvent
    ): EventBusAssert {

        isNotNull

        assertThat(actual.publishedEvents)
            .containsExactly(*expected)

        return this
    }
}

fun assertThat(eventBus: FakeEventBus): EventBusAssert =
    EventBusAssert(eventBus)
