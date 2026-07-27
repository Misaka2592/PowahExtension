package io.github.misaka2592.powahextension.client;

import io.github.misaka2592.powahextension.config.PEConfig;
import io.github.misaka2592.powahextension.item.PowahUpgraderItem;
import io.github.misaka2592.powahextension.upgrade.UpgradeLogic;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import owmii.powah.block.Tier;

/**
 * Client-only state for the batch-upgrade preview: while the player holds an upgrader and
 * presses sneak + sprint, this computes (via {@link UpgradeLogic#collectTargets}) which
 * connected machines a batch click would affect, so a renderer can outline them.
 *
 * <p>Performance: the flood fill only runs when the looked-at block or held tier changes,
 * or once every {@value #RECOMPUTE_INTERVAL_TICKS} ticks to catch world changes; rendering
 * just reads the cached list. The search itself is hard-capped by the config limit.
 */
public final class BatchPreview {
    /** How often (in client ticks) targets are recomputed even if nothing else changed. */
    private static final int RECOMPUTE_INTERVAL_TICKS = 10;

    private static List<BlockPos> targets = List.of();
    private static int color = 0xFFFFFF;
    private static BlockPos lastOrigin;
    private static Tier lastTier;
    private static long lastRecomputeTick = Long.MIN_VALUE;

    private BatchPreview() {
    }

    /** Machines the batch upgrade would affect right now; empty when the preview is inactive. */
    public static List<BlockPos> getTargets() {
        return targets;
    }

    /** Preview outline color (ARGB channels in RGB order) — the upgrader's tier color. */
    public static int getColor() {
        return color;
    }

    /** Called every client tick (registered from the common init via ClientTickEvent). */
    public static void clientTick(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null || minecraft.level == null || !PEConfig.CONFIG.enableBatchUpgrade) {
            clear();
            return;
        }

        PowahUpgraderItem upgrader = findHeldUpgrader(player);
        if (upgrader == null || !player.isShiftKeyDown() || !BatchUpgradeClient.isBatchKeyDown()) {
            clear();
            return;
        }

        if (!(minecraft.hitResult instanceof BlockHitResult hitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            clear();
            return;
        }

        BlockPos origin = hitResult.getBlockPos();
        Tier tier = upgrader.getTargetTier();
        boolean changed = !origin.equals(lastOrigin) || tier != lastTier;
        boolean stale = minecraft.level.getGameTime() - lastRecomputeTick >= RECOMPUTE_INTERVAL_TICKS;
        if (changed || stale) {
            lastOrigin = origin;
            lastTier = tier;
            lastRecomputeTick = minecraft.level.getGameTime();
            int limit = Math.max(1, PEConfig.CONFIG.batchUpgradeLimit);
            if (!player.getAbilities().instabuild || PEConfig.CONFIG.consumeInCreative) {
                limit = Math.min(limit, getHeldCount(player, upgrader));
            }
            targets = UpgradeLogic.collectTargets(minecraft.level, origin, tier, limit);
            color = tier.getColor();
        }
    }

    private static PowahUpgraderItem findHeldUpgrader(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof PowahUpgraderItem upgrader) {
                return upgrader;
            }
        }
        return null;
    }

    private static int getHeldCount(Player player, PowahUpgraderItem upgrader) {
        int count = 0;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() == upgrader) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void clear() {
        if (!targets.isEmpty()) {
            targets = List.of();
        }
        lastOrigin = null;
        lastTier = null;
    }
}
