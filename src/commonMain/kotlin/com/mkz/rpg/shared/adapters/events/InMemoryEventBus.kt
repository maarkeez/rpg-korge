package com.mkz.rpg.shared.adapters.events

import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import kotlin.reflect.KClass

class InMemoryEventBus : EventBus {
    private val handlers =
        mutableMapOf<KClass<out DomainEvent>, MutableList<(DomainEvent) -> Unit>>()

    private val queue =
        ArrayDeque<DomainEvent>()

    private val lastDispatched =
        ArrayDeque<DomainEvent>()

    val queueDepth: Int
        get() = queue.size

    val lastEvents: List<DomainEvent>
        get() = lastDispatched.toList()

    override fun publish(events: Set<DomainEvent>) {
        events.forEach(::publish)
    }

    override fun publish(event: DomainEvent) {
        queue += event
        println("[EVENT] Queued: ${event::class.simpleName}")
    }

    override fun dispatch() {
        while (queue.isNotEmpty()) {
            val event = queue.removeFirst()

            handlers[event::class]
                ?.toList()
                ?.forEach { it(event) }

            lastDispatched += event
            if (lastDispatched.size > MAX_LAST_EVENTS) {
                lastDispatched.removeFirst()
            }

            println("[EVENT] Dispatched: ${event::class.simpleName}")
        }
    }

    override fun <T : DomainEvent> subscribe(
        eventType: KClass<T>,
        handler: (T) -> Unit,
    ): Subscription {
        val list =
            handlers.getOrPut(eventType) {
                mutableListOf()
            }

        val wrapper: (DomainEvent) -> Unit = {
            @Suppress("UNCHECKED_CAST")
            handler(it as T)
        }

        list += wrapper

        return Subscription {
            list -= wrapper
        }
    }

    private companion object {
        private const val MAX_LAST_EVENTS = 32
    }
}
