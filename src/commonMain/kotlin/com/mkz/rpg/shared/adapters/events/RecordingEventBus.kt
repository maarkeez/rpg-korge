package com.mkz.rpg.shared.adapters.events

import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import kotlin.reflect.KClass

class RecordingEventBus(
    private val delegate: EventBus,
) : EventBus {
    private val recorded = mutableListOf<DomainEvent>()

    val events: List<DomainEvent>
        get() = recorded.toList()

    override fun publish(events: Set<DomainEvent>) {
        recorded += events
        delegate.publish(events)
    }

    override fun publish(event: DomainEvent) {
        recorded += event
        delegate.publish(event)
    }

    override fun dispatch() {
        delegate.dispatch()
    }

    override fun <T : DomainEvent> subscribe(
        eventType: KClass<T>,
        handler: (T) -> Unit,
    ): Subscription = delegate.subscribe(eventType, handler)

    fun clear() {
        recorded.clear()
    }
}
