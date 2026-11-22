package xanthian.copperandtuffbackport.blocks;

import static xanthian.copperandtuffbackport.util.BlockRegistration.registerBlock;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
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
        MapColor[] colors = {MapColor.ORANGE, MapColor.TERRACOTTA_LIGHT_GRAY, MapColor.DARK_AQUA, MapColor.TEAL};
        for (int wax = 0; wax < 2; wax++) {
            for (int level = 0; level < 4; level++) {
                int index = wax * 4 + level;
                var oxidation = Oxidizable.OxidationLevel.values()[level];
                String prefix = (wax == 1 ? "waxed_" : "") + PREFIXES[level];
                var barSettings = FabricBlockSettings.copyOf(Blocks.IRON_BARS).sounds(BlockSoundGroup.COPPER);
                BARS[index] = wax == 0 ? new CopperBarsBlock.OxidizableCopperBarsBlock(oxidation, barSettings) : new CopperBarsBlock(barSettings);
                var chestSettings = FabricBlockSettings.create().mapColor(colors[level]).strength(3.0F, 6.0F)
                        .sounds(BlockSoundGroup.COPPER).requiresTool();
                CHESTS[index] = wax == 0 ? new OxidizableCopperChestBlock(oxidation, chestSettings)
                        : new CopperChestBlock(oxidation, chestSettings);
                registerBlock(prefix + "copper_bars", BARS[index]);
                registerBlock(prefix + "copper_chest", CHESTS[index]);
            }
        }
        CHEST_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(Initialise.MOD_ID, "copper_chest"),
                FabricBlockEntityTypeBuilder.create(CopperChestBlockEntity::new, CHESTS).build());
        for (int level = 0; level < 4; level++) {
            OxidizableBlocksRegistry.registerWaxableBlockPair(BARS[level], BARS[level + 4]);
            OxidizableBlocksRegistry.registerWaxableBlockPair(CHESTS[level], CHESTS[level + 4]);
            if (level < 3) {
                OxidizableBlocksRegistry.registerOxidizableBlockPair(BARS[level], BARS[level + 1]);
                OxidizableBlocksRegistry.registerOxidizableBlockPair(CHESTS[level], CHESTS[level + 1]);
            }
        }
    }


    private ModCopperAgeBlocks() { }
}
