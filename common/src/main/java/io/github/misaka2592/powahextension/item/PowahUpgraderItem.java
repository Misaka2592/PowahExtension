package io.github.misaka2592.powahextension.item;

import io.github.misaka2592.powahextension.config.PEConfig;
import io.github.misaka2592.powahextension.upgrade.UpgradeLogic;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import owmii.powah.block.Tier;

/**
 * Mekanism-style tiered installer. Sneak-right-clicking a Powah machine exactly one
 * tier below {@link #targetTier} upgrades it in place, keeping its stored energy,
 * inventory, tank and redstone mode.
 *
 * <p>Interaction note: with Forge's default {@code doesSneakBypassUse = false}, the
 * machine's {@code use()} (opening its GUI) is skipped while sneaking, so this
 * {@link #useOn} gets called instead; a plain right-click still opens the GUI.
 */
public class PowahUpgraderItem extends Item {
    private final Tier targetTier;

    public PowahUpgraderItem(Properties properties, Tier targetTier) {
        super(properties);
        this.targetTier = targetTier;
    }

    public Tier getTargetTier() {
        return this.targetTier;
    }

    /** The tier this upgrader upgrades FROM (always exactly one below the target). */
    public Tier getSourceTier() {
        return Tier.values()[this.targetTier.ordinal() - 1];
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        if (level.isClientSide) {
            // Swing the hand; the server does the real work and reports back.
            return InteractionResult.SUCCESS;
        }

        UpgradeLogic.Result result = UpgradeLogic.tryUpgrade(level, context.getClickedPos(), this.targetTier);
        switch (result) {
            case SUCCESS -> {
                if (!player.getAbilities().instabuild || PEConfig.CONFIG.consumeInCreative) {
                    context.getItemInHand().shrink(1);
                }
                player.displayClientMessage(Component.translatable(
                        "message.powahextension.upgraded",
                        tierName(this.targetTier).withStyle(ChatFormatting.GREEN)), true);
                return InteractionResult.CONSUME;
            }
            case WRONG_TIER -> {
                player.displayClientMessage(Component.translatable(
                        "message.powahextension.wrong_tier",
                        tierName(getSourceTier())), true);
                return InteractionResult.FAIL;
            }
            case FAMILY_DISABLED, BLACKLISTED -> {
                player.displayClientMessage(Component.translatable("message.powahextension.disabled"), true);
                return InteractionResult.FAIL;
            }
            default -> {
                return InteractionResult.PASS;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.powahextension.upgrader",
                tierName(getSourceTier()).withStyle(ChatFormatting.YELLOW),
                tierName(this.targetTier).withStyle(ChatFormatting.GREEN))
                .withStyle(ChatFormatting.GRAY));
        if (PEConfig.CONFIG.enableBatchUpgrade) {
            tooltip.add(Component.translatable("tooltip.powahextension.upgrader.batch",
                    PEConfig.CONFIG.batchUpgradeLimit)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static MutableComponent tierName(Tier tier) {
        return Component.translatable("tier.powahextension." + tier.getName());
    }
}
