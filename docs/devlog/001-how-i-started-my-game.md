# How I Started My Game

## Hook

Have you ever had a game idea but never knew where to start?

That was me. Instead of waiting for the "perfect" idea or learning a huge game engine first, I treated game development like any other software project: start small, iterate quickly, and learn along the way.

This is how I started building my first game.

## Context

A bit of background first.

I'm a software engineer, so I'm used to working in short, iterative milestones that let me gather feedback as early as possible. Naturally, I wanted to approach game development the same way.

I've always loved turn-based strategy and RPG games. As a kid, I spent countless hours playing Pokémon, Final Fantasy, and Advance Wars on the Game Boy. More recently, games like *Into the Breach* reminded me why I enjoy this genre so much: tactical gameplay, meaningful decisions, and the freedom to play at my own pace without needing an internet connection.

Another big source of inspiration has been LitRPG novels. Reading *He Who Fights With Monsters* and *Dungeon Crawler Carl* brought back that childhood excitement and made me want to build a game of my own.

This is purely a hobby project. I'm not trying to make money from it. Instead, I'd like to build an open-source game that people can contribute to and hopefully grow into a community project for fans of turn-based RPGs.

Over the past few years I've read books about game development, listened to podcasts, and followed many developers on YouTube and Twitch. I know this project will take years of work, but I have one simple goal:

If, one day, a few strangers on the internet play my game and genuinely have fun with it, I'll consider it a success.

## Main Story

Although I'm developing this game alone, I still wanted to follow processes that have worked well for me in professional software development.

Many game development resources recommend starting with a large Game Design Document. Instead, I decided to use an iterative workflow:

- Ideate
- Design
- Implement
- Gather feedback
- Repeat

If you're familiar with Agile development, this should sound familiar.

### Step 1: Build a prototype

My first goal wasn't to build an entire game.

Instead, I wanted to validate the core gameplay loop.

There's no point writing a story or creating lots of content if the combat itself isn't fun.

### Step 2: Define the scope

Once I knew what I wanted to validate, I created a User Story Map using sticky notes.

The first prototype only needed a few milestones:

- Play a complete battle
- Units with different abilities
- Terrain with gameplay effects
- A simple but functional UI

Keeping the scope intentionally small helped me stay focused.

### Step 3: Design before coding

Before writing any code, I spent time understanding the problem.

I created:

- an Event Storming diagram
- a ubiquitous language document
- rough UI sketches

This gave me several advantages:

- I understood the project's scope before implementing anything.
- I identified uncertain areas early.
- I explored multiple solutions before committing to one.
- I could write much more detailed feature specifications.

### Step 4: Prototype with AI

Once the features were written in Gherkin, I used AI to quickly generate a prototype.

The goal wasn't to create production-ready code—it was to validate the design.

It immediately paid off.

After playing the prototype, I realized I'd forgotten to define two essential features:

- Battle Victory
- Game Over

It also inspired several UI improvements that I hadn't considered during the design phase.

### Step 5: Choosing the technology

The AI prototype was built with React and TypeScript.

While it confirmed that the design worked, it also confirmed something else: I'd rather write the real game in Kotlin.

Since this is a hobby project and my free time is limited, I want to spend it building gameplay—not learning an entirely new language or framework.

That left me with two options:

- Use a game engine (Unity, Unreal, or Godot).
- Use a Kotlin game framework (LibGDX or KorGE).

I chose KorGE.

Using a Kotlin framework has several advantages for my goals:

- I can stay within the JVM ecosystem that I'm already comfortable with.
- It's easier for contributors to clone, build, and review an open-source project.
- KorGE has fewer static dependencies than LibGDX, making automated testing easier.

This matters because most of my game's complexity lives in the battle logic rather than graphics.

Good automated tests won't tell me whether the game is fun—that still requires real players—but they'll let me evolve the gameplay with confidence as the project grows.

After making that decision, I recreated my UI mockups in KorGE and quickly confirmed that the framework could support the style I had in mind.

## Key Takeaways

- Software engineering practices transfer surprisingly well to game development.
- Build a prototype to answer questions, not to create production code.
- Keep the first milestone as small as possible.
- Validate gameplay before investing time in content or story.
- Choose tools that let you spend more time building your game and less time learning new technology.

## What's Next

In this post I mentioned concepts like User Story Mapping, Event Storming, ubiquitous language, and Gherkin without explaining them in detail.

In the next devlog, I'll walk through a single feature from start to finish and show exactly how I use these techniques while developing the game.