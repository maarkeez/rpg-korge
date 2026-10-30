# Building the CPU Brain Early

## Hook

A tactical game can have a great battlefield, interesting abilities, and satisfying combat mechanics. But if the opponent behaves randomly, none of that matters very much.

That was exactly what happened during one of my early playtests.

The CPU wasn't trying to beat me. It was just executing random actions. After a couple of rounds, I realized I could brute-force the game by casting my strongest abilities in sequence. I wasn't making clever decisions anymore—I was simply taking advantage of the fact that the opponent wasn't making any decisions at all.

The prototype was becoming boring.

That was when I realized something that had been missing from my initial estimations: **the CPU brain was not optional if I wanted to know whether the game was actually fun.**

## Context

The first version of the prototype had a very simple battle system. Each player had a couple of units. My units were knights with six different abilities, while the CPU controlled rats with only two: attack and heal.

Abilities consumed mana, which was restored each turn, and the objective was straightforward: defeat all the opposing units.

The battlefield and most of the battle logic were already working. What was missing was an actual opponent.

Initially, the CPU did nothing. In the next iteration, it started doing random things. This was enough to test some of the battle mechanics, but not enough to test the actual game.

The goal of the prototype was to answer a simple question: **is the core concept fun enough to continue building?**

With a CPU that wasn't trying to win, I couldn't really answer that question.

A simple CPU could make every battle boring. But going too far in the other direction would create another problem: an opponent that was so effective that the game became impossible to balance.

I didn't want the CPU to outsmart the player. I wanted it to make **fun and believable decisions** that created enough challenge for the player to feel smart when they won.

## Main Story

Once I understood the problem, I decided not to postpone the CPU until later in development.

That decision was partly about gameplay and partly about architecture.

If I had continued developing the game without considering the CPU, I would eventually have had to go back through a much larger codebase and make all the existing systems usable by the CPU. Since the core mechanic of the game revolves around chaining abilities, this would eventually affect many parts of the code.

At this stage, the presentation layer could remain a prototype. But the actual game logic needed a more solid architecture and tests so I could iterate quickly without constantly breaking existing behavior.

I started investigating four different approaches:

- State machines
- Decision trees
- Utility systems
- Reinforcement learning

State machines and decision trees were attractive because they are relatively easy to understand and get working initially. The problem is that they become increasingly difficult to maintain and balance as the number of decisions grows. With a small number of options they are cheap, but adding more behaviors creates more branches and more things to rebalance.

They also tend to make behavior predictable when kept simple. A fixed sequence of decisions doesn't necessarily adapt naturally to what is happening in the current battle.

Reinforcement learning was probably the most tempting alternative for me personally. With my background in artificial intelligence, the idea of training an agent to play the game sounded great.

But there were practical problems. The tooling around Kotlin is not as mature for this kind of work as the ecosystem available in languages such as Python. I would also need to build the framework required to train the algorithm and fine-tune it.

That would increase the initial investment considerably. Since this is a solo project that I also plan to make open source, I had another concern: **how easy would it be for someone else to understand and contribute to the system?**

In the long term, reinforcement learning might produce interesting results, but it would introduce a workflow that not every contributor would be familiar with.

That left Utility AI.

The core idea is surprisingly simple: evaluate possible decisions and give them a utility score based on the current state of the battle.

The CPU has a command use case that orchestrates several smaller utility queries:

> Should I move, cast, or do nothing?

If it should cast:

> Should I attack or heal?

If it should attack:

> Which attack is the best option?

If it should heal:

> Which healing ability is the best option?

Each utility is relatively self-contained. The utilities query other game subdomains for the information they need, normalize that information, and then use a function to calculate the utility.

For example, deciding whether a unit should move toward an enemy or an ally currently depends on its health. A unit with high health gets a higher utility for moving toward the enemy, while a unit with low health gets a higher utility for moving toward an ally.

For now, I'm using simple linear functions because the prototype doesn't need anything more complicated. As the game grows, individual utilities can be rebalanced or use different functions such as logarithmic or trigonometric curves.

That modularity is what made Utility AI particularly attractive.

Adding a new behavior doesn't necessarily mean redesigning the entire decision system. I can create another small building block, tune it independently, and let it react to the current battle state.

The first version already produced a much better battle. Rats now try to attack when they have enough health and move toward the player. When they are low on health, they try to retreat and heal.

It's still far from finished. For example, if I hit them particularly hard, they can sometimes just stand there instead of reacting appropriately. The utilities will need more balancing as the game becomes more complex.

But that's exactly what I wanted from this first iteration.

I wanted to prove that **even a simple CPU behavior could be fun to play against**, while also establishing an architecture that could grow with the game.

## Key Takeaways

- The CPU is part of the gameplay, not just a feature added after the prototype.
- Playtesting exposed a problem that the implementation itself hadn't revealed: random behavior made the core combat loop impossible to evaluate properly.
- Utility AI provides small, understandable building blocks that can react to the current battle state.
- Keeping each utility independent makes balancing and adding new behaviors faster.
- Building with the CPU in mind changes how I approach every new player-facing mechanic.

## What's Next

The current CPU is intentionally simple. Future enemies will need different behaviors: melee units that aggressively approach the player, mages that try to stay at range, and summoners that may prioritize positioning over direct attacks.

Beyond that, the AI will eventually need to understand things like fog of war, visibility ranges, pathfinding, terrain effects, battlefield objects, loot, and objectives beyond simply killing every enemy.

The important change is that I don't want to postpone those problems until the end.

**From now on, every new feature I build for the human player should also make me think about its CPU counterpart.**

Following an agile approach focused on improving development workflows can feel tedious at the beginning. But this iteration already showed me that the investment starts paying off surprisingly early.