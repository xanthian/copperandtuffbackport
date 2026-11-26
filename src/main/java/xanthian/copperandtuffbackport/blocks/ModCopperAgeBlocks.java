package xanthian.copperandtuffbackport.blocks;

import static xanthian.copperandtuffbackport.util.BlockRegistration.registerBlock;

import net.minecraft.world.level.block.state.BlockBehaviour;
import xanthian.copperandtuffbackport.util.CopperTransitions;
import net.minecraft.world.level.block.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import xanthian.copperandtuffbackport.Initialise;
import xanthian.copperandtuffbackport.blocks.custom.CopperChestBlock;
import xanthian.copperandtuffbackport.blocks.custom.CopperBarsBlock;
import xanthian.copperandtuffbackport.blocks.custom.OxidizableCopperChestBlock;
import xanthian.copperandtuffbackport.blockentity.CopperChestBlockEntity;

public final class ModCopperAgeBlocks {
    public static final Block[] BARS = new Block[8];
    public static final CopperChestBlock[] CHESTS = new CopperChestBlock[8];
    public static BlockEntityType<CopperChestBlockEntity> CHEST_ENTITY;
    public static final String[] PREFIXES = {"", "exposed_", "weathered_", "oxidized_"};

    public static void register() {
        MapColor[] colors = {MapColor.COLOR_ORANGE, MapColor.TERRACOTTA_LIGHT_GRAY, MapColor.WARPED_STEM, MapColor.WARPED_NYLIUM};
        for (int wax = 0; wax < 2; wax++) {
            for (int level = 0; level < 4; level++) {
                int index = wax * 4 + level;
                var oxidation = WeatheringCopper.WeatherState.values()[level];
                String prefix = (wax == 1 ? "waxed_" : "") + PREFIXES[level];
                var barSettings = BlockBehaviour.Properties.copy(Blocks.IRON_BARS).sound(SoundType.COPPER);
                BARS[index] = wax == 0 ? new CopperBarsBlock.OxidizableCopperBarsBlock(oxidation, barSettings) : new CopperBarsBlock(barSettings);
                var chestSettings = BlockBehaviour.Properties.of().mapColor(colors[level]).strength(3.0F, 6.0F)
                        .sound(SoundType.COPPER).requiresCorrectToolForDrops();
                CHESTS[index] = wax == 0 ? new OxidizableCopperChestBlock(oxidation, chestSettings)
                        : new CopperChestBlock(oxidation, chestSettings);
                registerBlock(prefix + "copper_bars", BARS[index]);
                registerBlock(prefix + "copper_chest", CHESTS[index]);
            }
        }
        for (int level = 0; level < 4; level++) {
            CopperTransitions.registerWaxableBlockPair(BARS[level], BARS[level + 4]);
            CopperTransitions.registerWaxableBlockPair(CHESTS[level], CHESTS[level + 4]);
            if (level < 3) {
                CopperTransitions.registerOxidizableBlockPair(BARS[level], BARS[level + 1]);
                CopperTransitions.registerOxidizableBlockPair(CHESTS[level], CHESTS[level + 1]);
            }
        }
    }


    private ModCopperAgeBlocks() { }
}
