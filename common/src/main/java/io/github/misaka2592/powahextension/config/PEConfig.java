package io.github.misaka2592.powahextension.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.misaka2592.powahextension.PowahExtension;
import io.github.misaka2592.powahextension.platform.Services;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Simple loader-agnostic JSON config ({@code config/powahextension.json}).
 * Deliberately not ForgeConfigSpec so Fabric/NeoForge ports keep the same file.
 */
public class PEConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "powahextension.json";

    public static PEConfig CONFIG = new PEConfig();

    /**
     * Machine families (base names, see {@code MachineFamilies#FAMILIES}) that may be
     * upgraded in-world. Defaults to every single-block machine family plus cables;
     * the reactor (multiblock) and the ender devices (channel-bound) are opt-in.
     */
    public Set<String> enabledFamilies = new LinkedHashSet<>(Set.of(
            "furnator", "magmator", "thermo_generator", "solar_panel",
            "energy_cell", "energizing_rod", "player_transmitter",
            "energy_hopper", "energy_discharger", "energy_cable"));

    /** Individual block ids (e.g. {@code "powah:furnator_basic"}) that can never be upgraded. */
    public Set<String> extraBlacklist = new LinkedHashSet<>();

    /** Play sound + particles on a successful upgrade. */
    public boolean playEffects = true;

    /** Consume the upgrader even in creative mode. */
    public boolean consumeInCreative = false;

    /** Enable sneak + sprint + right-click batch upgrading of connected machines. */
    public boolean enableBatchUpgrade = true;

    /**
     * Max machines upgraded in one batch operation. Hard-caps the flood-fill search
     * cost; in survival it is additionally capped by the number of upgraders in hand.
     */
    public int batchUpgradeLimit = 64;

    public static void load() {
        Path path = Services.PLATFORM.getConfigDir().resolve(FILE_NAME);
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                PEConfig loaded = GSON.fromJson(reader, PEConfig.class);
                if (loaded != null) {
                    CONFIG = loaded;
                }
            } catch (Exception e) {
                PowahExtension.LOGGER.error("Failed to read config {}, using defaults", path, e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        Path path = Services.PLATFORM.getConfigDir().resolve(FILE_NAME);
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(CONFIG, writer);
            }
        } catch (IOException e) {
            PowahExtension.LOGGER.error("Failed to write config {}", path, e);
        }
    }
}
