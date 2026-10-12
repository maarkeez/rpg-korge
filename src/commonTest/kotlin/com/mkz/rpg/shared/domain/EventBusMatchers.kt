package com.mkz.rpg.shared.domain

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly

fun FakeEventBus.shouldHavePublishedEvents(vararg expected: DomainEvent): FakeEventBus {
    publishedEvents.shouldContainExactly(*expected)
    return this
}

fun FakeEventBus.shouldNotHavePublishedEvents(): FakeEventBus {
    publishedEvents.shouldBeEmpty()
    return this
}
