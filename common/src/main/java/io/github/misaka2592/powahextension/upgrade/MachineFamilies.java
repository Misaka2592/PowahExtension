package io.github.misaka2592.powahextension.upgrade;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import owmii.powah.block.Blcks;
import owmii.powah.block.Tier;
import owmii.powah.lib.registry.VarReg;

/**
 * Index of Powah's tiered machine families. In Powah! Rearchitected every family is a
 * {@code VarReg<Tier, Block>} (see {@code owmii.powah.block.Blcks}); this class only reads
 * that public registry, so a new Powah version just needs this table re-checked.
 *
 * <p>Note: {@code Blcks.ENERGY_CELL} is registered for {@code Tier.values()} (a creative
 * cell exists) while the other families use {@code Tier.getNormalVariants()}, hence the
 * explicit {@code variants} array per family.
 */
public final class MachineFamilies {

    public record Family(String name, VarReg<Tier, Block> blocks, Tier[] variants) {
    }

    /** A block resolved back to its family + tier. */
    public record Located(Family family, Tier tier) {
    }

    private static final Tier[] NORMAL = Tier.getNormalVariants();

    public static final List<Family> FAMILIES = List.of(
            new Family("energy_cell", Blcks.ENERGY_CELL, Tier.values()),
            new Family("ender_cell", Blcks.ENDER_CELL, NORMAL),
            new Family("energy_cable", Blcks.ENERGY_CABLE, NORMAL),
            new Family("ender_gate", Blcks.ENDER_GATE, NORMAL),
            new Family("energizing_rod", Blcks.ENERGIZING_ROD, NORMAL),
            new Family("furnator", Blcks.FURNATOR, NORMAL),
            new Family("magmator", Blcks.MAGMATOR, NORMAL),
            new Family("thermo_generator", Blcks.THERMO_GENERATOR, NORMAL),
            new Family("solar_panel", Blcks.SOLAR_PANEL, NORMAL),
            new Family("reactor", Blcks.REACTOR, NORMAL),
            new Family("player_transmitter", Blcks.PLAYER_TRANSMITTER, NORMAL),
            new Family("energy_hopper", Blcks.ENERGY_HOPPER, NORMAL),
            new Family("energy_discharger", Blcks.ENERGY_DISCHARGER, NORMAL));

    /** Block instance → (family, tier), built on first use (i.e. in-game, after registries freeze). */
    private static volatile Map<Block, Located> index;

    private MachineFamilies() {
    }

    @Nullable
    public static Located locate(Block block) {
        return index().get(block);
    }

    private static Map<Block, Located> index() {
        Map<Block, Located> result = index;
        if (result == null) {
            result = new IdentityHashMap<>();
            for (Family family : FAMILIES) {
                for (Tier tier : family.variants()) {
                    result.put(family.blocks().get(tier), new Located(family, tier));
                }
            }
            index = result;
        }
        return result;
    }
}
