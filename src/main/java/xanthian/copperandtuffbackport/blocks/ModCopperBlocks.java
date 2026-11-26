package xanthian.copperandtuffbackport.blocks;

import static xanthian.copperandtuffbackport.util.BlockRegistration.registerBlock;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperFullBlock;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import xanthian.copperandtuffbackport.blocks.custom.*;
import xanthian.copperandtuffbackport.util.ModBlockSetTypes;
import xanthian.copperandtuffbackport.util.ModSounds;

import static net.minecraft.world.level.block.Blocks.*;

public class ModCopperBlocks {

    public static final Block CHISELED_COPPER = new OxidizableCopperFullBlock(WeatheringCopper.WeatherState.UNAFFECTED, BlockBehaviour.Properties.copy(COPPER_BLOCK));
    public static final Block EXPOSED_CHISELED_COPPER = new OxidizableCopperFullBlock(WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.copy(EXPOSED_COPPER));
    public static final Block OXIDIZED_CHISELED_COPPER = new OxidizableCopperFullBlock(WeatheringCopper.WeatherState.OXIDIZED, BlockBehaviour.Properties.copy(OXIDIZED_COPPER));
    public static final Block WEATHERED_CHISELED_COPPER = new OxidizableCopperFullBlock(WeatheringCopper.WeatherState.WEATHERED, BlockBehaviour.Properties.copy(WEATHERED_COPPER));
    public static final Block WAXED_CHISELED_COPPER = new Block(BlockBehaviour.Properties.copy(CHISELED_COPPER));
    public static final Block WAXED_EXPOSED_CHISELED_COPPER = new Block(BlockBehaviour.Properties.copy(EXPOSED_CHISELED_COPPER));
    public static final Block WAXED_OXIDIZED_CHISELED_COPPER = new Block(BlockBehaviour.Properties.copy(OXIDIZED_CHISELED_COPPER));
    public static final Block WAXED_WEATHERED_CHISELED_COPPER = new Block(BlockBehaviour.Properties.copy(WEATHERED_CHISELED_COPPER));

    public static final Block COPPER_DOOR = new OxidizableCopperDoorBlock(ModBlockSetTypes.COPPER, WeatheringCopper.WeatherState.UNAFFECTED, BlockBehaviour.Properties.of().mapColor(COPPER_BLOCK.defaultMapColor()).strength(3.0F, 6.0F).noOcclusion().requiresCorrectToolForDrops().pushReaction(PushReaction.DESTROY));
    public static final Block WAXED_COPPER_DOOR = new CopperDoorBlock(BlockBehaviour.Properties.copy(COPPER_DOOR), ModBlockSetTypes.COPPER);
    public static final Block EXPOSED_COPPER_DOOR = new OxidizableCopperDoorBlock(ModBlockSetTypes.COPPER, WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.copy(COPPER_DOOR).mapColor(EXPOSED_COPPER.defaultMapColor()));
    public static final Block WAXED_EXPOSED_COPPER_DOOR = new CopperDoorBlock(BlockBehaviour.Properties.copy(EXPOSED_COPPER_DOOR), ModBlockSetTypes.COPPER);
    public static final Block OXIDIZED_COPPER_DOOR = new OxidizableCopperDoorBlock(ModBlockSetTypes.COPPER, WeatheringCopper.WeatherState.OXIDIZED, BlockBehaviour.Properties.copy(COPPER_DOOR).mapColor(OXIDIZED_COPPER.defaultMapColor()));
    public static final Block WAXED_OXIDIZED_COPPER_DOOR = new CopperDoorBlock(BlockBehaviour.Properties.copy(OXIDIZED_COPPER_DOOR), ModBlockSetTypes.COPPER);
    public static final Block WEATHERED_COPPER_DOOR = new OxidizableCopperDoorBlock(ModBlockSetTypes.COPPER, WeatheringCopper.WeatherState.WEATHERED, BlockBehaviour.Properties.copy(COPPER_DOOR).mapColor(WEATHERED_COPPER.defaultMapColor()));
    public static final Block WAXED_WEATHERED_COPPER_DOOR = new CopperDoorBlock(BlockBehaviour.Properties.copy(WEATHERED_COPPER_DOOR), ModBlockSetTypes.COPPER);

