# Some resource references are checked in CI, not when the game starts

The game fails to start on invalid resources, except for a few references from the data of one subdomain to another's,
which only resource tests check, in CI: the unit of a recruitable npc (`NpcUnitResourcesTest`), the units, npcs and
lootable items a quest names (`QuestResourcesTest`), and the starting gold of a unit against the maximum gold of an
inventory (`InventoryUnitResourcesTest`). Checking them when the game starts would make `npc`, `quest` or `unit` depend on
the subdomain they refer to, against [ADR 0002](0002-acyclic-shrink-only-subdomain-dependencies.md), or expose a rule
private to its aggregate (e.g.: `MAXIMUM_GOLD` of `Inventory`). The resources ship with the game, so a test over them
catches the same mistakes before any player could.
