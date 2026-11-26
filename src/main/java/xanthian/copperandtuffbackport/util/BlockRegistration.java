package xanthian.copperandtuffbackport.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegisterEvent;
import xanthian.copperandtuffbackport.Initialise;
import xanthian.copperandtuffbackport.blocks.custom.CopperChestBlock;
import xanthian.copperandtuffbackport.item.CopperChestItem;

public final class BlockRegistration {
    private static final Map<String, Supplier<Item>> ITEMS = new LinkedHashMap<>();
    private static final Map<String, SoundEvent> SOUNDS = new LinkedHashMap<>();
    private static RegisterEvent blockEvent;
    public static void withBlockRegistry(RegisterEvent event, Runnable action) {
        blockEvent = event;
        try { action.run(); } finally { blockEvent = null; }
    }
    public static <T extends Block> T registerBlockOnly(String name, T block) {
        if (blockEvent == null) throw new IllegalStateException("Blocks must be registered during RegisterEvent");
        blockEvent.register(Registries.BLOCK, Initialise.id(name), () -> block);
        return block;
    }
    public static <T extends Block> T registerBlock(String name, T block) {
        registerBlockOnly(name, block);
        queueItem(name, () -> block instanceof CopperChestBlock ? new CopperChestItem(block) : new BlockItem(block, new Item.Properties()));
        return block;
    }
    public static void queueItem(String name, Supplier<Item> factory) {
        if (ITEMS.putIfAbsent(name, factory) != null) throw new IllegalArgumentException("Duplicate item: " + name);
    }
    public static SoundEvent sound(String name) {
        SoundEvent sound = SoundEvent.createVariableRangeEvent(Initialise.id(name));
        if (SOUNDS.putIfAbsent(name, sound) != null) throw new IllegalArgumentException("Duplicate sound: " + name);
        return sound;
    }
    public static void registerItems(RegisterEvent event) {
        ITEMS.forEach((name, factory) -> event.register(Registries.ITEM, Initialise.id(name), factory));
    }
    public static void registerSounds(RegisterEvent event) {
        SOUNDS.forEach((name, sound) -> event.register(Registries.SOUND_EVENT, Initialise.id(name), () -> sound));
    }
    private BlockRegistration() { }
}
