
# 005 - Cleaning Up My Idea Backlog

## Hook

One of the easiest ways to lose focus as a solo developer is to have too many ideas.

Ideas are useful, but during development they can also become distractions. A new mechanic, an architectural improvement, a quality-of-life feature, or an interesting technical experiment can all feel worth exploring when they appear. The problem is that not all of them are relevant to what I need to build right now.

At one point, my todo list had around 30–40 items covering everything from bugs and maintenance work to speculative mechanics. I was using it to avoid forgetting ideas, but I was also using it as an unofficial backlog. That made it surprisingly easy to spend time working on things that were not actually priorities.

I needed to clean up not just the list, but the way I thought about it.

## Context

Before writing code, I started the project with a User Story Map around the main journey I wanted for the core loop.

For the first milestone, the goal is relatively simple: build a working and interesting tactical battle. The player should be able to deploy units, move them around a 2D grid, think about positioning and paths, choose abilities to use against enemies or their own units, finish the turn, and ultimately win or lose the battle.

The story map helped me establish that as the immediate goal.

I then created an Event Storming diagram to understand the domain and the size of the prototype. This was particularly useful because the game is more backend-heavy than it might initially appear. I needed to think about things such as battlefield tiles, turns, abilities, units, and the CPU's decision-making, as well as how the boundaries between these concepts should work in an event-driven architecture.

From there, I started writing specifications for the individual use cases and implementing them.

The todo list appeared during this implementation work.

## Main Story

### The todo list was becoming a distraction

While coding, I kept a temporary todo list in the same way I might keep notes while pair programming: write something down so I don't forget it, but don't stop what I'm doing to investigate it.

The problem was that I wasn't always following the second part.

I would write down something like:

- A unit can walk through a void instead of going around it.
- Keep the selected unit after confirming an ability.
- Add architecture tests.
- Write more how-to guides.
- Build cross-platform tests.
- Investigate automated ability-balance testing.
- Maybe build tooling to automate Event Storming from YAML.

These are very different kinds of things.

Some are bugs. Some are quality-of-life improvements. Some are maintenance. Some are documentation. Others are potentially large future projects.

Previously, I would sometimes start evaluating these ideas before finishing the task I was working on. That was the real problem. The todo list was supposed to protect my focus, but it was becoming a source of distraction.

### Separating the queues

I now organize the items into different topics: **top prototype priorities, player quality-of-life improvements, bugs, maintenance, community work, and other areas**.

Each queue can then have its own priorities.

This creates a useful decision point when I finish something. If there is no higher-priority prototype work, I can ask what the project needs next: more prototyping, code consolidation, user-experience improvements, or something else.

This also makes it easier to recognize when an idea doesn't belong in the current milestone.

For example, a looting system or a quest campaign might be interesting mechanics for a future version of the game. But they don't help me answer the more immediate question: *is the core tactical battle actually working and fun?*

The User Story Map gives me a reference for that decision, while Event Storming gives me a better understanding of what the current prototype actually contains.

### Not every idea belongs in the backlog

Another important distinction I've made is between temporary thoughts and durable knowledge.

I use a Zettelkasten approach in Obsidian. A quick, insufficiently thought-through idea remains a **temporal note**. If I research it, understand it, and can explain it properly, I turn it into a more permanent note.

For project knowledge, those notes sometimes follow the Diátaxis framework: a tutorial, how-to guide, explanation, or reference.

This means that a todo item can evolve as I work.

For example, discovering that I need an architecture test might start as a todo item. While implementing it, I might discover a broader principle worth documenting, which becomes a Diátaxis document. That might lead to a change in the Event Storming model and eventually a change in the specification.

The todo list is therefore not the final destination for every idea.

### Cleaning up continuously

My current process is simple.

When I finish an item, I mark it as completed. At the end of the week, I remove completed items. More importantly, I now also look at what remains whenever I finish a task and remove things that are no longer relevant.

I judge relevance based mainly on the current milestone, the vision of the game, and the amount of time I expect to be able to invest as a solo developer.

This has also helped me recognize a particular category of dangerous idea: **good ideas that are too expensive right now**.

For example, I considered building a system to automatically measure how balanced a new unit ability is against existing abilities. That's an interesting problem, but building the infrastructure to support it could take more time than simply building the game.

I had a similar experience with trying to automate Event Storming using YAML. While researching the idea, I started thinking about the much larger problem of building an application that could replace a tool like Miro. Eventually I recognized that I had entered a rabbit hole and discarded the idea for this project.

The idea wasn't necessarily bad. It simply wasn't the project I was trying to build.

## Key Takeaways

- **A todo list should protect focus, not compete for it.**
- **The current milestone is a filter for ideas.** Interesting doesn't necessarily mean relevant now.
- **User Story Mapping defines what needs to exist for the next milestone.**
- **Event Storming helps understand the size, boundaries, and complexity of what I'm actually building.**
- **Temporal notes let me capture ideas without requiring me to investigate them immediately.** Zettelkasten and Diátaxis provide a path for turning the useful ones into durable knowledge.

## What's Next

The goal now is to keep the prototype moving without letting the growing complexity of the project pull me in every possible direction.

As the core battle becomes more complete, the interesting question will be what comes next: more prototyping, consolidating the code, improving the player experience, or addressing technical debt and bugs.

The important part is that I don't have to decide that every time an idea occurs to me.

I can write it down, keep working, and decide later.