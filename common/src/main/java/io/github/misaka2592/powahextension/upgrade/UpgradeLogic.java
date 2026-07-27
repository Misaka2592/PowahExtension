package io.github.misaka2592.powahextension.upgrade;

import io.github.misaka2592.powahextension.PowahExtension;
import io.github.misaka2592.powahextension.config.PEConfig;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import owmii.powah.block.Tier;

/**
 * The Mekanism-style in-world upgrade, applied to Powah machines.
 *
 * <p>How it works with Powah's internals (1.20.1, {@code Technici4n/Powah}):
 * <ul>
 *   <li>All tiers of a machine family share one {@code BlockEntityType}
 *       (see {@code owmii.powah.block.Tiles}), so a freshly placed higher-tier block
 *       creates a block entity that can load the old one's NBT verbatim.</li>
 *   <li>The tier itself is serialized as {@code nbt.putInt("variant", tier.ordinal())}
 *       (see {@code owmii.powah.lib.registry.IVariant#write} and
 *       {@code AbstractTileEntity#readSync}). It MUST be rewritten before loading,
 *       otherwise the old tier is read back.</li>
 *   <li>Energy, inventory, tank and redstone mode all live in the same NBT blob
 *       ({@code AbstractTileEntity#writeSync}), so they carry over untouched.</li>
 * </ul>
 *
 * <p>Pure Mojang-mapped code, no loader APIs — shared by every loader module.
 */
public final class UpgradeLogic {

    public enum Result {
        /** From {@link #check}: the block is eligible for the upgrade. */
        SUCCESS,
        /** The clicked block is not a tiered Powah machine. */
        NOT_MACHINE,
        /** The machine family is disabled in the config. */
        FAMILY_DISABLED,
        /** The specific block id is blacklisted in the config. */
        BLACKLISTED,
        /** The machine is not exactly one tier below the upgrader. */
        WRONG_TIER,
        /** The eligibility check passed, but the block could not be replaced. */
        UPGRADE_FAILED,
    }

    private UpgradeLogic() {
    }

    /**
     * Checks whether the block at {@code pos} can be upgraded to {@code targetTier}.
     * {@link Result#SUCCESS} means eligible. O(1): machine identification uses the
     * prebuilt identity index in {@link MachineFamilies}, plus two config set lookups.
     */
    public static Result check(Level level, BlockPos pos, Tier targetTier) {
        BlockState state = level.getBlockState(pos);
        MachineFamilies.Located located = MachineFamilies.locate(state.getBlock());
        if (located == null) {
            return Result.NOT_MACHINE;
        }
        if (!PEConfig.CONFIG.enabledFamilies.contains(located.family().name())) {
            return Result.FAMILY_DISABLED;
        }
        if (PEConfig.CONFIG.extraBlacklist.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString())) {
            return Result.BLACKLISTED;
        }
        if (located.tier().ordinal() != targetTier.ordinal() - 1) {
            return Result.WRONG_TIER;
        }
        return Result.SUCCESS;
    }

    /**
     * Performs the in-place block swap. Server side only; call {@link #check} first.
     * Re-verifies the tier at execution time so stale batch targets are skipped safely.
     *
     * @return true if the machine was upgraded
     */
    public static boolean upgradeOne(Level level, BlockPos pos, Tier targetTier, boolean playSound) {
        BlockState oldState = level.getBlockState(pos);
        MachineFamilies.Located located = MachineFamilies.locate(oldState.getBlock());
        if (located == null || located.tier().ordinal() != targetTier.ordinal() - 1) {
            return false; // changed (or already upgraded) since the check
        }

        BlockEntity oldBlockEntity = level.getBlockEntity(pos);
        CompoundTag data = oldBlockEntity != null ? oldBlockEntity.saveWithoutMetadata() : new CompoundTag();
        // Rewrite the stored tier, see class javadoc.
        data.putInt("variant", targetTier.ordinal());

        Block newBlock = located.family().blocks().get(targetTier);
        BlockState newState = copySharedProperties(oldState, newBlock.defaultBlockState());

        level.removeBlockEntity(pos);
        if (!level.setBlock(pos, newState, Block.UPDATE_ALL)) {
            PowahExtension.LOGGER.warn("Failed to replace block at {} while upgrading to {}", pos, targetTier);
            return false;
        }

        BlockEntity newBlockEntity = level.getBlockEntity(pos);
        if (newBlockEntity != null) {
            newBlockEntity.load(data);
            newBlockEntity.setChanged();
        }

        if (PEConfig.CONFIG.playEffects && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                    20, 0.3, 0.3, 0.3, 0.05);
            if (playSound) {
                serverLevel.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5F, 1.5F);
            }
        }
        return true;
    }

    /**
     * Attempts to upgrade the machine at {@code pos} to {@code targetTier}.
     * Must be called on the server side only.
     */
    public static Result tryUpgrade(Level level, BlockPos pos, Tier targetTier) {
        Result result = check(level, pos, targetTier);
        if (result != Result.SUCCESS) {
            return result;
        }
        return upgradeOne(level, pos, targetTier, true) ? Result.SUCCESS : Result.UPGRADE_FAILED;
    }

    /**
     * Collects the positions a batch upgrade starting at {@code origin} would affect:
     * the clicked machine plus every connected machine this upgrader applies to,
     * up to {@code maxCount}. Read-only — used by both the server-side
     * {@link #batchUpgrade} and the client-side target preview.
     *
     * <p>Performance notes:
     * <ul>
     *   <li>Flood fill that only expands THROUGH eligible machines — the searched region is
     *       exactly the connected machine array; the world is never scanned by radius.</li>
     *   <li>Eligibility is O(1) per block ({@link #check}), positions are deduplicated with a
     *       {@link HashSet}, and the frontier uses an {@link ArrayDeque} (no recursion).</li>
     *   <li>{@code maxCount} hard-caps the search, so a click on a huge cable network costs
     *       bounded time.</li>
     * </ul>
     */
    public static List<BlockPos> collectTargets(Level level, BlockPos origin, Tier targetTier, int maxCount) {
        if (maxCount <= 0) {
            return List.of();
        }

        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> frontier = new ArrayDeque<>();
        List<BlockPos> targets = new ArrayList<>();
        visited.add(origin);
        frontier.add(origin);

        while (!frontier.isEmpty() && targets.size() < maxCount) {
            BlockPos current = frontier.poll();
            if (check(level, current, targetTier) != Result.SUCCESS) {
                // Do not expand through non-eligible blocks: the flood stays inside the
                // connected array of upgradeable machines.
                continue;
            }
            targets.add(current.immutable());
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (visited.add(next)) {
                    frontier.add(next);
                }
            }
        }
        return targets;
    }

    /**
     * Batch upgrade (sneak + sprint + right-click): upgrades everything
     * {@link #collectTargets} finds, up to {@code maxCount}.
     *
     * @return the number of machines actually upgraded
     */
    public static int batchUpgrade(Level level, BlockPos origin, Tier targetTier, int maxCount) {
        int upgraded = 0;
        for (BlockPos target : collectTargets(level, origin, targetTier, maxCount)) {
            // Sound only on the first block of the batch; particles on each.
            if (upgradeOne(level, target, targetTier, upgraded == 0)) {
                upgraded++;
            }
        }
        return upgraded;
    }

    /** Copies block state properties (facing, lit, waterlogged, ...) shared by both tiers. */
    private static BlockState copySharedProperties(BlockState from, BlockState to) {
        for (Property<?> property : from.getProperties()) {
            if (to.hasProperty(property)) {
                to = copyProperty(from, to, property);
            }
        }
        return to;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }
}
