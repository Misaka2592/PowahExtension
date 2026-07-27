# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-07-27

### Added

- Batch upgrade: sneak + sprint (left Ctrl by default) + right-click upgrades the clicked
  machine and every connected machine the upgrader applies to, in one click.
  潜行 + 疾跑（默认左 Ctrl）+ 右键：一键批量升级整片连通的适用机器。
  - Flood-fill search that only expands through eligible machines (never scans the world
    by radius), O(1) machine lookup, hard-capped per click.
  - One upgrader item is consumed per upgraded machine; in survival the batch is limited
    by the stack in hand.
- New config keys: `enableBatchUpgrade` (default `true`) and `batchUpgradeLimit`
  (default `64`). Existing config files keep working — new keys fall back to defaults.

## [1.0.0] - 2026-07-27

Initial release / 首次发布。

### Added

- Six tiered machine upgraders (Basic / Hardened / Blazing / Niotic / Spirited / Nitro).
  Sneak + right-click a Powah! machine exactly one tier below to upgrade it in place —
  stored energy, inventory, tank and redstone mode are all preserved.
  六个分级机器升级器，潜行 + 右键低一级的 Powah 机器即可原地升级，
  机器内的能量、物品、流体和红石模式全部保留。
- Crafting recipes for all six upgraders (tier capacitor + tier material + dielectric casing).
  全部六个升级器的合成配方（对应等级电容 + 对应等级材料 + 介电外壳）。
- JSON config at `config/powahextension.json`:
  - `enabledFamilies`: machine families that may be upgraded (default: all single-block
    machines plus energy cables; reactor and ender devices are excluded by default).
  - `extraBlacklist`: individual block ids that can never be upgraded.
  - `playEffects`: toggle upgrade sound + particles.
  - `consumeInCreative`: consume the upgrader even in creative mode.
- English and Simplified Chinese localizations.
- Dedicated creative mode tab.

[1.1.0]: https://github.com/Misaka2592/PowahExtension/releases/tag/v1.1.0
[1.0.0]: https://github.com/Misaka2592/PowahExtension/releases/tag/v1.0.0
