package shared.domain

import kotlin.reflect.KClass

class FakeEventBus : EventBus {

    private val events = mutableListOf<DomainEvent>()

    val publishedEvents: List<DomainEvent>
        get() = events

    override fun publish(events: Set<DomainEvent>) {
        this.events += events
    }

    override fun publish(event: DomainEvent) {
        events += event
    }

    override fun dispatch() {
        // The fake only records published events.
    }

    override fun <T : DomainEvent> subscribe(
        eventType: KClass<T>,
        handler: (T) -> Unit
    ): Subscription =
        Subscription {}
}
