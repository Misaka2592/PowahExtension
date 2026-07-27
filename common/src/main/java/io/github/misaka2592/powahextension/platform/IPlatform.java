package io.github.misaka2592.powahextension.platform;

import java.nio.file.Path;

/**
 * The only loader-specific surface the common module needs. Implementations
 * live in each loader module and are discovered via {@link java.util.ServiceLoader}
 * (META-INF/services). When porting to Fabric/NeoForge this interface is the
 * main thing to re-implement.
 */
public interface IPlatform {
    Path getConfigDir();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();
}
