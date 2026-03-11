# Arcana Mod

Arcana is a mod for **Minecraft 1.21.1**, developed using **Java 21** and **NeoForge**.  
This project is currently **in development**, and many systems, mechanics and story elements are actively evolving.

Arcana is not simply about adding new content. It is about atmosphere, mystery, choice, and consequence.  
If you open the book, you choose to deal with whatever comes after.

---

## Lore

An ancient mage named **Kaliastrus Novarcanus** bound his legacy inside a cursed diary.  
This book can be found inside special structures that appear on plains, close to the player.

Inside the diary, you will discover powerful rituals capable of summoning bosses in a fixed sequence.  
Each boss must be defeated in order, because every ritual requires an item dropped by the previous one.

When each boss is summoned, it speaks to the player — taunting, warning, or mocking — right before the battle begins.  
Defeating them grants permanent effects and powers, marking those who survive with traces of forbidden magic.

---

## Language Support

Arcana currently supports the following languages:

- `en_us`
- `pt_br`

If another language is selected in Minecraft, `en_us` will be used as fallback.

---

## Technology

| | |
|---|---|
| Minecraft | **1.21.1** |
| NeoForge | **21.1.217** |
| Java | **21** |
| Gradle | **9.2.0** |
| Mappings | Parchment 2024.11.17 |

---

## Current Content

### Systems
- **Ritual Generation** — A magic circle structure spawns near the player on their first time in Plains biome. A bound diary is placed inside the central chest.
- **Bound Diary** — The diary is permanently linked to the player. If dropped or lost, it returns automatically. Cannot be duplicated.
- **Dream System** — When the player wakes from sleeping, a dream may trigger. The first dream guides the player toward the diary's location.
- **Delayed Narrative Messages** — Narrative messages are delivered over time, spaced out with ticks to create atmosphere.

### Boss — The Fool
- Summoned via the **Diorite Pedestal** ritual (requires Nether Star + Strange Totem + fire)
- Flying boss with a custom model, animations, and boss bar (purple)
- Combat phases: circling, windup, dash, melee, retreat, recover
- Special mechanics: stun on shield block, rage shockwave, push attack, death sequence with dialogue
- Drops: **Fool's Soul** item

### Items
- `Diary of Kaliastrus` — bound narrative item, opens a custom GUI with lore pages
- `Strange Totem` — found in Desert Pyramid loot; required for the boss ritual
- `Fool's Soul` — dropped by The Fool

### Blocks
- `Diorite Pedestal` — ritual activation block with custom hitbox and renderer

---

## Development Status

Arcana is under active development. Features, systems, story content and balance are subject to change as the mod grows.

### Known Roadmap Items
- Rename package `com.example.arcana` to a unique identifier
- Implement loot table JSON for The Fool (currently drops via code)
- Implement or remove `USING_CUBIC_DOMAIN` / cubic domain attack
- Add more dream types
- Implement boss phases (e.g. activate cubic domain below 50% HP)
