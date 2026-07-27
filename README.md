# PowahExtension

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL_v3-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)](https://www.minecraft.net/)
[![Loader](https://img.shields.io/badge/Loader-Forge-orange.svg)](https://files.minecraftforge.net/)

为 [Powah! Rearchitected](https://github.com/Technici4n/Powah) 添加 Mekanism 风格的**机器升级器**：
手持升级器**潜行 + 右键**一台 Powah 机器，即可将其原地升级到下一等级 ——
机器内储存的**能量、物品、流体、红石模式全部保留**，无需拆放重来。

Adds Mekanism-style tiered upgraders to [Powah! Rearchitected](https://github.com/Technici4n/Powah):
sneak + right-click a Powah machine to upgrade it to the next tier in place,
keeping its stored energy, inventory, tank and redstone mode.

## 内容 / Content

6 个分级升级器（与 Powah 等级一一对应）/ Six tiered upgraders:

| 升级器 Upgrader | 作用于 Works on |
| --- | --- |
| 基础 Basic | 初级 Starter 机器 |
| 硬化 Hardened | 基础 Basic 机器 |
| 烈焰 Blazing | 硬化 Hardened 机器 |
| 钻石 Niotic | 烈焰 Blazing 机器 |
| 富生 Spirited | 钻石 Niotic 机器 |
| 下界 Nitro | 富生 Spirited 机器 |

- 每个升级器都有合成配方（对应等级电容 ×4 + 对应等级材料 ×4 + 介电外壳）。
  Each upgrader is craftable (4× tier capacitor + 4× tier material + dielectric casing).
- 独立创造模式物品栏；中英文双语。
  Dedicated creative tab; English & Simplified Chinese localizations.

## 支持的机器 / Supported Machines

默认支持 / Enabled by default:
Furnator · Magmator · Thermo Generator · Solar Panel · Energy Cell · Energizing Rod ·
Player Transmitter · Energy Hopper · Energy Discharger · Energy Cable

默认排除（可在配置中开启）/ Disabled by default (opt-in via config):
Reactor（多方块 multiblock）· Ender Cell · Ender Gate

## 配置 / Configuration

`config/powahextension.json`（首次运行自动生成 / generated on first launch）：

| 键 Key | 说明 Description | 默认 Default |
| --- | --- | --- |
| `enabledFamilies` | 允许升级的机器族（加上 `"reactor"` / `"ender_cell"` / `"ender_gate"` 可解锁） | 全部单方块机器 + 电缆 |
| `extraBlacklist` | 按方块 ID 禁止升级，如 `"powah:furnator_basic"` | `[]` |
| `playEffects` | 升级成功时播放音效和粒子 | `true` |
| `consumeInCreative` | 创造模式下也消耗升级器 | `false` |

## 安装 / Installation

1. 安装 Minecraft 1.20.1 + Forge 47+。
2. 安装依赖：[Powah! Rearchitected](https://modrinth.com/mod/powah) 5.0.x 与
   [Architectury API](https://modrinth.com/mod/architectury-api) 9.x（及 Powah 前置 Cloth Config）。
3. 将本模组 jar 放入 `mods` 文件夹。

## 构建 / Build

```bash
./gradlew :forge:build
# 产物 / artifact: forge/build/libs/
# 带 Powah 的开发客户端 / dev client with Powah installed:
./gradlew :forge:runClient
```

> **注意 / Note**：`libs/powah-forge-5.0.10.jar` 是修补过的 Powah 官方 jar ——
> 发布的 jar 里 `architectury.common.json` 引用了未打包的 `powah.accesswidener`
> （且其 intermediary 命名空间会让 loom 的 forge 重映射失败），因此开发环境使用
> 移除了该文件的副本（详见 `gradle.properties` 注释）。这只是开发依赖的处理方式，
> 玩家仍使用原版 Powah jar。
>
> The vendored `libs/powah-forge-5.0.10.jar` is the official Modrinth jar with
> `architectury.common.json` stripped (it references a `powah.accesswidener` that is
> not shipped, crashing loom). Players still use the stock Powah jar at runtime.

## 移植 / Porting

工程为 Architectury 多模块结构（与 Powah 官方同栈）：**全部逻辑在 `common/`**，
`forge/` 只有入口类与元数据。移植到新加载器（Fabric / NeoForge）或新版本（1.21+）时：

1. 复制 `forge/` 为新模块，替换入口类与 `mods.toml`（Fabric 为 `fabric.mod.json`）；
2. 提供一份 `IPlatform` 的 ServiceLoader 实现；
3. 在 `settings.gradle` 中 `include` 新模块，并在 `gradle.properties` 调整版本号。

The project uses an Architectury multi-module layout (the same stack Powah itself uses):
all logic lives in `common/`; `forge/` is glue only. See [PLAN.md](PLAN.md) for details.

## 更新日志 / Changelog

见 [CHANGELOG.md](CHANGELOG.md)。

## 许可 / License

LGPL-3.0（与 Powah 一致 / same as Powah）— 见 [LICENSE](LICENSE)。
