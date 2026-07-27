package io.github.misaka2592.powahextension.forge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.misaka2592.powahextension.PowahExtension;
import io.github.misaka2592.powahextension.client.BatchPreview;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Renders the batch-upgrade preview: while the player holds sneak + sprint with an
 * upgrader in hand, every machine the batch would affect gets an outline in the
 * upgrader's tier color. The target list is computed (and rate-limited) by
 * {@link BatchPreview} in the common module; this class is only the Forge render hook.
 * When porting to another loader, re-subscribe the equivalent level-render event.
 */
@Mod.EventBusSubscriber(modid = PowahExtension.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BatchPreviewRenderer {
    private static final float ALPHA = 0.9F;
    private static final double GROW = 0.002D;

    private BatchPreviewRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        List<BlockPos> targets = BatchPreview.getTargets();
        if (targets.isEmpty()) {
            return;
        }

        int color = BatchPreview.getColor();
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        Minecraft minecraft = Minecraft.getInstance();
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        for (BlockPos pos : targets) {
            LevelRenderer.renderLineBox(poseStack, consumer,
                    pos.getX() - GROW, pos.getY() - GROW, pos.getZ() - GROW,
                    pos.getX() + 1 + GROW, pos.getY() + 1 + GROW, pos.getZ() + 1 + GROW,
                    red, green, blue, ALPHA);
        }
        poseStack.popPose();
        buffers.endBatch(RenderType.lines());
    }
}
