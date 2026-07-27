package io.github.misaka2592.powahextension.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import io.github.misaka2592.powahextension.PowahExtension;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * Forge bootstrap. Everything else lives in the common module; when porting to
 * Fabric/NeoForge this class is replaced by an equally small entry point.
 */
@Mod(PowahExtension.MOD_ID)
public final class PowahExtensionForge {
    public PowahExtensionForge() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Submit our event bus so architectury's DeferredRegister can register
        // content at the right time (required on Forge before PowahExtension.init()).
        EventBuses.registerModEventBus(PowahExtension.MOD_ID, modBus);
        PowahExtension.init();
        EnvExecutor.runInEnv(Env.CLIENT, () -> PowahExtension::initClient);

        if (FMLEnvironment.dist == Dist.CLIENT && !FMLEnvironment.production) {
            // Dev-only workaround: on Forge 1.20.1 the initial resource reload races client
            // config loading, and Forge hard-fails config reads in dev ("Cannot get config
            // value before config is loaded"; production only warns). Mods that read config
            // values in a resource reload listener (GuideME 20.1.x) therefore crash the dev
            // client. AddPackFindersEvent fires right after all mods are constructed and
            // before the first resource reload, so pre-loading client configs here is safe.
            modBus.addListener((AddPackFindersEvent event) -> {
                try {
                    ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, FMLPaths.CONFIGDIR.get());
                } catch (Exception e) {
                    PowahExtension.LOGGER.warn("Early client config preload failed", e);
                }
            });
        }
    }
}
