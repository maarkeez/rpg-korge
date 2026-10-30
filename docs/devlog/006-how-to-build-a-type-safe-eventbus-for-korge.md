# 006 How to Build a Type-Safe EventBus for KorGE

## Hook

I started designing an EventBus because I wanted the game's architecture to be event-driven.

The idea was straightforward: gameplay systems should be able to communicate without knowing about each other. Combat shouldn't need to know about Quests. Quests shouldn't need to know about UI. The battle system shouldn't need to know who is interested in the fact that a unit was defeated.

My first implementation worked, but it was also a good example of designing something more generic than I actually needed.

It subscribed every listener to every event, required listeners to decide whether they cared about an event, and immediately dispatched events when they were published.

After implementing more of the game, I replaced it with something smaller: a type-safe domain interface backed by an in-memory implementation with queued dispatch.

That change ended up being less about building an EventBus and more about discovering what the game actually needed.

## Context

The EventBus is part of the core architecture of the game.

I'm building the game around independent vertical slices and want to keep ownership separated. A battle system should be replaceable without forcing changes into unrelated systems. The same applies to inventory, quests, UI, and other parts of the game.

Domain events provide a useful boundary:

```text
BattleUnit
   │
   ▼
BattleUnitDefeated
   │
   ├──► Battle
   └──► Battlefield
```

BattleUnit only knows that it publishes a domain event. It doesn't know who consumes it.

This fits naturally with Clean Architecture: the domain defines what it needs, while the concrete EventBus implementation lives outside the domain.

## Main Story

### The first implementation was too generic

My first EventBus followed a fairly common approach.

There was one collection of listeners, and every listener received every event:

```text
publish(event)
      │
      ▼
 iterate all listeners
      │
      ├── "Is this my event?"
      ├── "Is this my event?"
      ├── "Is this my event?"
      └── ...
```

This meant every listener had to perform its own filtering.

It also meant that publishing an event effectively meant dispatching it immediately.

That seemed convenient at first, but it created two problems.

First, the filtering was unnecessary work. Every event could result in a large number of listeners being invoked just so they could determine that the event wasn't relevant to them.

Second, everything happened in the same frame. If one event generated another event, which generated another event, the entire chain could execute immediately.

For a tactical game where one action can trigger many consequences, that can become expensive and difficult to reason about.

So I changed the design.

### The domain only knows the EventBus contract

Instead of putting the implementation in the domain, I defined the smallest interface the domain actually needs:

```kotlin
// shared/domain/DomainEvent.kt
interface DomainEvent

// shared/domain/Subscription.kt
class Subscription(
    private val disposeAction: () -> Unit,
) {
    fun dispose() {
        disposeAction()
    }
}

// shared/domain/EventBus.kt
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
```

The actual implementation is an adapter:

```kotlin
// shared/adapters/events/InMemoryEventBus.kt
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
```

This separation is important to me.

The domain doesn't care whether events are stored in memory, logged, replayed, sent somewhere else, or handled by a different implementation in the future.

It only knows that it can publish events and subscribe to them.

### Type-safe subscriptions

One of the biggest changes was making subscriptions specific to an event type.

Instead of subscribing to everything:

```kotlin
eventBus.subscribe {
    if (it is BattleUnitDefeated) {
        // ...
    }
}
```

the caller can say exactly what it wants:

```kotlin
eventBus.subscribe<BattleUnitDefeated> { event ->
    defeatPlayer(event.playerId)
}
```

The important part here is Kotlin's `reified` type parameter:

```kotlin
inline fun <reified T : DomainEvent> subscribe(
    noinline handler: (T) -> Unit
)
```

Because `T` is reified, the EventBus can obtain the runtime class:

```kotlin
T::class
```

and store handlers by event type.

Conceptually, the internal structure becomes:

```text
BattleUnitDefeated::class
    ├── Battlefield listener
    ├── Battle listener
    └── BattleUnit presentation listener

PlayerDefeated::class
    ├── …
    └── …
```

This eliminates the unnecessary filtering from the first implementation.

The unchecked cast still exists internally when converting the generic handler into the common representation used by the map, but callers interact with a typed API. The important difference is that the type decision happens when subscribing rather than forcing every listener to inspect every event.

### Publishing doesn't mean dispatching

The second major change was separating these two operations.

`publish()` puts an event into the queue.

`dispatch()` processes the queue.

That distinction lets the main game screen control when domain events are processed.

Conceptually:

```text
User action
    │
    ▼
Gameplay
    │
    ▼
publish(event)
    │
    ▼
Event queue
    │
    │ next frame
    ▼
dispatch()
    │
    ├──► Quest
    ├──► Battle
    ├──► UI
    └──► ...
```

The main screen has an updater that invokes `dispatch()` every frame.

This gives the game a predictable boundary between producing events and processing them.

It also makes testing easier. A test can publish events and decide explicitly when to dispatch them instead of having publishing immediately trigger the entire downstream system.

### One action can produce multiple events

Another detail that became important is that a domain operation doesn't necessarily result in a single event.

Consider a battle unit being defeated.

The battle system can react to that fact and evaluate the entire battle state. Depending on the result, several things might have happened:

```text
BattleUnitDefeated
       │
       ▼
 evaluate battle state
       │
       ├──► PlayerTurnStarted
       ├──► PlayerDefeated
       └──► BattleFinished
```

Not all of these will necessarily happen every time, but the domain may discover multiple facts as a consequence of one operation.

That's why the EventBus supports publishing a set of events:

```kotlin
eventBus.publish(
    setOf(
        PlayerTurnStarted(...),
        PlayerDefeated(...),
        BattleFinished(...)
    )
)
```

The important thing is that the domain can describe the result without coupling itself to the order or implementation of downstream consumers.

Events are facts about what happened. The EventBus is responsible for delivering those facts.

## Key Takeaways

- **Start with the smallest abstraction the game actually needs.** My first design was more generic than necessary.
- **Type subscriptions at the API boundary.** `subscribe<T>()` avoids forcing every listener to inspect every event.
- **Separate publishing from dispatching.** Queuing events creates a clear boundary between producing domain facts and processing their consequences.
- **Let the domain produce multiple events.** A single gameplay operation can reveal several independent domain facts.
- **Keep the implementation outside the domain.** The domain defines the EventBus contract; the in-memory implementation is an adapter.

## What's Next

There are still things I haven't solved.

In particular, subscription lifecycle and disposal need more exploration. I don't want to pretend the current implementation has solved scene lifecycle management just because I know subscriptions eventually need to be disposed.

There are also ideas that I deliberately left for later: event logging, metrics, replay, and other debugging or infrastructure capabilities.

Those might become useful once the game is a more solid product.

For now, I'm happy with something much smaller.

I started by designing a generic EventBus, but implementing the actual game showed me that I needed a much smaller, explicitly typed boundary between my domain systems.