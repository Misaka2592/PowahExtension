package io.github.misaka2592.powahextension.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.misaka2592.powahextension.PowahExtension;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge bootstrap. Everything else lives in the common module; when porting to
 * Fabric/NeoForge this class is replaced by an equally small entry point.
 */
@Mod(PowahExtension.MOD_ID)
public final class PowahExtensionForge {
    public PowahExtensionForge() {
        // Submit our event bus so architectury's DeferredRegister can register
        // content at the right time (required on Forge before PowahExtension.init()).
        EventBuses.registerModEventBus(
                PowahExtension.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        PowahExtension.init();
    }
}
