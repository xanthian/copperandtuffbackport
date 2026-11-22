package xanthian.copperandtuffbackport.util;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import xanthian.copperandtuffbackport.blocks.ModCopperAgeBlocks;
import xanthian.copperandtuffbackport.blocks.ModCopperDecorations;
import xanthian.copperandtuffbackport.blocks.ModTuffBlocks;

import static xanthian.copperandtuffbackport.blocks.ModCopperBlocks.*;

/** Creative layout follows 1.21.1, with Copper Age additions in their 1.21.9 positions. */
public final class ModItemGroup {
    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            addAfter(entries, Blocks.REINFORCED_DEEPSLATE, Blocks.TUFF,
                    ModTuffBlocks.TUFF_STAIRS, ModTuffBlocks.TUFF_SLAB, ModTuffBlocks.TUFF_WALL,
                    ModTuffBlocks.CHISELED_TUFF, ModTuffBlocks.POLISHED_TUFF,
                    ModTuffBlocks.POLISHED_TUFF_STAIRS, ModTuffBlocks.POLISHED_TUFF_SLAB,
                    ModTuffBlocks.POLISHED_TUFF_WALL, ModTuffBlocks.TUFF_BRICKS,
                    ModTuffBlocks.TUFF_BRICK_STAIRS, ModTuffBlocks.TUFF_BRICK_SLAB,
                    ModTuffBlocks.TUFF_BRICK_WALL, ModTuffBlocks.CHISELED_TUFF_BRICKS);

            Block[] solid = {Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER,
                    Blocks.WAXED_COPPER_BLOCK, Blocks.WAXED_EXPOSED_COPPER, Blocks.WAXED_WEATHERED_COPPER, Blocks.WAXED_OXIDIZED_COPPER};
            Block[] slabs = {Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB,
                    Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB};
            Block[] chiseled = {CHISELED_COPPER, EXPOSED_CHISELED_COPPER, WEATHERED_CHISELED_COPPER, OXIDIZED_CHISELED_COPPER,
                    WAXED_CHISELED_COPPER, WAXED_EXPOSED_CHISELED_COPPER, WAXED_WEATHERED_CHISELED_COPPER, WAXED_OXIDIZED_CHISELED_COPPER};
            Block[] grates = {COPPER_GRATE, EXPOSED_COPPER_GRATE, WEATHERED_COPPER_GRATE, OXIDIZED_COPPER_GRATE,
                    WAXED_COPPER_GRATE, WAXED_EXPOSED_COPPER_GRATE, WAXED_WEATHERED_COPPER_GRATE, WAXED_OXIDIZED_COPPER_GRATE};
            Block[] doors = {COPPER_DOOR, EXPOSED_COPPER_DOOR, WEATHERED_COPPER_DOOR, OXIDIZED_COPPER_DOOR,
                    WAXED_COPPER_DOOR, WAXED_EXPOSED_COPPER_DOOR, WAXED_WEATHERED_COPPER_DOOR, WAXED_OXIDIZED_COPPER_DOOR};
            Block[] trapdoors = {COPPER_TRAPDOOR, EXPOSED_COPPER_TRAPDOOR, WEATHERED_COPPER_TRAPDOOR, OXIDIZED_COPPER_TRAPDOOR,
                    WAXED_COPPER_TRAPDOOR, WAXED_EXPOSED_COPPER_TRAPDOOR, WAXED_WEATHERED_COPPER_TRAPDOOR, WAXED_OXIDIZED_COPPER_TRAPDOOR};
            Block[] bulbs = bulbs();
            for (int i = 0; i < 8; i++) {
                addAfter(entries, solid[i], chiseled[i], grates[i]);
                addAfter(entries, slabs[i], ModCopperAgeBlocks.BARS[i], doors[i], trapdoors[i], bulbs[i], ModCopperDecorations.CHAINS[i]);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            addAfter(entries, Blocks.SOUL_TORCH, ModCopperDecorations.TORCH);
            addAfter(entries, Blocks.SOUL_LANTERN, ModCopperDecorations.LANTERNS);
            addAfter(entries, Blocks.CHAIN, ModCopperDecorations.CHAINS);
            addAfter(entries, Blocks.REDSTONE_LAMP, bulbs());
            addAfter(entries, Blocks.CHEST, ModCopperAgeBlocks.CHESTS);
            addAfter(entries, Blocks.LIGHTNING_ROD, java.util.Arrays.copyOfRange(ModCopperDecorations.RODS, 1, 8));
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            addAfter(entries, Blocks.TARGET, WAXED_COPPER_BULB, WAXED_EXPOSED_COPPER_BULB,
                    WAXED_WEATHERED_COPPER_BULB, WAXED_OXIDIZED_COPPER_BULB);
            // 1.21.9 exposes only the unaffected waxed rod and chest in Redstone.
            entries.getDisplayStacks().removeIf(stack -> stack.isOf(Items.LIGHTNING_ROD));
            entries.getSearchTabStacks().removeIf(stack -> stack.isOf(Items.LIGHTNING_ROD));
            addAfter(entries, Blocks.DAYLIGHT_DETECTOR, ModCopperDecorations.RODS[4]);
            addAfter(entries, Blocks.CHEST, ModCopperAgeBlocks.CHESTS[4]);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries ->
                entries.addBefore(Items.IRON_NUGGET, ModCopperDecorations.NUGGET));
    }

    private static Block[] bulbs() {
        return new Block[]{COPPER_BULB, EXPOSED_COPPER_BULB, WEATHERED_COPPER_BULB, OXIDIZED_COPPER_BULB,
                WAXED_COPPER_BULB, WAXED_EXPOSED_COPPER_BULB, WAXED_WEATHERED_COPPER_BULB, WAXED_OXIDIZED_COPPER_BULB};
    }

    private static void addAfter(FabricItemGroupEntries entries, ItemConvertible anchor, ItemConvertible... items) {
        // Advance the anchor: repeatedly inserting after the same item would reverse the order.
        for (ItemConvertible item : items) {
            entries.addAfter(anchor, item);
            anchor = item;
        }
    }

    private ModItemGroup() { }
}
