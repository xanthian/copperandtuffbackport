package xanthian.copperandtuffbackport.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import xanthian.copperandtuffbackport.Initialise;

public class ModSounds {
    public static final SoundEvent BLOCK_TUFF_BRICKS_BREAK = register("block.tuff_bricks.break");
    public static final SoundEvent BLOCK_TUFF_BRICKS_FALL = register("block.tuff_bricks.fall");
    public static final SoundEvent BLOCK_TUFF_BRICKS_HIT = register("block.tuff_bricks.hit");
    public static final SoundEvent BLOCK_TUFF_BRICKS_PLACE = register("block.tuff_bricks.place");
    public static final SoundEvent BLOCK_TUFF_BRICKS_STEP = register("block.tuff_bricks.step");
    public static final SoundEvent BLOCK_POLISHED_TUFF_BREAK = register("block.polished_tuff.break");
    public static final SoundEvent BLOCK_POLISHED_TUFF_FALL = register("block.polished_tuff.fall");
    public static final SoundEvent BLOCK_POLISHED_TUFF_HIT = register("block.polished_tuff.hit");
    public static final SoundEvent BLOCK_POLISHED_TUFF_PLACE = register("block.polished_tuff.place");
    public static final SoundEvent BLOCK_POLISHED_TUFF_STEP = register("block.polished_tuff.step");

    public static final SoundEvent BLOCK_COPPER_BULB_BREAK = register("block.copper_bulb.break");
    public static final SoundEvent BLOCK_COPPER_BULB_STEP = register("block.copper_bulb.step");
    public static final SoundEvent BLOCK_COPPER_BULB_PLACE = register("block.copper_bulb.place");
    public static final SoundEvent BLOCK_COPPER_BULB_HIT = register("block.copper_bulb.hit");
    public static final SoundEvent BLOCK_COPPER_BULB_FALL = register("block.copper_bulb.fall");
    public static final SoundEvent BLOCK_COPPER_BULB_TURN_ON = register("block.copper_bulb.turn_on");
    public static final SoundEvent BLOCK_COPPER_BULB_TURN_OFF = register("block.copper_bulb.turn_off");

    public static final SoundEvent BLOCK_COPPER_DOOR_CLOSE = register("block.copper_door.close");
    public static final SoundEvent BLOCK_COPPER_DOOR_OPEN = register("block.copper_door.open");

    public static final SoundEvent BLOCK_COPPER_GRATE_BREAK = register("block.copper_grate.break");
    public static final SoundEvent BLOCK_COPPER_GRATE_STEP = register("block.copper_grate.step");
    public static final SoundEvent BLOCK_COPPER_GRATE_PLACE = register("block.copper_grate.place");
    public static final SoundEvent BLOCK_COPPER_GRATE_HIT = register("block.copper_grate.hit");
    public static final SoundEvent BLOCK_COPPER_GRATE_FALL = register("block.copper_grate.fall");

    public static final SoundEvent BLOCK_COPPER_TRAPDOOR_CLOSE = register("block.copper_trapdoor.close");
    public static final SoundEvent BLOCK_COPPER_TRAPDOOR_OPEN = register("block.copper_trapdoor.open");

    // Append new sounds after the original 1.2 entries so upgraded worlds retain their numeric IDs.
    public static final SoundEvent[] COPPER_CHEST_OPEN = {
            register("block.copper_chest.open"), register("block.copper_chest_weathered.open"), register("block.copper_chest_oxidized.open")};
    public static final SoundEvent[] COPPER_CHEST_CLOSE = {
            register("block.copper_chest.close"), register("block.copper_chest_weathered.close"), register("block.copper_chest_oxidized.close")};

    public static final SoundType TUFF_BRICKS = new SoundType(1.0F, 1.0F,
            ModSounds.BLOCK_TUFF_BRICKS_BREAK, ModSounds.BLOCK_TUFF_BRICKS_STEP, ModSounds.BLOCK_TUFF_BRICKS_PLACE,
            ModSounds.BLOCK_TUFF_BRICKS_HIT, ModSounds.BLOCK_TUFF_BRICKS_FALL);

    public static final SoundType POLISHED_TUFF = new SoundType(1.0F, 1.0F,
            ModSounds.BLOCK_POLISHED_TUFF_BREAK, ModSounds.BLOCK_POLISHED_TUFF_STEP, ModSounds.BLOCK_POLISHED_TUFF_PLACE,
            ModSounds.BLOCK_POLISHED_TUFF_HIT, ModSounds.BLOCK_POLISHED_TUFF_FALL);

    public static final SoundType COPPER_BULB = new SoundType(1.0F, 1.0F,
            ModSounds.BLOCK_COPPER_BULB_BREAK, ModSounds.BLOCK_COPPER_BULB_STEP, ModSounds.BLOCK_COPPER_BULB_PLACE,
            ModSounds.BLOCK_COPPER_BULB_HIT, ModSounds.BLOCK_COPPER_BULB_FALL);

    public static final SoundType COPPER_GRATE = new SoundType(1.0F, 1.0F,
            ModSounds.BLOCK_COPPER_GRATE_BREAK, ModSounds.BLOCK_COPPER_GRATE_STEP, ModSounds.BLOCK_COPPER_GRATE_PLACE,
            ModSounds.BLOCK_COPPER_GRATE_HIT, ModSounds.BLOCK_COPPER_GRATE_FALL);

    private static SoundEvent register(String name) {
        ResourceLocation id = new ResourceLocation(Initialise.MOD_ID, name);
        return BlockRegistration.sound(name);
    }

    public static void registerSounds() {
    }
}
