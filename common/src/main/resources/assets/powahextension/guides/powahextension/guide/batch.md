---
navigation:
  title: Batch Upgrading
  icon: powah:energy_cable_nitro
  parent: index.md
---

# Batch Upgrading

Hold an upgrader and press **sneak + sprint (left Ctrl by default) + right-click**
to upgrade a whole **connected array** of machines in one click.

## Preview

While you hold sneak + sprint and look at a machine, every machine the batch would
affect is outlined in the upgrader's **tier color** — what you see is what you get.

## How it works

* Starting from the clicked block, the mod flood-fills through **connected machines
  that this upgrader can upgrade** and upgrades them all.
* The flood does **not** pass through other blocks (or machines of the wrong tier),
  so mixed-tier arrays stop at the boundary.
* One upgrader item is consumed **per machine**; in survival the batch is limited by
  the stack in your hand.
* The batch is hard-capped (default **64** machines) — see
  `batchUpgradeLimit` in the config — so even a huge cable network is upgraded
  instantly without lag.

Perfect for upgrading a solar farm, a cable backbone, or a wall of energy cells
without touching each block individually.
