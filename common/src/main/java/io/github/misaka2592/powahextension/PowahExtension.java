package io.github.misaka2592.powahextension;

import io.github.misaka2592.powahextension.config.PEConfig;
import io.github.misaka2592.powahextension.registry.PEItems;
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
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
