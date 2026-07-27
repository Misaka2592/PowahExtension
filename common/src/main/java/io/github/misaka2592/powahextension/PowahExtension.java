package io.github.misaka2592.powahextension;

import dev.architectury.event.events.client.ClientTickEvent;
import io.github.misaka2592.powahextension.client.BatchPreview;
import io.github.misaka2592.powahextension.config.PEConfig;
import io.github.misaka2592.powahextension.network.PENetwork;
import io.github.misaka2592.powahextension.registry.PEItems;
import io.github.misaka2592.powahextension.upgrade.BatchUpgradeHandler;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader-agnostic entry point. Every loader module (forge/, and fabric/ or
 * neoforge/ when ported) calls {@link #init()} from its own bootstrap and
 * provides an {@code IPlatform} service implementation. Keep ALL game logic
 * in this module so ports stay glue-only.
 */
public final class PowahExtension {
    public static final String MOD_ID = "powahextension";
    public static final Logger LOGGER = LoggerFactory.getLogger("PowahExtension");

    private PowahExtension() {
    }

    public static void init() {
        PEConfig.load();
        PEItems.register();
        PENetwork.register();
        BatchUpgradeHandler.register();
    }

    /**
     * Client-only init. Call through {@code EnvExecutor.runInEnv(Env.CLIENT, ...)} so the
     * client classes ({@link BatchPreview}, {@code Minecraft}) are never referenced on a
     * dedicated server.
     */
    public static void initClient() {
        ClientTickEvent.CLIENT_PRE.register(BatchPreview::clientTick);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
