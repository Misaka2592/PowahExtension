package io.github.misaka2592.powahextension.upgrade;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import io.github.misaka2592.powahextension.client.BatchUpgradeClient;
import io.github.misaka2592.powahextension.config.PEConfig;
import io.github.misaka2592.powahextension.item.PowahUpgraderItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Intercepts sneak + sprint + right-click on the CLIENT and turns it into a batch-upgrade
 * packet. Interrupting with a SUCCESS-equivalent result cancels the vanilla interaction,
 * so the server never sees the original click — without this, the server-side
 * {@code useOn} would perform a single upgrade and the batch flood would then find the
 * clicked machine already upgraded and stop.
 */
public final class BatchUpgradeHandler {
    private BatchUpgradeHandler() {
    }

    public static void register() {
        InteractionEvent.RIGHT_CLICK_BLOCK.register(BatchUpgradeHandler::onRightClickBlock);
    }

    private static EventResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof PowahUpgraderItem) || !player.isShiftKeyDown()) {
            return EventResult.pass();
        }
        // Client side only, short-circuited before touching client-only classes.
        if (player.level().isClientSide && PEConfig.CONFIG.enableBatchUpgrade && BatchUpgradeClient.isBatchKeyDown()) {
            BatchUpgradeClient.sendBatchUpgrade(pos, hand);
            // interruptTrue -> InteractionResult.SUCCESS on the loader side: cancels the
            // vanilla click (no useItemOn packet) and swings the arm.
            return EventResult.interruptTrue();
        }
        return EventResult.pass();
    }
}
