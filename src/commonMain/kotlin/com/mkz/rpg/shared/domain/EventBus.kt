package com.mkz.rpg.shared.domain

import kotlin.reflect.KClass

interface EventBus {
    fun publish(events: Set<DomainEvent>)

    fun publish(event: DomainEvent)

    fun dispatch()

    fun <T : DomainEvent> subscribe(
        eventType: KClass<T>,
        handler: (T) -> Unit,
    ): Subscription
}

inline fun <reified T : DomainEvent> EventBus.subscribe(noinline handler: (T) -> Unit): Subscription = subscribe(T::class, handler)
