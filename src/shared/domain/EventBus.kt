package shared.domain

import kotlin.reflect.KClass

class EventBus {

    val handlers =
        mutableMapOf<KClass<out DomainEvent>, MutableList<(DomainEvent) -> Unit>>()

    private val queue =
        ArrayDeque<DomainEvent>()

    fun publish(events: Set<DomainEvent>) {
        events.forEach(::publish)
    }

    fun publish(event: DomainEvent) {
        queue += event
        println("[EVENT] Queued: ${event::class.simpleName}")
    }

    fun dispatch() {

        while (queue.isNotEmpty()) {

            val event = queue.removeFirst()

            handlers[event::class]
                ?.toList()
                ?.forEach { it(event) }

            println("[EVENT] Dispatched: ${event::class.simpleName}")
        }
    }

    inline fun <reified T : DomainEvent> subscribe(
        noinline handler: (T) -> Unit
    ): Subscription {

        val list = handlers.getOrPut(T::class) {
            mutableListOf()
        }

        val wrapper: (DomainEvent) -> Unit = {
            handler(it as T)
        }

        list += wrapper

        return Subscription {
            list -= wrapper
        }
    }
}
