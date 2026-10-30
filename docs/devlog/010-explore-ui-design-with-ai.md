---
date: 9th October 2026
commit: 63dc3d3a9168f49c19c8200cc47f2f624e52bb8e
---

# 010 — Explore UI Design with AI

## Hook (1 minute)

How do you improve the visual quality of a game without spending too much time experimenting with designs that might not work?

I decided to use AI as a design consultant to explore improvements to my game's UI and visual assets. The existing interface was functional, but the overall presentation lacked polish and a coherent visual direction. I wanted to explore what the game could look like before committing too much time to implementation.

The experiment led to an important realization: generating better-looking assets wasn't the first problem I needed to solve. I needed to define the design principles that those assets should follow.

## Context

The game is a tactical, turn-based fantasy RPG built around a grid battlefield. Players control multiple units, use abilities, and trigger chains of effects against enemies. Understanding the relationships between abilities, targets, and status effects is central to the experience.

The battlefield is more than a background. It's where players make tactical decisions, so the interface needs to communicate useful information without overwhelming the world.

Until now, however, some fundamental design decisions weren't sufficiently documented. I had ideas about the visual style and how the interface should work, but I lacked a clear reference that could guide future decisions consistently.

I wanted to use AI to help me examine the existing design, establish a direction, and prototype improvements quickly.

## Main Story — From Visual Exploration to a Design Brief

### A structured approach to AI-assisted design

Instead of asking AI to redesign everything in one step, I organized the work into a sequence of smaller tasks.

I started by reflecting on the existing design and exploring the tools available for the job. From there, I worked with an agent to develop a UI design brief, create a redesign plan, plan the implementation, and explore improvements to the game's assets.

This structure helped turn a broad and somewhat subjective goal — making the game look better — into a more concrete design exercise.

The most important deliverable was the **UI Design Brief for the Tactical Fantasy RPG**.

### Defining the design principles

The brief establishes a visual direction inspired by the charm of *The Legend of Zelda: A Link to the Past* and the vibrant colors of *Super Mario All-Stars: Super Mario Bros. 3*, using the Famicube palette as the color reference.

It also defines several design pillars: tactical clarity, world immersion, pixel-art authenticity, minimalism, and sustainability for a solo developer.

These principles matter because visual quality isn't just about making individual elements attractive. The interface, characters, icons, effects, and battlefield need to feel like parts of the same game.

The brief also documents how the interface should behave. Selecting a unit should reveal its movement range and available actions. Choosing an ability should make valid targets or locations clear. Before confirming a cast, the player should be able to understand which targets will be affected and what consequences to expect.

These are not merely visual preferences. They describe the information players need to make meaningful tactical decisions.

The brief further establishes practical constraints: portrait mobile play, touch-friendly interactions, consistent pixel-art scaling, reusable UI components, and a restrained approach to permanent interface elements.

Having these requirements in writing gives future design and implementation work a shared point of reference.

### AI accelerates exploration, but consistency remains a challenge

With a clearer direction established, I used the process to develop and implement a prototype.

AI was useful as a design consultant because it helped me explore possibilities without requiring every idea to be developed manually from scratch. I could use the resulting concepts to move from abstract design goals toward something more concrete.

However, the experiment also exposed limitations.

**Consistency across different outputs** was one challenge. Individual designs can look promising in isolation without necessarily sharing the same visual language. Without explicit principles, different outputs can pull the game in different directions.

Pixel-art quality and authenticity were another concern. A design that looks polished at first glance doesn't automatically respect the proportions, palette, and visual conventions that make pixel art feel cohesive.

These limitations reinforced the importance of treating AI-generated results as proposals rather than finished assets. The goal isn't simply to generate more content faster. It's to select and refine results that support the intended experience.

A design brief cannot guarantee that every output will be correct, but it provides criteria for evaluating the results and making more consistent decisions.

### The tradeoff: planning before production

Creating the brief and planning the redesign takes time that could otherwise be spent implementing features directly. But skipping this work risks accumulating inconsistent decisions that become harder to correct later.

For a solo developer, the challenge is to find the right balance. The design process needs enough structure to keep the game coherent without becoming an end in itself.

In this experiment, the brief was the most valuable outcome. The prototype demonstrated the direction in practice, while the documented principles provided a foundation for continuing the work.

## Key Takeaways

- **Define the design principles before generating assets.** A clear direction makes it easier to evaluate AI outputs and keep the game visually coherent.
- **Document the fundamentals.** Important requirements shouldn't exist only in the developer's head. A written brief helps guide future decisions.
- **Design for the actual gameplay.** In a tactical game, UI design must communicate movement, abilities, targets, and consequences, not just look attractive.
- **Treat AI as a design consultant, not an unquestioned authority.** It can accelerate exploration, but consistency and pixel-art quality still require judgment.
- **Use a structured workflow.** Breaking the task into reflection, briefing, planning, prototyping, and asset design makes a broad redesign more manageable.

## What's Next

The next milestone is to integrate the redesigned assets into the game.

The design brief will serve as a reference during that process, helping ensure that the new assets support the same visual direction and tactical priorities. The objective isn't simply to replace the old UI with something prettier. It's to build a more coherent interface that makes combat easier to understand while keeping the battlefield at the center of the experience.