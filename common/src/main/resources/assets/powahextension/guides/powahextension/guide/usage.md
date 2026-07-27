---
navigation:
  title: Using Upgraders
  icon: powah:furnator_hardened
  parent: index.md
---

# Using Upgraders

Hold the upgrader and **sneak + right-click** a Powah machine.

* The machine must be **exactly one tier below** the upgrader.
* The machine is replaced **in place** — nothing is dropped, nothing needs rewiring.
* Everything inside is kept: stored **energy**, **inventory**, **fluid tank**,
  **redstone mode** and facing.
* One upgrader is consumed per machine (not in creative mode, unless configured).

A normal right-click (without sneaking) still opens the machine's GUI as usual.

## Supported Machines

Works out of the box on every single-block machine, **including energy cables**:
Furnators, Magmators, Thermo Generators, Solar Panels, Energy Cells, Energizing Rods,
Player Transmitters, Energy Hoppers and Energy Dischargers.

Excluded by default (can be enabled in the config):

* **Reactor** — upgrading a single part would break the formed multiblock.
* **Ender Cell / Ender Gate** — bound to a channel network.
