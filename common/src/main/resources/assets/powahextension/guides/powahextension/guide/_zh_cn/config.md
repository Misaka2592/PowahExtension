---
navigation:
  title: 配置
  icon: minecraft:writable_book
  parent: index.md
---

# 配置

配置文件 `config/powahextension.json` 在首次运行时自动生成。
更新新增的配置键会自动使用默认值。

| 键 | 默认值 | 说明 |
| --- | --- | --- |
| `enabledFamilies` | 全部单方块机器 + 电缆 | 允许升级的机器族。添加 `"reactor"`、`"ender_cell"`、`"ender_gate"` 可解锁。 |
| `extraBlacklist` | `[]` | 按方块 ID 禁止升级，如 `"powah:furnator_basic"`。 |
| `playEffects` | `true` | 升级成功时播放音效和粒子。 |
| `consumeInCreative` | `false` | 创造模式下也消耗升级器。 |
| `enableBatchUpgrade` | `true` | 启用潜行+疾跑+右键批量升级（及预览）。 |
| `batchUpgradeLimit` | `64` | 单次批量升级上限，同时限制查找开销。 |

## 机器族名称

`energy_cell` · `ender_cell` · `energy_cable` · `ender_gate` · `energizing_rod` ·
`furnator` · `magmator` · `thermo_generator` · `solar_panel` · `reactor` ·
`player_transmitter` · `energy_hopper` · `energy_discharger`