    public static final Block COPPER_TRAPDOOR = new OxidizableCopperTrapdoorBlock(ModBlockSetTypes.COPPER, WeatheringCopper.WeatherState.UNAFFECTED, BlockBehaviour.Properties.of().mapColor(COPPER_BLOCK.defaultMapColor()).strength(3.0F, 6.0F).requiresCorrectToolForDrops().noOcclusion().isValidSpawn((state, world, pos, type) -> false));
    public static final Block WAXED_COPPER_TRAPDOOR = new CopperTrapdoorBlock(BlockBehaviour.Properties.copy(COPPER_TRAPDOOR), ModBlockSetTypes.COPPER);
    public static final Block EXPOSED_COPPER_TRAPDOOR = new OxidizableCopperTrapdoorBlock(ModBlockSetTypes.COPPER, WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.copy(COPPER_TRAPDOOR).mapColor(EXPOSED_COPPER.defaultMapColor()));
    public static final Block WAXED_EXPOSED_COPPER_TRAPDOOR = new CopperTrapdoorBlock(BlockBehaviour.Properties.copy(EXPOSED_COPPER_TRAPDOOR), ModBlockSetTypes.COPPER);
    public static final Block OXIDIZED_COPPER_TRAPDOOR = new OxidizableCopperTrapdoorBlock(ModBlockSetTypes.COPPER, WeatheringCopper.WeatherState.OXIDIZED, BlockBehaviour.Properties.copy(COPPER_TRAPDOOR).mapColor(OXIDIZED_COPPER.defaultMapColor()));
    public static final Block WAXED_OXIDIZED_COPPER_TRAPDOOR = new CopperTrapdoorBlock(BlockBehaviour.Properties.copy(OXIDIZED_COPPER_TRAPDOOR), ModBlockSetTypes.COPPER);
    public static final Block WEATHERED_COPPER_TRAPDOOR = new OxidizableCopperTrapdoorBlock(ModBlockSetTypes.COPPER, WeatheringCopper.WeatherState.WEATHERED, BlockBehaviour.Properties.copy(COPPER_TRAPDOOR).mapColor(WEATHERED_COPPER.defaultMapColor()));
    public static final Block WAXED_WEATHERED_COPPER_TRAPDOOR = new CopperTrapdoorBlock(BlockBehaviour.Properties.copy(WEATHERED_COPPER_TRAPDOOR), ModBlockSetTypes.COPPER);

    public static final Block COPPER_GRATE = new OxidizableCopperGrateBlock(WeatheringCopper.WeatherState.UNAFFECTED, BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(ModSounds.COPPER_GRATE).mapColor(MapColor.COLOR_ORANGE).noOcclusion().requiresCorrectToolForDrops().isValidSpawn((state, world, pos, type) -> false).isRedstoneConductor((state, world, pos) -> false).isSuffocating((state, world, pos) -> false).isViewBlocking((state, world, pos) -> false));
    public static final Block WAXED_COPPER_GRATE = new CopperGrateBlock(BlockBehaviour.Properties.copy(COPPER_GRATE));
    public static final Block EXPOSED_COPPER_GRATE = new OxidizableCopperGrateBlock(WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.copy(COPPER_GRATE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY));
    public static final Block WAXED_EXPOSED_COPPER_GRATE = new CopperGrateBlock(BlockBehaviour.Properties.copy(EXPOSED_COPPER_GRATE));
    public static final Block WEATHERED_COPPER_GRATE = new OxidizableCopperGrateBlock(WeatheringCopper.WeatherState.WEATHERED, BlockBehaviour.Properties.copy(COPPER_GRATE).mapColor(MapColor.WARPED_STEM));
    public static final Block WAXED_WEATHERED_COPPER_GRATE = new CopperGrateBlock(BlockBehaviour.Properties.copy(WEATHERED_COPPER_GRATE));
    public static final Block OXIDIZED_COPPER_GRATE = new OxidizableCopperGrateBlock(WeatheringCopper.WeatherState.OXIDIZED, BlockBehaviour.Properties.copy(COPPER_GRATE).mapColor(MapColor.WARPED_NYLIUM));
    public static final Block WAXED_OXIDIZED_COPPER_GRATE = new CopperGrateBlock(BlockBehaviour.Properties.copy(OXIDIZED_COPPER_GRATE));

