package xanthian.copperandtuffbackport.util;

import xanthian.copperandtuffbackport.util.CopperTransitions;
import xanthian.copperandtuffbackport.blocks.ModCopperBlocks;

public class ModRegistries {

    public static void registerOxidizableBlocks() {
        registerOxidizable();

    }

    private static void registerOxidizable() {

        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.COPPER_DOOR, ModCopperBlocks.EXPOSED_COPPER_DOOR);
        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.EXPOSED_COPPER_DOOR, ModCopperBlocks.WEATHERED_COPPER_DOOR);
        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.WEATHERED_COPPER_DOOR, ModCopperBlocks.OXIDIZED_COPPER_DOOR);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.COPPER_DOOR, ModCopperBlocks.WAXED_COPPER_DOOR);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.EXPOSED_COPPER_DOOR, ModCopperBlocks.WAXED_EXPOSED_COPPER_DOOR);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.WEATHERED_COPPER_DOOR, ModCopperBlocks.WAXED_WEATHERED_COPPER_DOOR);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.OXIDIZED_COPPER_DOOR, ModCopperBlocks.WAXED_OXIDIZED_COPPER_DOOR);

        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.COPPER_TRAPDOOR, ModCopperBlocks.EXPOSED_COPPER_TRAPDOOR);
        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.EXPOSED_COPPER_TRAPDOOR, ModCopperBlocks.WEATHERED_COPPER_TRAPDOOR);
        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.WEATHERED_COPPER_TRAPDOOR, ModCopperBlocks.OXIDIZED_COPPER_TRAPDOOR);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.COPPER_TRAPDOOR, ModCopperBlocks.WAXED_COPPER_TRAPDOOR);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.EXPOSED_COPPER_TRAPDOOR, ModCopperBlocks.WAXED_EXPOSED_COPPER_TRAPDOOR);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.WEATHERED_COPPER_TRAPDOOR, ModCopperBlocks.WAXED_WEATHERED_COPPER_TRAPDOOR);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.OXIDIZED_COPPER_TRAPDOOR, ModCopperBlocks.WAXED_OXIDIZED_COPPER_TRAPDOOR);

        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.COPPER_GRATE, ModCopperBlocks.EXPOSED_COPPER_GRATE);
        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.EXPOSED_COPPER_GRATE, ModCopperBlocks.WEATHERED_COPPER_GRATE);
        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.WEATHERED_COPPER_GRATE, ModCopperBlocks.OXIDIZED_COPPER_GRATE);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.COPPER_GRATE, ModCopperBlocks.WAXED_COPPER_GRATE);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.EXPOSED_COPPER_GRATE, ModCopperBlocks.WAXED_EXPOSED_COPPER_GRATE);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.WEATHERED_COPPER_GRATE, ModCopperBlocks.WAXED_WEATHERED_COPPER_GRATE);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.OXIDIZED_COPPER_GRATE, ModCopperBlocks.WAXED_OXIDIZED_COPPER_GRATE);

        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.COPPER_BULB, ModCopperBlocks.EXPOSED_COPPER_BULB);
        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.EXPOSED_COPPER_BULB, ModCopperBlocks.WEATHERED_COPPER_BULB);
        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.WEATHERED_COPPER_BULB, ModCopperBlocks.OXIDIZED_COPPER_BULB);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.COPPER_BULB, ModCopperBlocks.WAXED_COPPER_BULB);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.EXPOSED_COPPER_BULB, ModCopperBlocks.WAXED_EXPOSED_COPPER_BULB);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.WEATHERED_COPPER_BULB, ModCopperBlocks.WAXED_WEATHERED_COPPER_BULB);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.OXIDIZED_COPPER_BULB, ModCopperBlocks.WAXED_OXIDIZED_COPPER_BULB);

        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.CHISELED_COPPER, ModCopperBlocks.EXPOSED_CHISELED_COPPER);
        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.EXPOSED_CHISELED_COPPER, ModCopperBlocks.WEATHERED_CHISELED_COPPER);
        CopperTransitions.registerOxidizableBlockPair(ModCopperBlocks.WEATHERED_CHISELED_COPPER, ModCopperBlocks.OXIDIZED_CHISELED_COPPER);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.CHISELED_COPPER, ModCopperBlocks.WAXED_CHISELED_COPPER);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.EXPOSED_CHISELED_COPPER, ModCopperBlocks.WAXED_EXPOSED_CHISELED_COPPER);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.WEATHERED_CHISELED_COPPER, ModCopperBlocks.WAXED_WEATHERED_CHISELED_COPPER);
        CopperTransitions.registerWaxableBlockPair(ModCopperBlocks.OXIDIZED_CHISELED_COPPER, ModCopperBlocks.WAXED_OXIDIZED_CHISELED_COPPER);
    }
}
