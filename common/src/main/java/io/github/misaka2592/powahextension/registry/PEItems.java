package io.github.misaka2592.powahextension.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.misaka2592.powahextension.PowahExtension;
import io.github.misaka2592.powahextension.item.PowahUpgraderItem;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import owmii.powah.block.Tier;

/** Item + creative tab registration, via the Architectury API (loader-agnostic). */
public final class PEItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(PowahExtension.MOD_ID, Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(PowahExtension.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<PowahUpgraderItem> UPGRADER_BASIC = registerUpgrader("upgrader_basic", Tier.BASIC);
    public static final RegistrySupplier<PowahUpgraderItem> UPGRADER_HARDENED = registerUpgrader("upgrader_hardened", Tier.HARDENED);
    public static final RegistrySupplier<PowahUpgraderItem> UPGRADER_BLAZING = registerUpgrader("upgrader_blazing", Tier.BLAZING);
    public static final RegistrySupplier<PowahUpgraderItem> UPGRADER_NIOTIC = registerUpgrader("upgrader_niotic", Tier.NIOTIC);
    public static final RegistrySupplier<PowahUpgraderItem> UPGRADER_SPIRITED = registerUpgrader("upgrader_spirited", Tier.SPIRITED);
    public static final RegistrySupplier<PowahUpgraderItem> UPGRADER_NITRO = registerUpgrader("upgrader_nitro", Tier.NITRO);

    public static final List<RegistrySupplier<PowahUpgraderItem>> UPGRADERS = List.of(
            UPGRADER_BASIC, UPGRADER_HARDENED, UPGRADER_BLAZING,
            UPGRADER_NIOTIC, UPGRADER_SPIRITED, UPGRADER_NITRO);

    public static final RegistrySupplier<CreativeModeTab> MAIN_TAB = TABS.register("main", () ->
            CreativeTabRegistry.create(
                    Component.translatable("itemGroup.powahextension"),
                    () -> new ItemStack(UPGRADER_NITRO.get())));

    private PEItems() {
    }

    public static void register() {
        ITEMS.register();
        TABS.register();
        registerTabContents();
    }

    @SuppressWarnings("unchecked")
    private static void registerTabContents() {
        CreativeTabRegistry.append(MAIN_TAB, UPGRADERS.toArray(new RegistrySupplier[0]));
    }

    private static RegistrySupplier<PowahUpgraderItem> registerUpgrader(String name, Tier tier) {
        return ITEMS.register(name, () -> new PowahUpgraderItem(new Item.Properties().stacksTo(16), tier));
    }
}
