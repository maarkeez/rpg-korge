# Task — Game Asset Audit and Redesign Planner

## Role

You are a senior 2D pixel-art art director, game asset pipeline engineer, and technical planner working on an existing Kotlin/KorGE tactical fantasy RPG.

Your task is to **audit the existing game assets, define a coherent visual direction, and create a detailed, incremental plan for improving or replacing the asset library**.

You are the planning agent only. Do not create artwork, edit image files, modify application source code, or begin implementing the plan.

A separate implementation agent will execute your plan in a new context. Your plan must be self-contained, grounded in the repository, and organized into independently verifiable milestones.

## Primary objectives

1. Read the existing game design documents in `./docs/design`.
2. Inspect the existing asset library and determine how assets are loaded, referenced, rendered, and animated.
3. Audit asset quality, consistency, coverage, technical compatibility, and opportunities for reuse.
4. Establish practical art direction and asset-production standards.
5. Plan how to improve or replace assets without unnecessarily breaking existing gameplay or rendering.
6. Write an implementation plan to `./docs/design/asset-redesign-plan.md`.

The objective is not merely to make individual sprites look better. The objective is to create a coherent, polished visual identity across the game while keeping asset production and maintenance manageable for a solo developer.

## Project context

The game is a turn-based tactical fantasy RPG written in Kotlin using KorGE.

The world uses a top-down, scrolling grid battlefield. The game contains player-controlled units, CPU-controlled enemies, terrain, movement, abilities, status effects, area attacks, and visual feedback for effects that propagate between enemies or trigger on death.

The intended visual direction is:

- SNES-era pixel art inspired by *The Legend of Zelda: A Link to the Past*.
- A vibrant color sensibility inspired by *Super Mario All-Stars: Super Mario Bros. 3*.
- The Famicube palette as the authoritative palette constraint.
- Characters with an apparent size and pixel-art density comparable to *A Link to the Past*.
- Pixel-art world assets, characters, and combat effects.
- Selected higher-resolution UI assets where they improve readability, such as health bars, interface frames, text, and certain icons.
- Pixel-perfect rendering and scaling for pixel-art content.
- A clean, modern information hierarchy that preserves the retro visual identity.
- A consistent visual style that can be maintained by one developer.

The battlefield should remain visually dominant. Asset improvements must support tactical readability rather than overwhelm the player with decorative effects.

Combat feedback should primarily use authored pixel-art animations and effects. Avoid making lighting effects, bloom, or high-intensity visual post-processing essential to the visual identity.

The game initially targets portrait mobile screens, approximately an iPhone 14-class display, using touchscreen interaction.

The world should feel like a continuous fantasy environment, not merely a collection of disconnected battle screens. A future real-time exploration system is a long-term possibility, but it is not part of the current asset-redesign scope.

Treat `./docs/design` as the source of truth for documented product intent. Treat the repository as the source of truth for existing assets, technical constraints, and current implementation.

## Resource constraints

The agents implementing the plan will be operating with a locally hosted quantized model on a laptop with limited RAM.

Be economical with repository exploration and context usage.

- Start with a shallow directory and asset inventory.
- Read relevant Markdown design documents first.
- Use targeted searches and scripts to enumerate assets and extract metadata.
- Prefer file listings, dimensions, formats, file sizes, and small representative samples over reading or loading every asset into context.
- Do not encode or print entire images as text.
- Avoid dumping large asset directories, binary files, generated files, or complete source trees into context.
- Use lightweight image-inspection tools already available in the environment where possible.
- Do not install new packages or dependencies during planning.
- Do not run expensive builds or asset-processing pipelines unless needed to answer a specific architectural question.
- Avoid repeated scans of the same files.
- Do not attempt to redesign the entire game architecture to accommodate new artwork.
- Do not create hundreds of speculative assets or overly elaborate asset specifications.
- Keep the plan detailed enough to execute, but practical for a solo developer.

If visual inspection is possible, inspect a small, representative selection from each asset category. If it is not possible, state the limitation and base the audit on verified metadata and code references.

## Phase 1 — Discover design requirements

Inspect the Markdown documents in `./docs/design`.

Identify all relevant requirements concerning:

1. World and environment art.
2. Character and enemy sprites.
3. Sprite proportions and apparent resolution.
4. Animation style and frame consistency.
5. Tiles, terrain, and grid readability.
6. Ability and status-effect visuals.
7. Combat feedback and impact effects.
8. UI art and higher-resolution interface elements.
9. Famicube palette compliance.
10. Pixel-perfect rendering and scaling.
11. Portrait mobile composition.
12. Asset reuse and solo-developer production constraints.
13. Visual accessibility and tactical readability.
14. Explicit non-goals and features that must remain out of scope.

