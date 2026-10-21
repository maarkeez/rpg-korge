# Plan: Npc and party dependency cycle

> **Status:** done.
>
> **Summary:** `npc` and `party` depended on each other. `party` no longer depends on `npc`: talking to the npc next to
> the leader moves to `npc`, whose `TalkToAdjacentNpc` finds it with its own `SearchNpcNextTo` query and publishes the
> new `RequestStartTalk`, which `party` listens to and starts the talk with. `npc` keeps publishing `RequestJoinParty`
> and listening to `NpcTalkFinished`.

## Decisions

| Topic       | Decision                                                                                                                                                                       |
|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope       | Only `npc` ⇄ `party`. `battle` ⇄ `cpuBattleBrain` and `cpuBattleBrain` ⇄ `cpuWanderBrain` are left for later.                                                                  |
| Direction   | `party` is upstream: it does not know about `npc`. `npc` keeps publishing `RequestJoinParty`, and its `OnNpcTalkFinished` listener.                                            |
| Lookup      | `npc`'s new `TalkToAdjacentNpc` reads the leader with `SearchParty`, its position with `SearchPosition`, and the npc next to it with `SearchNpcNextTo`. The screen calls it.   |
| Request     | `party` declares `RequestStartTalk(npcId)`, listened by `OnRequestStartTalk`, calling the new `StartTalk`, as `FinishTalk` does. `party`'s `TalkToAdjacentNpc` is deleted.     |
| Guard       | `party.talk` stays the only "can walk" guard: `npc` requests the talk whenever an npc is next to the leader, and `party` ignores it while walking or battling.                |
| Directions  | `npc` keeps its own north, east, south and west offsets, in `SearchNpcNextTo`, instead of reusing `party`'s `DirectionDto`. `SearchNpcAt` is deleted: nothing else uses it.   |
| Event order | The bus drains its queue in one `dispatch`, so `NpcTalkStarted` still comes out in the same frame as before.                                                                  |
| Wiring      | `PartyApi` no longer takes `NpcApi`; `NpcApi` takes `PartyApi`, so it is built after it.                                                                                       |
| Guard test  | The pair is removed from `SubdomainDependencyArchitectureTest`.                                                                                                                |
| Docs        | `package-structure.md` has the party example, next to the inventory one. `exploration.md` says `npc` requests the talk. The dependency report is regenerated.                 |
| Delivery    | One commit, with this plan, pushed to `main`.                                                                                                                                  |

| What `party` needed from `npc`  | Now                                                                                     |
|---------------------------------|-----------------------------------------------------------------------------------------|
| `SearchNpcAt`, to talk to an npc | Listening to its own `RequestStartTalk`, published by `npc`'s `TalkToAdjacentNpc`.     |

## party

- Add `PartyEvent.RequestStartTalk`, `OnRequestStartTalk` and `StartTalk`. Delete `TalkToAdjacentNpc`.
- `PartyApi` no longer takes `NpcApi`.

## npc

- Add `TalkToAdjacentNpc` and `SearchNpcNextTo`. Delete `SearchNpcAt`.
- `NpcApi` takes `PartyApi`.

## screen

- `ExplorationPresenter` calls `npcApi.talkToAdjacentNpc()`. `BattleScene` builds `PartyApi` before `NpcApi`.

## Tests

- `TalkToAdjacentNpcTest` moves to `npc` and expects `RequestStartTalk`. Its walking and battling cases move to `StartTalkTest`.
- `SearchNpcNextToTest` replaces `SearchNpcAtTest`. `OnRequestStartTalkTest` is added.
- The acceptance and presenter tests build `PartyApi` before `NpcApi`, and talk through `npcApi`.
