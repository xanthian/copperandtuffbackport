package xanthian.copperandtuffbackport.blocks;

import static xanthian.copperandtuffbackport.util.BlockRegistration.registerBlock;

import java.util.HashSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import xanthian.copperandtuffbackport.util.BlockRegistration;
import xanthian.copperandtuffbackport.util.CopperTransitions;
import net.minecraft.world.level.block.*;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import xanthian.copperandtuffbackport.Initialise;
import xanthian.copperandtuffbackport.blocks.custom.CopperTorchBlock;
import xanthian.copperandtuffbackport.blocks.custom.CopperLightningRodBlock;
import xanthian.copperandtuffbackport.blocks.custom.WeatheringCopperDecorations;

public final class ModCopperDecorations {
    public static final Block[] LANTERNS = new Block[8];
    public static final Block[] CHAINS = new Block[8];
    public static final Block[] RODS = new Block[8];
    public static Item NUGGET;
    public static final SimpleParticleType FLAME = new SimpleParticleType(false);
    public static Block TORCH;
    public static Block WALL_TORCH;

    public static void prepare() { }

    public static void register() {
        BlockRegistration.queueItem("copper_nugget", () -> NUGGET);

        TORCH = new CopperTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).lightLevel(state -> 14), FLAME);
        BlockRegistration.registerBlockOnly("copper_torch", TORCH);
        WALL_TORCH = new CopperTorchBlock.CopperWallTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).lightLevel(state -> 14).lootFrom(() -> TORCH), FLAME);
        BlockRegistration.registerBlockOnly("copper_wall_torch", WALL_TORCH);
        BlockRegistration.queueItem("copper_torch", () -> new StandingAndWallBlockItem(TORCH, WALL_TORCH, new Item.Properties(), Direction.DOWN));
        MapColor[] colors = {MapColor.COLOR_ORANGE, MapColor.TERRACOTTA_LIGHT_GRAY, MapColor.WARPED_STEM, MapColor.WARPED_NYLIUM};
        for (int wax = 0; wax < 2; wax++) {
            for (int level = 0; level < 4; level++) {
                int index = wax * 4 + level;
                var oxidation = WeatheringCopper.WeatherState.values()[level];
                String prefix = (wax == 1 ? "waxed_" : "") + ModCopperAgeBlocks.PREFIXES[level];
                var lantern = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).forceSolidOn().strength(3.5F)
                        .sound(SoundType.LANTERN).lightLevel(state -> 15).noOcclusion().pushReaction(PushReaction.DESTROY);
                LANTERNS[index] = wax == 0 ? new WeatheringCopperDecorations.CopperLanternBlock(oxidation, lantern) : new LanternBlock(lantern);
                var chain = BlockBehaviour.Properties.of().forceSolidOn().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.CHAIN).noOcclusion();
                CHAINS[index] = wax == 0 ? new WeatheringCopperDecorations.CopperChainBlock(oxidation, chain) : new ChainBlock(chain);
                if (index == 0) RODS[index] = Blocks.LIGHTNING_ROD;
                else {
                    var rod = BlockBehaviour.Properties.copy(Blocks.LIGHTNING_ROD).mapColor(colors[level]);
                    RODS[index] = wax == 0 ? new WeatheringCopperDecorations.OxidizableCopperLightningRodBlock(oxidation, rod) : new CopperLightningRodBlock(rod);
                    registerBlock(prefix + "lightning_rod", RODS[index]);
                }
                registerBlock(prefix + "copper_lantern", LANTERNS[index]);
                registerBlock(prefix + "copper_chain", CHAINS[index]);
            }
        }
        for (Block[] family : new Block[][]{LANTERNS, CHAINS, RODS}) {
            for (int i = 0; i < 4; i++) {
                CopperTransitions.registerWaxableBlockPair(family[i], family[i + 4]);
                if (i < 3) CopperTransitions.registerOxidizableBlockPair(family[i], family[i + 1]);
            }
        }
    }
    private static ResourceLocation id(String name) { return ResourceLocation.tryBuild(Initialise.MOD_ID, name); }
    private ModCopperDecorations() { }
}