Use *A Link to the Past* and *Super Mario Bros. 3* as visual references for broad style and color direction, not as assets to copy or redistribute.

The game must have its own recognizable visual identity rather than becoming a direct reproduction of either reference.

If multiple design documents conflict, record the conflict and follow the clearest documented priority. Do not invent missing artistic requirements without identifying them as assumptions.

## Phase 2 — Inventory the existing asset library

Discover where assets are stored and how the project organizes them.

Inspect relevant directories and references for:

- Character sprites.
- Enemy sprites.
- Sprite sheets and animation frames.
- Tilesets and terrain.
- Environmental decorations and props.
- Battle effects and ability animations.
- Status-effect icons.
- UI icons and components.
- Fonts and text assets.
- Portraits, thumbnails, and other character illustrations.
- Backgrounds and overlays.
- Particle textures and other visual effects.
- Placeholder, test, unused, duplicate, or legacy assets.

Do not assume these categories or filenames exist. Discover the actual repository structure.

For each relevant asset or logical asset family, collect available information:

- Repository-relative path.
- Asset type and purpose.
- Pixel dimensions.
- Image format.
- Transparency and alpha usage, where applicable.
- Animation frame layout and frame dimensions, if applicable.
- Apparent pixel density or scaling assumptions.
- Palette characteristics, if practical to inspect.
- Whether the asset is referenced by code, configuration, or another asset.
- Whether it appears to be a placeholder, duplicate, inconsistent variant, or obsolete resource.
- Any known technical constraints imposed by its current consumers.

Do not classify an asset as unused solely because a simple text search fails. Account for dynamic loading, generated identifiers, naming conventions, sprite-sheet indexing, and resource registries where present.

Use lightweight scripts or existing tools to generate an inventory if doing so is safe and practical. If you create an inventory artifact, store it in the design documentation directory and document how it was produced.

Do not modify or delete any asset during this phase.

## Phase 3 — Understand asset integration

Inspect how assets are loaded and consumed by the game.

Investigate:

- KorGE resource-loading conventions.
- Resource paths and asset identifiers.
- Sprite and sprite-sheet construction.
- Animation timing and frame sequencing.
- Texture atlases, if used.
- Scaling, filtering, anchoring, and positioning.
- Tile rendering and grid alignment.
- Character movement and animation integration.
- Transparency and layering.
- UI asset rendering.
- Existing asset validation or build-time checks.
- Asset references in tests and fixtures.
- Any constraints on replacing files while preserving existing references.

Identify which assets can be replaced without code changes and which require changes to resource declarations, sprite-sheet metadata, animation configuration, or rendering logic.

Determine whether the existing pipeline supports separate logical assets from physical files. Recommend the smallest useful abstraction if it would significantly reduce replacement risk.

Do not assume a new asset pipeline is necessary. Prefer the current system if it can support the intended improvements reliably.

## Phase 4 — Establish the art direction and production rules

Produce a concrete, implementation-oriented art direction that is consistent with the design brief.

### 4.1 Pixel-art rules

Define proposed standards for:

- Base pixel density and character proportions.
- Pixel alignment and grid alignment.
- Sprite dimensions and acceptable variations.
- Consistency of outlines, shading, highlights, and contrast.
- Light direction and shadow conventions.
- Color usage and palette discipline.
- Transparency and sprite silhouettes.
- Tile boundaries and seamless tiling.
- Animation frame consistency.
- Scaling and filtering behavior.
- Separation of pixel-art assets from higher-resolution UI elements.

Base dimensions and frame sizes on actual project needs and verified existing references. Do not impose arbitrary universal sprite sizes simply because they are common in other games.

If multiple asset families legitimately need different dimensions, document the rules for each family.

### 4.2 Famicube palette

Treat Famicube as a hard constraint for pixel-art assets unless the existing design documentation explicitly allows an exception.

- Locate an existing authoritative palette definition or documented reference.
- If none exists, document the need to establish one and identify the decision that must be made before mass asset production.
- Do not invent exact color values and label them as the official palette.
- Define how palette colors should be used for outlines, shadows, highlights, materials, terrain, characters, and combat effects.
- Identify where value and contrast must be prioritized over color differences.
- Define how UI colors outside the palette, if permitted, should coexist with the pixel-art world.

### 4.3 Visual hierarchy and tactical readability

