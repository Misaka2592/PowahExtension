# PowahExtension — Powah! Rearchitected 机器升级器模组（1.20.1 Forge 先行，多模块可移植）

## Context（背景与目标）

为 Powah! Rearchitected 开发一个附属模组：添加类似 Mekanism 工厂安装器的「PowahUpgrader」物品，
手持并**潜行右键**一台 Powah 机器，即可将其从低一级升级到对应等级，**保留机器内物品、能量、红石模式等全部数据**。

- 首发目标：Minecraft 1.20.1 + Forge
- 硬性要求：为其他版本（1.21+）和其他加载器（Fabric / NeoForge）留好移植余量

## 已确认的 Powah 1.20.1 内部机制（调研结论，实现依据）

源码：`Technici4n/Powah` 分支 `1.20.1`（官方仓库，本身即 Architectury 多模块：common/fabric/forge）。

1. **等级枚举** `owmii.powah.block.Tier`：`STARTER, BASIC, HARDENED, BLAZING, NIOTIC, SPIRITED, NITRO, CREATIVE`，
   `getNormalVariants()` 返回前 7 级。升级器做 6 个：BASIC→NITRO（BASIC 升级器把 STARTER 机器升为 BASIC，依此类推）。
2. **方块注册表** `owmii.powah.block.Blcks`：每族机器是一个 `VarReg<Tier, Block>`，
   `varReg.get(tier)` 直接拿到该等级的 Block 实例 —— 升级映射不需要猜注册名，编译期安全。
3. **方块实体** `owmii.powah.block.Tiles`：**一族所有等级共用同一个 `BlockEntityType`**
   （如 `Tiles.FURNATOR` 覆盖全部 7 个等级方块）。这意味着原地换方块后新 TE 类型不变，`load()` 完全兼容。
4. **TE 序列化** `AbstractTileEntity.readSync/writeSync`：能量、物品栏、流体罐、红石模式都存进同一个 NBT；
   tier 以 `nbt.putInt("variant", tier.ordinal())` 存储（见 `IVariant.write`）。
   **关键：升级时必须先把 NBT 里的 `variant` 改成新等级再 `load()`**，否则旧 tier 会被读回去。
5. **交互时机**：Forge 默认 `doesSneakBypassUse=false` —— 玩家潜行时方块的 `use()`（打开 GUI）被跳过，
   物品的 `useOn()` 会被调用；非潜行右键照常打开机器 GUI。升级逻辑写在物品的 `useOn()` 中即可，无需事件拦截。
6. **依赖获取**：Powah 发布在 Modrinth Maven（`https://api.modrinth.com/maven`，坐标 `maven.modrinth:powah:5.0.10-forge`），
   无需 CurseForge API key。Powah 运行时还需 `architectury-forge` + `cloth-config-forge`。

## 工程结构（Architectury 多模块，与 Powah 官方同栈）

```
PowahExtension/
├── settings.gradle / build.gradle / gradle.properties
├── common/                          ← 全部逻辑，纯 Mojang 映射，零 Forge API
│   └── src/main/java/<group>.powahextension/
│       ├── PowahExtension.java          # 公共初始化入口
│       ├── item/PowahUpgraderItem.java  # 升级器物品（useOn → UpgradeLogic）
│       ├── upgrade/UpgradeLogic.java    # 核心升级算法（纯逻辑，可单测）
│       ├── upgrade/MachineFamilies.java # VarReg 家族注册表 + 反查索引
│       ├── config/PEConfig.java         # Gson JSON 配置（跨加载器）
│       ├── platform/IPlatform.java + Services.java  # 加载器抽象（configDir 等）
│       └── registry/PEItems.java        # Architectury DeferredRegister 注册 6 个物品
│   └── src/main/resources/
│       ├── assets/powahextension/{lang,models/item,textures/item}
│       └── data/powahextension/recipes/
├── forge/
│   └── src/main/java/<group>.powahextension.forge/PowahExtensionForge.java  # 仅入口 + 注册触发
│   └── src/main/resources/META-INF/mods.toml, pack.mcmeta
└── (未来) fabric/ 、 neoforge/        ← 移植时新增，每个模块只需入口类 + 元数据
```

- 构建栈与 Powah 1.20.1 完全一致：architectury-loom + architectury-plugin + Mojang 官方映射，Java 17，Gradle 8.x。
- 依赖 architectury-api（`dev.architectury:architectury-forge:9.2.14`）做跨加载器注册 —— Powah 本身就要求它，零额外成本。
- `gradle.properties` 集中管理版本号；包名 group 使用 `io.github.<你的GitHub用户名>`（**实施开始时可随时改**，先占位）。
- modid：`powahextension`；物品 ID：`powahextension:upgrader_basic` … `upgrader_nitro`。

