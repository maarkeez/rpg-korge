package shared.adapters.events

import shared.domain.DomainEvent
import shared.domain.EventBus
import shared.domain.Subscription
import kotlin.reflect.KClass

class InMemoryEventBus : EventBus {
    private val handlers =
        mutableMapOf<KClass<out DomainEvent>, MutableList<(DomainEvent) -> Unit>>()

    private val queue =
        ArrayDeque<DomainEvent>()

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
}
