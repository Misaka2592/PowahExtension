package io.github.misaka2592.powahextension.platform;

import java.util.ServiceLoader;

/** Service-loader based platform resolution (architectury-example-mod pattern). */
public final class Services {
    public static final IPlatform PLATFORM = load(IPlatform.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No platform implementation found for " + clazz.getName()));
    }
}