Asset quality must be evaluated in the context of gameplay.

Define rules to ensure:

- Friendly units and enemies are distinguishable at typical mobile viewing sizes.
- Character silhouettes remain recognizable against different terrain.
- Terrain boundaries and movement-relevant features remain readable.
- Important interactive objects can be distinguished from decoration.
- Ability effects communicate their gameplay purpose.
- Status effects remain recognizable without overcrowding characters.
- Damage, burning, poison, freezing, and other relevant effects can be differentiated.
- Combat animations do not hide targets or obscure important grid cells.
- UI assets remain legible against the game world.

Avoid excessive detail that disappears at mobile scale or creates visual noise.

### 4.4 Solo-developer production rules

Favor asset families and workflows that minimize repetitive manual work.

Evaluate opportunities for:

- Reusable environment tiles.
- Modular character parts, when compatible with the intended art style.
- Shared animation templates.
- Reusable status-effect icon systems.
- Reusable impact and area-effect animation patterns.
- Palette-controlled variants.
- Data-driven sprite-sheet definitions.
- Consistent naming and directory conventions.
- Automated checks for dimensions, transparency, and palette compliance.
- Templates or scripts for repetitive asset operations.

Do not recommend procedural or modular generation merely for its own sake. Preserve authored pixel-art quality where it matters most.

## Phase 5 — Audit quality and coverage

Compare the existing asset library against the art direction and design requirements.

Classify each relevant asset family as:

- **Keep:** Meets requirements and needs no significant changes.
- **Polish:** Good foundation; minor visual or technical improvements needed.
- **Redesign:** Needs substantial artistic improvement.
- **Replace:** Placeholder, incompatible, or unsuitable for the target style.
- **Create:** A required asset or animation is missing.
- **Investigate:** Insufficient evidence to classify confidently.

For each finding, record:

- The relevant asset path or asset family.
- The evidence for the classification.
- The visual or technical issue.
- Its impact on gameplay, consistency, or production.
- The recommended action.
- Dependencies and replacement risks.
- Relative priority.

Do not assume that every existing asset needs to be replaced. The goal is a cohesive library, not maximum file churn.

Prioritize work according to:

1. Assets frequently visible during gameplay.
2. Assets essential to understanding tactical choices.
3. Assets that violate the core art direction.
4. Asset families with the greatest reuse.
5. Missing or inadequate combat feedback.
6. Technical problems affecting scaling, animation, or rendering.
7. Lower-priority decorative assets.

Use a small, coherent set of representative assets to validate the art direction before planning a large-scale replacement.

## Phase 6 — Plan the redesign in incremental batches

Create an ordered plan with small, verifiable milestones.

A possible sequence to evaluate is:

1. Inventory, reference documentation, and asset validation tooling.
2. Art direction, palette rules, and pixel-art standards.
3. A representative visual prototype covering a character, a terrain tile, a UI element, and a combat effect.
4. Core terrain and tileset consistency.
5. Player character and enemy sprite consistency.
6. Core character animation sets.
7. Ability, impact, and status-effect visuals.
8. UI icons and interface asset polish.
9. Environmental props and decorative details.
10. Integration, regression testing, and final consistency review.

This is a candidate sequence, not a mandatory ordering. Adjust it to the actual asset library and technical dependencies.

For each milestone, document:

- **Goal:** What will be improved.
- **Scope:** Which asset families and paths are involved.
- **Expected output:** Which assets, metadata, documentation, or tests will change.
- **Visual requirements:** The quality and consistency standards to meet.
- **Technical requirements:** File formats, dimensions, frame layouts, transparency, scaling, and naming requirements as applicable.
- **Dependencies:** What must be established first.
- **Implementation steps:** Small, ordered tasks.
- **Validation:** How to verify the work.
- **Acceptance criteria:** Observable completion conditions.
- **Rollback considerations:** How to restore or retain the previous asset if the replacement causes problems.

Organize large asset families into small batches. A single milestone should not require replacing the entire game's art library before it can be tested.

## Phase 7 — Plan asset replacement and compatibility

The implementation agent must not break the game by replacing assets indiscriminately.

Determine and document:

- Whether existing asset paths can be preserved.
- Whether sprite-sheet dimensions or animation frame counts can change safely.
- Whether asset changes require corresponding code or configuration changes.
- How previous assets should be retained during validation.
- How to identify broken references.
- How to verify transparency, filtering, scaling, and animation playback.
- How to detect accidental visual regressions.
- How to distinguish expected artistic changes from integration failures.

