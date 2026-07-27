---
navigation:
  title: Configuration
  icon: minecraft:writable_book
  parent: index.md
---

# Configuration

The config file `config/powahextension.json` is created on first launch.
New keys added by updates automatically fall back to their defaults.

| Key | Default | Description |
| --- | --- | --- |
| `enabledFamilies` | all single-block machines + cables | Machine families that may be upgraded. Add `"reactor"`, `"ender_cell"` or `"ender_gate"` to opt in. |
| `extraBlacklist` | `[]` | Block ids that can never be upgraded, e.g. `"powah:furnator_basic"`. |
| `playEffects` | `true` | Sound + particles on a successful upgrade. |
| `consumeInCreative` | `false` | Consume upgraders even in creative mode. |
| `enableBatchUpgrade` | `true` | Enable sneak + sprint + right-click batch upgrading (and its preview). |
| `batchUpgradeLimit` | `64` | Max machines per batch. Also bounds the search cost. |

## Family names

`energy_cell` · `ender_cell` · `energy_cable` · `ender_gate` · `energizing_rod` ·
`furnator` · `magmator` · `thermo_generator` · `solar_panel` · `reactor` ·
`player_transmitter` · `energy_hopper` · `energy_discharger`
