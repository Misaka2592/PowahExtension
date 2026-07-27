package io.github.misaka2592.powahextension.network;

import dev.architectury.networking.NetworkManager;
import io.github.misaka2592.powahextension.PowahExtension;
import io.github.misaka2592.powahextension.config.PEConfig;
import io.github.misaka2592.powahextension.item.PowahUpgraderItem;
import io.github.misaka2592.powahextension.upgrade.UpgradeLogic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * C2S packet for the batch upgrade (sneak + sprint + right-click). The sprint key state
 * only exists on the client, so the client intercepts the interaction and sends this
 * packet instead of letting vanilla process the click. Implemented with the Architectury
 * NetworkManager so it works unchanged on every loader.
 */
public final class PENetwork {
    public static final ResourceLocation BATCH_UPGRADE = PowahExtension.id("batch_upgrade");

    private PENetwork() {
    }

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), BATCH_UPGRADE, (buf, context) -> {
            BlockPos origin = buf.readBlockPos();
            InteractionHand hand = buf.readEnum(InteractionHand.class);
            context.queue(() -> handleBatchUpgrade(context, origin, hand));
        });
    }

    private static void handleBatchUpgrade(NetworkManager.PacketContext context, BlockPos origin, InteractionHand hand) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (!PEConfig.CONFIG.enableBatchUpgrade) {
            return;
        }
        // Server-side validation of everything the client claimed:
        // correct item in the claimed hand, actually sneaking, and within normal reach.
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof PowahUpgraderItem upgrader)) {
            return;
        }
        if (!player.isShiftKeyDown()) {
            return;
        }
        if (origin.distSqr(player.blockPosition()) > 64.0) { // 8 blocks, generous reach
            PowahExtension.LOGGER.warn("Ignoring batch upgrade from {} for out-of-reach pos {}",
                    player.getGameProfile().getName(), origin);
            return;
        }

        int limit = Math.max(1, PEConfig.CONFIG.batchUpgradeLimit);
        if (!player.getAbilities().instabuild || PEConfig.CONFIG.consumeInCreative) {
            limit = Math.min(limit, stack.getCount()); // one upgrader per machine
        }

        int upgraded = UpgradeLogic.batchUpgrade(player.level(), origin, upgrader.getTargetTier(), limit);
        if (upgraded > 0) {
            if (!player.getAbilities().instabuild || PEConfig.CONFIG.consumeInCreative) {
                stack.shrink(upgraded);
            }
            player.displayClientMessage(Component.translatable(
                    "message.powahextension.batch_upgraded",
                    Component.literal(String.valueOf(upgraded)).withStyle(ChatFormatting.GREEN)), true);
        } else {
            player.displayClientMessage(Component.translatable("message.powahextension.batch_none"), true);
        }
    }
}
