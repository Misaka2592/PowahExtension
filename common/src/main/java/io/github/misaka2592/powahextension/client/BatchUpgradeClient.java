package io.github.misaka2592.powahextension.client;

import dev.architectury.networking.NetworkManager;
import io.github.misaka2592.powahextension.network.PENetwork;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

/**
 * Client-only helpers for the batch upgrade. Only referenced from code paths guarded by
 * {@code level.isClientSide}, never loaded on a dedicated server.
 */
public final class BatchUpgradeClient {
    private BatchUpgradeClient() {
    }

    /** The vanilla sprint key (left Ctrl by default) — the third key of the batch combo. */
    public static boolean isBatchKeyDown() {
        return Minecraft.getInstance().options.keySprint.isDown();
    }

    public static void sendBatchUpgrade(BlockPos pos, InteractionHand hand) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeEnum(hand);
        NetworkManager.sendToServer(PENetwork.BATCH_UPGRADE, buf);
    }
}
