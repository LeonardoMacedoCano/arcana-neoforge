# Arcana Mod

A narrative and lore mod for **Minecraft 1.21.1** built with **NeoForge**. The theme is arcane occultism with tarot cards — the first boss is **The Fool**.

> *"An ancient mage named Kaliastrus Novarcanus bound his legacy inside a cursed diary. If you open the book, you choose to deal with whatever comes after."*

---

## What This Mod Does

When a player first enters a Plains biome, a **magic ritual circle** generates nearby with a chest containing a **bound diary**. The diary is permanently linked to that player — if dropped or lost, it returns automatically and cannot be duplicated.

Sleeping triggers a **dream sequence** that guides the player toward the ritual site. The diary contains lore pages written by Kaliastrus, and reading them reveals the rituals needed to summon the bosses.

Each boss is summoned via a **Diorite Pedestal** ritual and must be defeated in sequence, since each ritual requires an item dropped by the previous boss.

---

## Current Content

### Boss — The Fool
- Summoned at the **Diorite Pedestal** with a Nether Star + Strange Totem + fire source
- Flying boss with a custom 3D model, animations, and a purple boss bar
- Combat system: circling, windup, dash, melee, retreat, recover
- Special mechanics: stun on shield block, rage shockwave, push attack
- Speaks to the player during summon and at death
- Drops: **Fool's Soul**

### Systems
| System | Description |
|---|---|
| Ritual Generation | Magic circle spawns in Plains on first login, with the diary in the central chest |
| Bound Diary | Permanently linked to the player — auto-returns if lost, prevents duplicates |
| Dream System | Dreams trigger on wake-up; the first one guides the player to the diary |
| Narrative Messages | Messages are spaced over time using a tick-based queue for atmosphere |
| Desert Pyramid Loot | Strange Totem has a 15% chance to appear in pyramid chests |

### Items
| Item | Description |
|---|---|
| Diary of Kaliastrus | Opens a custom GUI with lore pages; bound to one player |
| Strange Totem | Found in Desert Pyramids; required for The Fool's ritual |
| Fool's Soul | Dropped by The Fool |

### Blocks
| Block | Description |
|---|---|
| Diorite Pedestal | Ritual activation block with custom hitbox and item renderer |

---

## Language Support

Fully translated in `en_us` and `pt_br`. Falls back to `en_us` for any other language.

---

## Tech Stack

| | |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.217 |
| Java | 21 |
| Gradle | 9.2.0 |
| Mappings | Parchment 2024.11.17 |

---

## Authors

**PSICOQUATO** and **LCano**