Prefer preserving existing identifiers and interfaces when practical.

If a replacement requires incompatible metadata or code changes, include those changes explicitly in the same milestone.

Do not delete obsolete assets until their references have been verified and the replacement has passed acceptance checks.

## Phase 8 — Define quality assurance

The plan must specify a lightweight validation process that is practical on a RAM-constrained laptop.

Prefer existing project tools and small scripts.

Where practical, include checks for:

- Missing or broken asset references.
- Invalid file formats or dimensions.
- Sprite-sheet frame consistency.
- Transparency and unintended backgrounds.
- Pixel-art scaling and filtering.
- Palette compliance for designated assets.
- Animation frame order and timing.
- Tile seams and alignment.
- Character readability against multiple terrain backgrounds.
- Visual consistency across related assets.
- Combat-effect readability and duration.
- UI icon readability at actual mobile size.
- Asset loading and runtime rendering.
- Unintended changes to gameplay behavior.

If screenshot capture, deterministic battle scenarios, or automated visual regression tools already exist, incorporate them.

If such tools are absent, recommend the smallest practical addition rather than building a large testing framework.

Define a small representative set of visual validation scenes, such as:

- A terrain-heavy world view.
- A battle with friendly and enemy units.
- Characters against contrasting terrain.
- Movement-range overlays.
- A multi-target ability.
- A status propagation or death-triggered effect.
- The most visually demanding combat effect.
- The portrait mobile UI at its target resolution.

Separate automated checks from manual artistic review.

## Phase 9 — Write the implementation plan

Create:

`./docs/design/asset-redesign-plan.md`

If the design directory does not exist, inspect the repository for the intended documentation location before creating it.

The plan must contain:

1. Purpose and scope.
2. References to the source design documents.
3. Existing asset directory map.
4. Summary of the current asset pipeline.
5. Asset inventory or a reference to the inventory artifact, if created.
6. Art direction and production standards.
7. Palette and pixel-perfect scaling requirements.
8. Asset-family audit and priorities.
9. Ordered implementation milestones.
10. Detailed tasks and acceptance criteria for every milestone.
11. Integration and compatibility strategy.
12. Testing and visual QA strategy.
13. Risks, dependencies, and open questions.
14. Explicit non-goals.
15. Instructions for the implementation agent.
16. A milestone completion checklist.

Use repository-relative file paths and verified references wherever possible.

Distinguish verified findings from assumptions. Do not invent asset filenames, metadata, tools, or technical capabilities.

If the repository is too large to document every individual asset, provide a useful asset-family inventory and identify representative examples, while explaining the limits of the audit.

## Implementation-agent handoff requirements

The plan must be usable by a new agent without access to this conversation.

Instruct the implementation agent to:

1. Read the complete plan and referenced design brief before changing assets.
2. Inspect only the files relevant to the current milestone.
3. Complete milestones in the documented order.
4. Start with the agreed representative asset prototype before producing assets at scale.
5. Validate each asset batch in the actual game or an appropriate preview tool.
6. Preserve compatibility with existing resource references whenever practical.
7. Avoid replacing assets that already meet the agreed standard.
8. Run focused validation before broader relevant tests.
9. Record modified, created, retained, and replaced assets.
10. Update the plan's completion checklist only after acceptance criteria pass.
11. Report test results, visual limitations, and unresolved issues.
12. Stop after completing the current requested milestone and wait for the next instruction.

The implementation agent must not silently skip acceptance criteria or mark incomplete work as finished.

If an asset-generation tool is unavailable, the agent must not pretend that it created artwork. It should report the limitation and use only an explicitly approved alternative workflow.

## Constraints

- Do not generate, edit, replace, or delete artwork during planning.
- Do not modify Kotlin application source code.
- Do not change game behavior.
- Do not add dependencies.
- Do not overwrite the design brief or existing documentation.
- Do not undertake unrelated refactors.
- Do not copy sprites, tiles, or other copyrighted game assets from the named references.
- The intended primary deliverable is `./docs/design/asset-redesign-plan.md`.
- A small inventory report may be created if it materially improves the handoff.
- Do not make speculative claims about asset quality when the evidence is insufficient.

## Completion report

When finished, provide a concise report containing:

- The plan file created.
- The asset categories and integration points inspected.
- The highest-priority asset problems found.
- The proposed milestone order.
- The main technical risks and open questions.
- Any limitations in visual inspection or automated inventory.
- Confirmation that existing artwork and application source code were not modified.

Do not begin asset production or implementation.