    public static final Block COPPER_BULB = new OxidizableCopperBulbBlock(WeatheringCopper.WeatherState.UNAFFECTED, BlockBehaviour.Properties.of().mapColor(COPPER_BLOCK.defaultMapColor()).strength(3.0F, 6.0F).sound(ModSounds.COPPER_BULB).requiresCorrectToolForDrops().isRedstoneConductor((state, world, pos) -> false).lightLevel(bulbLight(15)));
    public static final Block WAXED_COPPER_BULB = new CopperBulbBlock(BlockBehaviour.Properties.copy(COPPER_BULB));
    public static final Block EXPOSED_COPPER_BULB = new OxidizableCopperBulbBlock(WeatheringCopper.WeatherState.EXPOSED, BlockBehaviour.Properties.copy(COPPER_BULB).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY).lightLevel(bulbLight(12)));
    public static final Block WAXED_EXPOSED_COPPER_BULB = new CopperBulbBlock(BlockBehaviour.Properties.copy(EXPOSED_COPPER_BULB));
    public static final Block WEATHERED_COPPER_BULB = new OxidizableCopperBulbBlock(WeatheringCopper.WeatherState.WEATHERED, BlockBehaviour.Properties.copy(COPPER_BULB).mapColor(MapColor.WARPED_STEM).lightLevel(bulbLight(8)));
    public static final Block WAXED_WEATHERED_COPPER_BULB = new CopperBulbBlock(BlockBehaviour.Properties.copy(WEATHERED_COPPER_BULB));
    public static final Block OXIDIZED_COPPER_BULB = new OxidizableCopperBulbBlock(WeatheringCopper.WeatherState.OXIDIZED, BlockBehaviour.Properties.copy(COPPER_BULB).mapColor(MapColor.WARPED_NYLIUM).lightLevel(bulbLight(4)));
    public static final Block WAXED_OXIDIZED_COPPER_BULB = new CopperBulbBlock(BlockBehaviour.Properties.copy(OXIDIZED_COPPER_BULB));

    public static void registerBlocks() {

        registerBlock("chiseled_copper", CHISELED_COPPER);
        registerBlock("exposed_chiseled_copper", EXPOSED_CHISELED_COPPER);
        registerBlock("oxidized_chiseled_copper", OXIDIZED_CHISELED_COPPER);
        registerBlock("weathered_chiseled_copper", WEATHERED_CHISELED_COPPER);
        registerBlock("waxed_chiseled_copper", WAXED_CHISELED_COPPER);
        registerBlock("waxed_exposed_chiseled_copper", WAXED_EXPOSED_CHISELED_COPPER);
        registerBlock("waxed_oxidized_chiseled_copper", WAXED_OXIDIZED_CHISELED_COPPER);
        registerBlock("waxed_weathered_chiseled_copper", WAXED_WEATHERED_CHISELED_COPPER);

        registerBlock("copper_door", COPPER_DOOR);
        registerBlock("exposed_copper_door", EXPOSED_COPPER_DOOR);
        registerBlock("oxidized_copper_door", OXIDIZED_COPPER_DOOR);
        registerBlock("weathered_copper_door", WEATHERED_COPPER_DOOR);
        registerBlock("waxed_copper_door", WAXED_COPPER_DOOR);
        registerBlock("waxed_exposed_copper_door", WAXED_EXPOSED_COPPER_DOOR);
        registerBlock("waxed_oxidized_copper_door", WAXED_OXIDIZED_COPPER_DOOR);
        registerBlock("waxed_weathered_copper_door", WAXED_WEATHERED_COPPER_DOOR);

        registerBlock("copper_trapdoor", COPPER_TRAPDOOR);
        registerBlock("exposed_copper_trapdoor", EXPOSED_COPPER_TRAPDOOR);
        registerBlock("oxidized_copper_trapdoor", OXIDIZED_COPPER_TRAPDOOR);
        registerBlock("weathered_copper_trapdoor", WEATHERED_COPPER_TRAPDOOR);
        registerBlock("waxed_copper_trapdoor", WAXED_COPPER_TRAPDOOR);
        registerBlock("waxed_exposed_copper_trapdoor", WAXED_EXPOSED_COPPER_TRAPDOOR);
        registerBlock("waxed_oxidized_copper_trapdoor", WAXED_OXIDIZED_COPPER_TRAPDOOR);
        registerBlock("waxed_weathered_copper_trapdoor", WAXED_WEATHERED_COPPER_TRAPDOOR);

        registerBlock("copper_grate", COPPER_GRATE);
        registerBlock("exposed_copper_grate", EXPOSED_COPPER_GRATE);
        registerBlock("oxidized_copper_grate", OXIDIZED_COPPER_GRATE);
        registerBlock("weathered_copper_grate", WEATHERED_COPPER_GRATE);
        registerBlock("waxed_copper_grate", WAXED_COPPER_GRATE);
        registerBlock("waxed_exposed_copper_grate", WAXED_EXPOSED_COPPER_GRATE);
        registerBlock("waxed_oxidized_copper_grate", WAXED_OXIDIZED_COPPER_GRATE);
        registerBlock("waxed_weathered_copper_grate", WAXED_WEATHERED_COPPER_GRATE);

        registerBlock("copper_bulb", COPPER_BULB);
        registerBlock("exposed_copper_bulb", EXPOSED_COPPER_BULB);
        registerBlock("oxidized_copper_bulb", OXIDIZED_COPPER_BULB);
        registerBlock("weathered_copper_bulb", WEATHERED_COPPER_BULB);
        registerBlock("waxed_copper_bulb", WAXED_COPPER_BULB);
        registerBlock("waxed_exposed_copper_bulb", WAXED_EXPOSED_COPPER_BULB);
        registerBlock("waxed_oxidized_copper_bulb", WAXED_OXIDIZED_COPPER_BULB);
        registerBlock("waxed_weathered_copper_bulb", WAXED_WEATHERED_COPPER_BULB);


    }

    private static java.util.function.ToIntFunction<net.minecraft.world.level.block.state.BlockState> bulbLight(int light) {
        return state -> state.getValue(CopperBulbBlock.LIT) ? light : 0;
    }
}