## 核心升级算法（`UpgradeLogic`，全部在 common）

1. 反查：被点击的 Block 属于哪个 `VarReg` 家族 + 哪个 Tier（`MachineFamilies` 预建 `Block → (family, tier)` 索引）。
2. 校验：家族已启用（配置）且 `tier.ordinal() == upgraderTier.ordinal() - 1`，否则给玩家动作栏提示并返回。
3. 执行（仅服务端）：
   ```java
   CompoundTag data = oldTe.saveWithoutMetadata();
   data.putInt("variant", newTier.ordinal());        // 关键：覆盖 tier
   BlockState newState = copySharedProperties(oldState, newBlock.defaultBlockState()); // FACING/LIT/WATERLOGGED 等同名属性
   level.removeBlockEntity(pos);
   level.setBlock(pos, newState, Block.UPDATE_ALL);
   BlockEntity newTe = level.getBlockEntity(pos);
   newTe.load(data);
   newTe.setChanged();
   ```
4. 收尾：非创造模式消耗 1 个升级器；播放升级音效 + 粒子；动作栏消息「已升级到 X 级」。

### 默认升级范围（配置可改）

- **默认启用**：Furnator、Magmator、Thermo Generator、Solar Panel、Energy Cell、Energizing Rod、
  Player Transmitter、Energy Hopper、Energy Discharger、**Energy Cable**（含电缆，按你的选择）。
- **默认排除**：Reactor（多方块，升级单部件会破坏结构）、Ender Cell / Ender Gate（绑定频道网络，保守排除）。
- 配置 `config/powahextension.json`（Gson）：`enabledFamilies`、`extraBlacklist`（按方块 ID）、`playEffects` 等。

### 已知风险点

- 电缆的 TE 由 `EnvHandler.createCable(pos, state, Tier.STARTER)` 创建，tier 处理与其他机器不同 ——
  电缆升级列为**必须实测**项；若异常则从默认启用列表移除。
- 换方块会触发邻居更新（线缆网络会重连，属预期行为）。

## 资源与内容

- **合成配方**（JSON，6 个）：中心 `powah:dielectric_casing`，两侧对应等级 `powah:capacitor_<tier>`，
  上下对应等级材料（energized_steel / blazing_crystal / niotic_crystal / spirited_crystal / nitro_crystal），
  具体 ID 实施时对照 Powah `Itms` 核实。
- **贴图**：16×16 程序化生成的占位贴图（安装器轮廓 + 按 `Tier` 颜色着色的饰带），后续可换美术资源。
- **语言文件**：en_us + zh_cn（物品名、提示、动作栏消息）。
- 创造模式物品栏：独立「Powah Extension」标签页。
- `mods.toml`：必需依赖 `powah`、`architectury`；Forge `[47,)`；pack_format 15。

## 实施步骤

1. 初始化 Gradle 多模块工程（settings/build.gradle/gradle.properties/wrapper），接入 Modrinth/Architectury maven。
2. common：配置系统 → MachineFamilies → UpgradeLogic → PowahUpgraderItem → PEItems 注册。
3. forge：入口类 + mods.toml + pack.mcmeta；runtime 引入 Powah/architectury/cloth-config。
4. 资源：lang×2、item model×6、贴图×6（脚本生成）、配方×6。
5. 编译 + `runClient` 实测（见下）。
6. 收尾：README（用法/配置/移植指南）、LICENSE（LGPL，与 Powah 兼容）。

## 验证

1. `./gradlew :forge:build` 编译通过。
2. `./gradlew :forge:runClient`（开发环境自动带 Powah）：创造世界放置 STARTER Furnator →
   放入燃料、充满能量、改红石模式 → 手持 BASIC 升级器潜行右键 → 确认：方块变为 BASIC、
   **物品/能量/红石模式全部保留**、升级器消耗（切生存验证）。
3. 边界用例：非潜行右键仍正常打开 GUI；NITRO 机器提示已是最高级；反应堆/末影设备提示不支持；
   配置中禁用某家族后该家族拒绝升级。
4. 电缆升级专项实测（tier 与能量网络是否正常重连）。

## 移植余量说明

- 新增加载器 = 新增一个 gradle 模块：入口类（十几行）+ `fabric.mod.json`/`neoforge.mods.toml`，common 零改动。
- 1.21+：Powah 官方 1.21.1 同为 Architectury 多模块，升级 gradle.properties 版本号 + 适配少量 Mojmap 改名即可。
- 1.20.1 Fabric：Powah 官方有 Fabric 构建（`maven.modrinth:powah:5.0.4-fabric`），直接可加 fabric 模块。
