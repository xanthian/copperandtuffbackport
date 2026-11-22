package xanthian.copperandtuffbackport.util;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xanthian.copperandtuffbackport.Initialise;

/** Registers an ordinary block and its item together, under the same stable ID. */
public final class BlockRegistration {
    public static <T extends Block> T registerBlock(String name, T block) {
        Identifier id = new Identifier(Initialise.MOD_ID, name);
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new FabricItemSettings()));
        return block;
    }

    private BlockRegistration() { }
}
